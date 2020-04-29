package net.sf.l2j.gameserver.votesystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Announcement;

public class NetWorkAuto extends AutoVoteMain
{
	
	protected static int getL2NetworkVotes()
	{
		int votes = -1;
		
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_L2NETWORK_LINK_NET);
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.addRequestProperty("User-Agent", "L2Network");
			con.setConnectTimeout(5000);
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						if (inputLine.contains("color:#e7ebf2"))
						{
							votes = Integer.valueOf(inputLine.split(">")[2].replace("</b", ""));
							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Server L2Network is offline Trying to Reconnect");
			Announcement.VoteAnnouncements("L2Network is offline...Trying to Reconnect");
		}
		return votes;
	}
	
	public static class CheckForRewardNet implements Runnable
	{
		
		@Override
		public void run()
		{
			if (_l2network)
			{
				int l2network_votes = getL2NetworkVotes();
				if ((l2network_votes != 0) && (l2network_votes >= getL2NetworkVoteCount() + Config.VOTES_FOR_REWARD_NET))
				{
					rewardPlayer();
					setL2NetworkVoteCount(l2network_votes);
					Announcement.VoteAnnouncements("Network: You have been rewarded! Next reward at " + (getL2NetworkVoteCount() + Config.VOTES_FOR_REWARD_NET) + " votes.");
				}
			}
		}
	}
}