package net.sf.l2j.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Reborn12
 */
public class VoteApiManager extends Folk
{
	private static final Logger _log = Logger.getLogger(VoteApiManager.class.getName());
	// Queries
	private static final String DELETE_QUERY = "DELETE FROM mods_voting_reward WHERE time < ?";
	private static final String SELECT_QUERY = "SELECT * FROM mods_voting_reward";
	private static final String INSERT_QUERY = "INSERT INTO mods_voting_reward (data, scope, time) VALUES (?, ?, ?)";
	// Constants
	private static final long VOTING_INTERVAL = TimeUnit.HOURS.toMillis(12);
	// api LINKS
	private static final String TOPZONE_API_URL = "http://l2topzone.com/api.php?API_KEY=%s&SERVER_ID=%s&IP=%s";
	private static final String NETWORK_API_URL = "https://l2network.eu/index.php?a=in&u=%s&ipc=%s";
	private static final String HOPZONE_API_URL = "https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s";
	
	// Cache
	private static final Map<UserScope, ScopeContainer> VOTTERS_CACHE = new EnumMap<>(UserScope.class);
	
	public VoteApiManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		load();
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		long time = getLastVotedTime(player);
		
		if (actualCommand.startsWith("getreward"))
		{
			// Make sure player haven't received reward already!
			if (time > 0)
			{
				sendReEnterMessage(time, player);
				return;
			}
			// Check if player on topzone
			if (Voted(player.getClient().getConnection().getInetAddress().getHostAddress()))
			{
				// Give him reward
				giveReward(player);
				
				// Mark down this reward as given
				markAsVotted(player);
				
				// Say thanks ;)
				player.sendMessage("Done! You have been rewarded.");
			}
			else
				player.sendMessage("No votes count for the last 12 hours.");
		}
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		
		// Calculate the distance between the Player and the L2Npc
		if (!canInteract(player))
		{
			// Notify the Player AI with INTERACT
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
		}
		else
			showHtmlWindow(player);
		
		// Send ActionFailed to the player in order to avoid he stucks
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showHtmlWindow(Player player)
	{
		try
		{
			final StringBuilder sb = new StringBuilder();
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			
			final long timeRemaining = getLastVotedTime(player);
			boolean isValid = timeRemaining > System.currentTimeMillis();
			
			if (isValid)
			{
				int timeStamp = (int) ((timeRemaining - System.currentTimeMillis()) / 1000);
				StringUtil.append(sb, "Vote In: ", StringUtil.getTimeStamp(timeStamp), "");
			}
			else
			{
				StringUtil.append(sb, "You can Vote now!");
			}
			
			html.setFile("data/html/mods/VoteManagerApi/Vote.htm");
			html.replace("%time%", sb.toString());
			html.replace("%name%", player.getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static boolean Voted(String ip)
	{
		return votedHop(ip) && votedNet(ip) && votedTop(ip);
	}
	
	private static final void load()
	{
		// Initialize the cache
		for (UserScope scope : UserScope.values())
		{
			VOTTERS_CACHE.put(scope, new ScopeContainer());
		}
		
		// Cleanup old entries and load the data for votters
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(DELETE_QUERY); Statement st = con.createStatement())
		{
			ps.setLong(1, System.currentTimeMillis());
			ps.execute();
			
			// Load the data
			try (ResultSet rset = st.executeQuery(SELECT_QUERY))
			{
				while (rset.next())
				{
					final String data = rset.getString("data");
					final UserScope scope = UserScope.findByName(rset.getString("scope"));
					final Long time = rset.getLong("time");
					if (scope != null)
					{
						VOTTERS_CACHE.get(scope).registerVotter(data, time);
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, VoteApiManager.class.getSimpleName() + ": " + e.getMessage(), e);
		}
	}
	
	private static void giveReward(Player activeChar)
	{
		activeChar.addItem("Reward", Config.API_REWARD_ID, Config.API_REWARD_COUNT, activeChar, true);
	}
	
	private static void sendReEnterMessage(long time, Player player)
	{
		if (time > System.currentTimeMillis())
		{
			final long remainingTime = (time - System.currentTimeMillis()) / 1000;
			final int hours = (int) (remainingTime / 3600);
			final int minutes = (int) ((remainingTime % 3600) / 60);
			final int seconds = (int) ((remainingTime % 3600) % 60);
			
			String msg = "You have received your reward already try again in: " + hours + " hours";
			if (minutes > 0)
			{
				msg += " " + minutes + " minutes";
			}
			if (seconds > 0)
			{
				msg += " " + seconds + " seconds";
			}
			player.sendMessage(msg);
		}
	}
	
	private static final long getLastVotedTime(Player activeChar)
	{
		for (Entry<UserScope, ScopeContainer> entry : VOTTERS_CACHE.entrySet())
		{
			final String data = entry.getKey().getData(activeChar);
			final long reuse = entry.getValue().getReuse(data);
			if (reuse > 0)
			{
				return reuse;
			}
		}
		return 0;
	}
	
	private static final void markAsVotted(final Player player)
	{
		final long reuse = System.currentTimeMillis() + VOTING_INTERVAL;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(INSERT_QUERY))
		{
			for (UserScope scope : UserScope.values())
			{
				final String data = scope.getData(player);
				final ScopeContainer container = VOTTERS_CACHE.get(scope);
				container.registerVotter(data, reuse);
				
				ps.setString(1, data);
				ps.setString(2, scope.name());
				ps.setLong(3, reuse);
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, VoteApiManager.class.getSimpleName() + ": " + e.getMessage(), e);
		}
	}
	
	public static final boolean votedTop(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(TOPZONE_API_URL, Config.API_KEYTOPZONE, Config.SERVERID_KEYTOPZONE, ip));
			
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200) // OK
			{
				try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					return br.readLine().equals("TRUE");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static final boolean votedHop(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(HOPZONE_API_URL, Config.APIKEY_HOPZONE, ip));
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						sb.append(inputLine);
					}
				}
				return sb.toString().contains("true");
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	public static final boolean votedNet(String ip)
	{
		try
		{
			final URL obj = new URL(String.format(NETWORK_API_URL, Config.SERVERID_NETWORK, ip));
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setRequestProperty("User-Agent", "L2Network");
			con.setConnectTimeout(5000);
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						sb.append(inputLine);
					}
				}
				return sb.toString().equals("1");
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	private enum UserScope
	{
		ACCOUNT
		{
			@Override
			public String getData(Player player)
			{
				return player.getAccountName();
			}
		},
		IP
		{
			@Override
			public String getData(Player player)
			{
				return player.getClient().getConnection().getInetAddress().getHostAddress();
			}
		};
		
		public abstract String getData(Player player);
		
		public static UserScope findByName(String name)
		{
			for (UserScope scope : values())
			{
				if (scope.name().equals(name))
				{
					return scope;
				}
			}
			return null;
		}
	}
	
	private static class ScopeContainer
	{
		private final Map<String, Long> _votters = new ConcurrentHashMap<>();
		
		public ScopeContainer()
		{
		}
		
		public void registerVotter(String data, long reuse)
		{
			_votters.put(data, reuse);
		}
		
		public long getReuse(String data)
		{
			if (_votters.containsKey(data))
			{
				long time = _votters.get(data);
				if (time > System.currentTimeMillis())
				{
					return time;
				}
			}
			return 0;
		}
	}
}