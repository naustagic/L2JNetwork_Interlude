package net.sf.l2j.gameserver.votesystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.votesystem.HopZoneAuto.CheckForRewardHop;
import net.sf.l2j.gameserver.votesystem.NetWorkAuto.CheckForRewardNet;
import net.sf.l2j.gameserver.votesystem.TopZoneAuto.CheckForRewardTop;

public class AutoVoteMain
{
	static final Logger LOGGER = Logger.getLogger(AutoVoteMain.class.getName());
	static Map<String, Integer> playerIps = new HashMap<>();
	private static int _topzoneVotesCount = 0;
	private static int _l2networkVotesCount = 0;
	private static int _hopzoneVotesCount = 0;
	private static int _BrasilVotesCount = 0;
	static boolean _topzone = false;
	static boolean _l2network = false;
	static boolean _hopzone = false;
	static boolean _brasil = true;
	
	AutoVoteMain()
	{
		LOGGER.info("VoteSystem Loaded.");
		if (_topzone)
		{
			int topzone_votes = TopZoneAuto.getTopZoneVotes();
			
			if (topzone_votes == -1)
				topzone_votes = 0;
			
			setTopZoneVoteCount(topzone_votes);
		}
		
		if (_brasil)
		{
			int l2brasil_votes = getL2BrasilVotes();
			
			if (l2brasil_votes == -1)
				l2brasil_votes = 0;
			
			setL2BrasilVoteCount(l2brasil_votes);
		}
		
		if (_l2network)
		{
			int l2network_votes = NetWorkAuto.getL2NetworkVotes();
			
			if (l2network_votes == -1)
				l2network_votes = 0;
			
			setL2NetworkVoteCount(l2network_votes);
		}
		
		if (_hopzone)
		{
			int hopzone_votes = HopZoneAuto.getHopZoneVotes();
			
			if (hopzone_votes == -1)
				hopzone_votes = 0;
			
			setHopZoneVoteCount(hopzone_votes);
		}
		startTimer();
	}
	
	static void setL2BrasilVoteCount(int l2brasil_votes)
	{
		_BrasilVotesCount = l2brasil_votes;
	}
	
	static int getL2BrasilVoteCount()
	{
		return _BrasilVotesCount;
	}
	
	/**
	 * @return
	 */
	static int getL2BrasilVotes()
	{
		int votes = -1;
		
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_BRASIL_LINK_BRA);
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.addRequestProperty("User-Agent", "L2jBrasil");
			con.setConnectTimeout(5000);
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						if (inputLine.contains("(Total):</b> "))
						{
							votes = Integer.parseInt(inputLine.split("(Total)")[1].split(" ")[1].replace("<br", ""));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Server Brasil is offline Trying to Reconnect");
			Announcement.VoteAnnouncements("Brasil is offline...Trying to Reconnect");
		}
		return votes;
	}
	
	private static void startTimer()
	{
		ThreadPool.scheduleAtFixedRate(new CheckForRewardBrasil(), Config.VOTES_SYSTEM_INITIAL_DELAY_BRA * 60 * 1000, Config.VOTES_SYSTEM_INITIAL_DELAY_BRA * 60 * 1000);
		ThreadPool.scheduleAtFixedRate(new CheckForRewardNet(), Config.VOTES_SYSTEM_INITIAL_DELAY_NET * 60 * 1000, Config.VOTES_SYSTEM_INITIAL_DELAY_NET * 60 * 1000);
		ThreadPool.scheduleAtFixedRate(new CheckForRewardTop(), Config.VOTES_SYSTEM_INITIAL_DELAY_TOP * 60 * 1000, Config.VOTES_SYSTEM_INITIAL_DELAY_TOP * 60 * 1000);
		ThreadPool.scheduleAtFixedRate(new CheckForRewardHop(), Config.VOTES_SYSTEM_INITIAL_DELAY_HOP * 60 * 1000, Config.VOTES_SYSTEM_INITIAL_DELAY_HOP * 60 * 1000);
	}
	
	public static class CheckForRewardBrasil implements Runnable
	{
		
		@Override
		public void run()
		{
			if (_brasil)
			{
				int l2jbrasil_votes = getL2BrasilVotes();
				if ((l2jbrasil_votes != 0) && (l2jbrasil_votes >= getL2BrasilVoteCount() + Config.VOTES_FOR_REWARD_BRA))
				{
					rewardPlayer();
					setL2BrasilVoteCount(l2jbrasil_votes);
					Announcement.VoteAnnouncements("Brasil: You have been rewarded! Next reward at " + (getL2BrasilVoteCount() + Config.VOTES_FOR_REWARD_BRA) + " votes.");
				}
			}
		}
	}
	
	protected static void rewardPlayer()
	{
		for (Player p : World.getInstance().getPlayers())
		{
			if (p.getClient() == null || p.getClient().isDetached()) // offline shops protection
				continue;
			if (p.isAfking())
				continue;
			
			boolean canReward = false;
			String pIp = p.getClient().getConnection().getInetAddress().getHostAddress();
			
			if (playerIps.containsKey(pIp))
			{
				int count = playerIps.get(pIp);
				if (count < Config.VOTE_BOXES_ALLOWED)
				{
					playerIps.remove(pIp);
					playerIps.put(pIp, count + 1);
					canReward = true;
				}
			}
			else
			{
				canReward = true;
				playerIps.put(pIp, 1);
			}
			
			if (canReward)
				for (int i : Config.VOTES_REWARDS_LIST_AUTOVOTE.keySet())
					p.addItem("Vote reward.", i, Config.VOTES_REWARDS_LIST_AUTOVOTE.get(i), p, true);
			else
				p.sendMessage("Already " + Config.VOTE_BOXES_ALLOWED + " character(s) of your ip have been rewarded, so this character won't be rewarded.");
		}
		playerIps.clear();
	}
	
	protected static void setTopZoneVoteCount(int voteCount)
	{
		_topzoneVotesCount = voteCount;
	}
	
	protected static int getTopZoneVoteCount()
	{
		return _topzoneVotesCount;
		
	}
	
	protected static void setL2NetworkVoteCount(int voteCount)
	{
		_l2networkVotesCount = voteCount;
	}
	
	protected static int getL2NetworkVoteCount()
	{
		return _l2networkVotesCount;
	}
	
	protected static void setHopZoneVoteCount(int voteCount)
	{
		_hopzoneVotesCount = voteCount;
	}
	
	protected static int getHopZoneVoteCount()
	{
		return _hopzoneVotesCount;
	}
	
	public static AutoVoteMain getInstance()
	{
		
		if (Config.VOTES_SITE_TOPZONE_LINK_TOP != null && !Config.VOTES_SITE_TOPZONE_LINK_TOP.equals(""))
		{
			_topzone = true;
		}
		if (Config.VOTES_SITE_L2NETWORK_LINK_NET != null && !Config.VOTES_SITE_L2NETWORK_LINK_NET.equals(""))
		{
			_l2network = true;
		}
		if (Config.VOTES_SITE_HOPZONE_LINK_HOP != null && !Config.VOTES_SITE_HOPZONE_LINK_HOP.equals(""))
		{
			_hopzone = true;
		}
		if (Config.VOTES_SITE_BRASIL_LINK_BRA != null && !Config.VOTES_SITE_BRASIL_LINK_BRA.equals(""))
		{
			_brasil = true;
		}
		if (_topzone && _l2network && _hopzone && _brasil)
		{
			return SingletonHolder._instance;
		}
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoVoteMain _instance = new AutoVoteMain();
	}
}