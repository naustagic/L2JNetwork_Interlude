/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.votesystem;

import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Melron
 */
public class VoteReminder
{
	static final Logger _log = Logger.getLogger(VoteReminder.class.getName());
	
	protected void StartReminder()
	{
		
		for (Player player : World.getInstance().getPlayers())
			if (player != null && (player.canVoteHopZone() || player.canVoteNetwork() || player.canVoteTopZone()))
			{
				
				boolean hopzone = false;
				boolean topzone = false;
				boolean network = false;
				
				if (player.canVoteHopZone())
					hopzone = true;
				if (player.canVoteNetwork())
					network = true;
				if (player.canVoteTopZone())
					topzone = true;
				
				if (!hopzone && !topzone && !network)
					return;
				
				String SitesForVote = "";
				if (hopzone && topzone && network)
					SitesForVote += "HopZone, TopZone and L2Network!";
				
				else if (hopzone && topzone && !network)
					SitesForVote += "HopZone and TopZone!";
				else if (hopzone && network && !topzone)
					SitesForVote += "HopZone and L2Network!";
				else if (topzone && network && !hopzone)
					SitesForVote += "TopZone and L2Network!";
				
				// single cases
				else if (network && !hopzone && !topzone)
					SitesForVote += "L2Network!";
				else if (hopzone && !topzone && !network)
					SitesForVote += "HopZone!";
				else
					SitesForVote += "TopZone!";
				
				if (!player.isVoting())
					player.sendPacket(new CreatureSay(0, Say2.TELL, " Vote Reminder: You can vote in", SitesForVote));
			}
	}
	
	protected VoteReminder()
	{
		ThreadPool.schedule(new VoteReminderC(), Config.VOTE_REMINDER_MINUTES * 60000);
	}
	
	public static VoteReminder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final VoteReminder _instance = new VoteReminder();
	}
	
	protected class VoteReminderC implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.ALLOW_VOTE_REMINDER)
				StartReminder();
		}
	}
}
