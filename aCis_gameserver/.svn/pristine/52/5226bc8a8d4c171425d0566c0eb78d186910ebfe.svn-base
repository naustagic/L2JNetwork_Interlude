/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.l2j.gameserver.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

/**
 * @Author Reborn12
 */

public class VoteHandler
{
	public VoteHandler()
	{
	}
	
	public static String whoIsVoting()
	{
		for (Player player : World.getInstance().getPlayers())
			if (player.isVoting())
				return player.getName();
			
		return "NONE";
	}
	
	public static int getTopZoneVotes()
	{
		int votes = -1;
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_TOPZONE_URL);
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.addRequestProperty("User-Agent", "L2TopZone");
			con.setConnectTimeout(5000);
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
					{
						if (inputLine.contains("Votes:"))
						{
							votes = Integer.valueOf(inputLine.split("<br>")[1].replace("</div>", ""));
							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("TOPZONE is offline. We will check reward as it will be online again.");
		}
		
		return votes;
	}
	
	public static void tzvote(final Player player)
	{
		long LastTZVote = 0L;
		long voteDelay = 43200000L;
		final int actualvotes;
		
		actualvotes = getTopZoneVotes();
		
		class tzvotetask implements Runnable
		{
			private final Player p;
			
			public tzvotetask(Player player)
			
			{
				p = player;
			}
			
			@Override
			public void run()
			{
				if (actualvotes < getTopZoneVotes())
				{
					p.setIsVoting(false);
					VoteHandler.updateLastTZVote(p);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "Thanks for Voting."));
					p.addItem("TZreward", Config.VOTE_REWARD_ID, Config.VOTE_REWARD_ID_COUNT, null, true);
				}
				else
				{
					p.setIsVoting(false);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You didn't vote. Try Again Later."));
				}
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT LastTZVote FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				LastTZVote = rset.getLong("LastTZVote");
			}
			statement.close();
			rset.close();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Vote Manager: could not select LastTZVote in characters " + e);
		}
		
		if ((LastTZVote + voteDelay) < System.currentTimeMillis())
		{
			for (Player actualchar : World.getInstance().getPlayers())
			{
				if (actualchar.isVoting())
				{
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", actualchar.getName() + ", is voting now. Please wait for a while."));
					return;
				}
			}
			player.setIsVoting(true);
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You have " + Config.TIME_TO_VOTE + " seconds to vote on Topzone."));
			ThreadPool.schedule(new tzvotetask(player), Config.TIME_TO_VOTE * 880);
		}
		else
		{
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You can vote only once every 12 hours."));
		}
	}
	
	public static void updateLastTZVote(Player player)
	{
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET LastTZVote=? WHERE obj_Id=?");
				statement.setLong(1, System.currentTimeMillis());
				statement.setInt(2, player.getObjectId());
				statement.execute();
				statement.close();
				statement = null;
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Vote Manager: could not update LastTZVote in characters " + e);
			}
		}
	}
	
	public static int getHopZoneVotes()
	{
		
		int votes = -1;
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_HOPZONE_URL);
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
			e.printStackTrace();
			System.out.println("HOPZONE is offline. We will check reward as it will be online again.");
		}
		return votes;
	}
	
	public static void HZvote(final Player player)
	{
		long LastHZVote = 0L;
		long voteDelay = 43200000L;
		final int actualvotes;
		
		actualvotes = getHopZoneVotes();
		class hpvotetask implements Runnable
		{
			private final Player p;
			
			public hpvotetask(Player player)
			
			{
				p = player;
			}
			
			@Override
			public void run()
			{
				if (actualvotes < getHopZoneVotes())
				{
					p.setIsVoting(false);
					VoteHandler.updateLastHZVote(p);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "Thanks for Voting."));
					p.addItem("HZreward", Config.VOTE_REWARD_ID, Config.VOTE_REWARD_ID_COUNT, null, true);
				}
				else
				{
					p.setIsVoting(false);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You didn't vote. Try Again Later."));
				}
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT LastHZVote FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				LastHZVote = rset.getLong("LastHZVote");
			}
			statement.close();
			rset.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Vote Manager: could not select LastHZVote in characters " + e);
		}
		
		if ((LastHZVote + voteDelay) < System.currentTimeMillis())
		{
			for (Player actualchar : World.getInstance().getPlayers())
			{
				if (actualchar.isVoting())
				{
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", actualchar.getName() + ", is voting now. Please wait for a while."));
					return;
				}
			}
			player.setIsVoting(true);
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You have " + Config.TIME_TO_VOTE + " seconds to vote on Hopzone."));
			ThreadPool.schedule(new hpvotetask(player), Config.TIME_TO_VOTE * 880);
		}
		else
		{
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You can vote only once every 12 hours"));
		}
	}
	
	public static void updateLastHZVote(Player player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET LastHZVote=? WHERE obj_Id=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
			statement = null;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Vote Manager: could not update LastHZVote in characters " + e);
		}
	}
	
	public static int getL2NetworkVotes()
	{
		int votes = -1;
		try
		{
			final URL obj = new URL(Config.VOTES_SITE_L2NETWORK_URL);
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
			e.printStackTrace();
			System.out.println("NetWork is offline. We will check reward as it will be online again.");
		}
		return votes;
	}
	
	public static void NZvote(final Player player)
	{
		long LastNZVote = 0L;
		long voteDelay = 43200000L;
		final int actualvotes;
		
		actualvotes = getL2NetworkVotes();
		
		class nzvotetask implements Runnable
		{
			private final Player p;
			
			public nzvotetask(Player player)
			
			{
				p = player;
			}
			
			@Override
			public void run()
			{
				if (actualvotes < getL2NetworkVotes())
				{
					p.setIsVoting(false);
					VoteHandler.updateLastNZVote(p);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "Thanks for Voting."));
					p.addItem("NZreward", Config.VOTE_REWARD_ID, Config.VOTE_REWARD_ID_COUNT, null, true);
				}
				else
				{
					p.setIsVoting(false);
					p.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You didn't vote. Try Again Later."));
				}
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT LastNZVote FROM characters WHERE obj_Id=?");
			statement.setInt(1, player.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				LastNZVote = rset.getLong("LastNZVote");
			}
			statement.close();
			rset.close();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Vote Manager: could not select LastNZVote in characters " + e);
		}
		
		if ((LastNZVote + voteDelay) < System.currentTimeMillis())
		{
			for (Player actualchar : World.getInstance().getPlayers())
			{
				if (actualchar.isVoting())
				{
					player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", actualchar.getName() + ", is voting now. Please wait for a while."));
					return;
				}
			}
			player.setIsVoting(true);
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You have " + Config.TIME_TO_VOTE + " seconds to vote on Network."));
			ThreadPool.schedule(new nzvotetask(player), Config.TIME_TO_VOTE * 880);
		}
		else
		{
			player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Vote Manager", "You can vote only once every 12 hours."));
		}
	}
	
	public static void updateLastNZVote(Player player)
	{
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET LastNZVote=? WHERE obj_Id=?");
				statement.setLong(1, System.currentTimeMillis());
				statement.setInt(2, player.getObjectId());
				statement.execute();
				statement.close();
				statement = null;
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Vote Manager: could not update LastNZVote in characters " + e);
			}
		}
	}
}