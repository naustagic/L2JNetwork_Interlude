package net.sf.l2j.gameserver.votesystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Announcement;

public class HopZoneAuto extends AutoVoteMain
{
	
	protected static int getHopZoneVotes()
	{
		int votes = -1;
		
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_HOPZONE_LINK_HOP);
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.addRequestProperty("User-Agent", "L2Hopzone");
			con.setConnectTimeout(5000);
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String line;
					while ((line = in.readLine()) != null)
					{
						if (line.contains("Total Votes") || line.contains("rank tooltip") || line.contains("no steal make love") || line.contains("no votes here") || line.contains("bang, you don't have votes") || line.contains("la vita e bella") || line.contains("rank anonymous tooltip"))
						{
							String inputLine = line.split(">")[2].replace("</span", "");
							votes = Integer.parseInt(inputLine);
							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Server HOPZONE is offline Trying to Reconnect");
			Announcement.VoteAnnouncements("HOPZONE is offline...Trying to Reconnect");
		}
		return votes;
	}
	
	public static class CheckForRewardHop implements Runnable
	{
		@Override
		public void run()
		{
			if (_hopzone)
			{
				int hopzone_votes = getHopZoneVotes();
				
				if (hopzone_votes != 0 && hopzone_votes >= getHopZoneVoteCount() + Config.VOTES_FOR_REWARD_HOP)
				{
					rewardPlayer();
					setHopZoneVoteCount(hopzone_votes);
					Announcement.VoteAnnouncements("Hopzone: You have been rewarded! Next reward at " + (getHopZoneVoteCount() + Config.VOTES_FOR_REWARD_HOP) + " votes.");
				}
			}
		}
	}
}