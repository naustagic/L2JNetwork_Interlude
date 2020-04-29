package net.sf.l2j.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Reborn12
 */
public class ApiVoteManagerSingle extends Folk
{
	private static final Logger _log = Logger.getLogger(ApiVoteManagerSingle.class.getName());
	private static final String LOAD = "SELECT * FROM custom_vote_manager";
	private static final String INSERT = "INSERT INTO custom_vote_manager VALUES (?,?,?)";
	private static final String UPDATE = "UPDATE custom_vote_manager SET time = ? WHERE ip = ? AND topsite = ?";
	public static final Map<String, Long> _mapL2Network = new HashMap<>();
	public static final Map<String, Long> _mapL2TopZone = new HashMap<>();
	public static final Map<String, Long> _mapL2HopZone = new HashMap<>();
	
	private static final String TOPZONE_API_URL = "http://l2topzone.com/api.php?API_KEY=%s&SERVER_ID=%s&IP=%s";
	private static final String NETWORK_API_URL = "https://l2network.eu/index.php?a=in&u=%s&ipc=%s";
	private static final String HOPZONE_API_URL = "https://api.hopzone.net/lineage2/vote?token=%s&ip_address=%s";
	
	public ApiVoteManagerSingle(final int id, final NpcTemplate template)
	{
		super(id, template);
		load();
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/custom/VoteManager.htm");
		html.replace("%name%", player.getName());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%netvote%", isVotedForL2Network(player) ? "Voted" : "Not Voted");
		html.replace("%hopvote%", isVotedForL2HopZone(player) ? "Voted" : "Not Voted");
		html.replace("%Topvote%", isVotedForL2TopZone(player) ? "Voted" : "Not Voted");
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if ((player == null) || (command == null))
			return;
		
		if (command.equals("getRewardL2Network"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			if (Config.L2NETWORK_ID == null)
				return;
			
			if (getVoteTime(player, _mapL2Network, "L2Network"))
				return;
			
			if (isVotedForL2Network(player))
			{
				setDatabase(player, "L2Network");
				giveReward(player, "L2Network");
			}
			else
			{
				player.sendMessage("No votes count for Network. Go vote!");
				return;
			}
		}
		
		else if (command.equals("getRewardL2TopZone"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			if (Config.L2TOPZONE_APIKEY == null)
				return;
			
			if (getVoteTime(player, _mapL2TopZone, "L2TopZone"))
				return;
			
			if (isVotedForL2TopZone(player))
			{
				setDatabase(player, "L2TopZone");
				giveReward(player, "L2TopZone");
			}
			else
			{
				player.sendMessage("No votes count for Topzone. Go vote!");
				return;
			}
		}
		else if (command.equals("getRewardL2HopZone"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			if (Config.L2HOPZONE_APIKEY == null)
				return;
			
			if (getVoteTime(player, _mapL2HopZone, "L2HopZone"))
				return;
			
			if (isVotedForL2HopZone(player))
			{
				setDatabase(player, "L2HopZone");
				giveReward(player, "L2HopZone");
			}
			else
			{
				player.sendMessage("No votes count for Hopzone. Go vote!");
				return;
			}
		}
		
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	/*
	 * private static boolean isVotedForL2Network(final L2PcInstance player) { if (player == null) return false; final String ip = player.getClient().getConnection().getInetAddress().getHostAddress(); try { final URL url = new URL(Config.L2NETWORK_SITE + ip); final HttpURLConnection connection =
	 * (HttpURLConnection) url.openConnection(); connection.setRequestProperty("User-Agent", "L2Network"); final int responseCode = connection.getResponseCode(); if (responseCode == 200) { final StringBuilder sb = new StringBuilder(); try (BufferedReader br = new BufferedReader(new
	 * InputStreamReader(connection.getInputStream()))) { String text; while ((text = br.readLine()) != null) { sb.append(text); } //return sb.toString().equals("1"); } } connection.disconnect(); } catch (Exception e) { _log.warn(L2VoteManagerInstance.class.getSimpleName() + " " + e); } return
	 * false; }
	 */
	private static boolean isVotedForL2Network(final Player player)
	{
		if (player == null)
			return false;
		
		final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
		// final String ip = player.getHWID();
		try
		{
			final URL obj = new URL(String.format(NETWORK_API_URL, Config.L2NETWORK_ID, ip));
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
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + " " + e);
		}
		return false;
	}
	
	private static boolean isVotedForL2HopZone(final Player player)
	{
		if (player == null)
			return false;
		
		final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
		// final String ip = player.getHWID();
		try
		{
			final URL obj = new URL(String.format(HOPZONE_API_URL, Config.L2HOPZONE_APIKEY, ip));
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
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + " " + e);
		}
		return false;
	}
	
	private static boolean isVotedForL2TopZone(final Player player)
	{
		if (player == null)
			return false;
		
		final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
		// final String ip = player.getHWID();
		try
		{
			final URL obj = new URL(String.format(TOPZONE_API_URL, Config.L2TOPZONE_APIKEY, Config.L2TOPZONE_ID, ip));
			
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
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + " " + e);
		}
		return false;
	}
	
	private static boolean getVoteTime(final Player player, final Map<String, Long> map, final String topName)
	{
		if ((player == null) || (map == null))
			return true;
		
		final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
		// final String ip = player.getHWID();
		if ((map.get(ip) != null) && (map.get(ip).longValue() > System.currentTimeMillis()))
		{
			final long time = (map.get(ip).longValue() - System.currentTimeMillis()) / 1000;
			final int h = (int) time / 3600;
			final int m = (int) (time - (h * 3600)) / 60;
			final int s = (int) (time - (h * 3600) - (m * 60));
			player.sendMessage("You can vote only once per 12 hours! Time until next vote: " + h + " hours, " + m + " minutes and " + s + " seconds.");
			return true;
		}
		
		return false;
	}
	
	private static void setDatabase(final Player player, final String topName)
	{
		if ((player == null) || (topName == null))
			return;
		
		final String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
		// final String ip = player.getHWID();
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, 12);
		
		if (topName.equalsIgnoreCase("L2Network"))
		{
			if (_mapL2Network.get(ip) != null)
				update(cal.getTimeInMillis(), ip, "L2Network");
			else
				insert(ip, cal.getTimeInMillis(), "L2Network");
		}
		
		if (topName.equalsIgnoreCase("L2TopZone"))
		{
			if (_mapL2TopZone.get(ip) != null)
				update(cal.getTimeInMillis(), ip, "L2TopZone");
			else
				insert(ip, cal.getTimeInMillis(), "L2TopZone");
		}
		if (topName.equalsIgnoreCase("L2HopZone"))
		{
			if (_mapL2HopZone.get(ip) != null)
				update(cal.getTimeInMillis(), ip, "L2HopZone");
			else
				insert(ip, cal.getTimeInMillis(), "L2HopZone");
		}
	}
	
	private static void giveReward(final Player player, final String topName)
	{
		if ((player == null) || (topName == null) || (Config.VOTE_MANAGER_REWARD == null))
			return;
		
		for (String data : Config.VOTE_MANAGER_REWARD)
		{
			final String[] reward = data.split(",");
			player.addItem("reward", Integer.parseInt(reward[0]), Integer.parseInt(reward[1]), player, true);
		}
		
		player.sendMessage("Thanks for voting!");
		player.sendPacket(new ExShowScreenMessage("Thanks for voting!", 5000));
	}
	
	public static void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement(LOAD); ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				if (rset.getString("topsite").equalsIgnoreCase("L2Network"))
					_mapL2Network.put(rset.getString("ip"), rset.getLong("time"));
				
				if (rset.getString("topsite").equalsIgnoreCase("L2TopZone"))
					_mapL2TopZone.put(rset.getString("ip"), rset.getLong("time"));
				
				if (rset.getString("topsite").equalsIgnoreCase("L2HopZone"))
					_mapL2HopZone.put(rset.getString("ip"), rset.getLong("time"));
			}
		}
		catch (Exception e)
		{
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + ": " + e);
		}
	}
	
	private static void insert(final String ip, final long time, final String topsite)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement(INSERT))
		{
			statement.setString(1, ip);
			statement.setLong(2, time);
			statement.setString(3, topsite);
			statement.executeUpdate();
			
			if (topsite.equalsIgnoreCase("L2Network"))
				_mapL2Network.put(ip, time);
			
			if (topsite.equalsIgnoreCase("L2TopZone"))
				_mapL2TopZone.put(ip, time);
			if (topsite.equalsIgnoreCase("L2HopZone"))
				_mapL2HopZone.put(ip, time);
		}
		catch (Exception e)
		{
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + ": " + e);
		}
	}
	
	private static void update(final long time, final String ip, final String topsite)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement(UPDATE))
		{
			statement.setLong(1, time);
			statement.setString(2, ip);
			statement.setString(3, topsite);
			statement.executeUpdate();
			
			if (topsite.equalsIgnoreCase("L2Network"))
				_mapL2Network.put(ip, time);
			
			if (topsite.equalsIgnoreCase("L2TopZone"))
				_mapL2TopZone.put(ip, time);
			
			if (topsite.equalsIgnoreCase("L2HopZone"))
				_mapL2HopZone.put(ip, time);
		}
		catch (Exception e)
		{
			_log.warning(ApiVoteManagerSingle.class.getSimpleName() + ": " + e);
		}
	}
}