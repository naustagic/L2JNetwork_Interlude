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
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.Calendar;

import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Baggos
 */
public class BossStatusBBSManager extends BaseBBSManager
{
	public static BossStatusBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final int[] RBOSSES =
	{
		25325,
		25276,
		25527,
		25245,
		25299,
		25248,
		25523,
		25450,
		25229,
		25249,
		25282,
		25319
	};
	private static int MBOSS = 25126;
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbsBoss"))
		{
			final StringBuilder sb = new StringBuilder();
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			for (int rboss : RBOSSES)
			{
				
				long delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss);
				String name = NpcTable.getInstance().getTemplate(rboss).getName().toUpperCase();
				sb.append("<html><head><title>RaidBoss Manager</title></head><body>");
				sb.append("<center>");
				sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1><br>");
				if (delay == 0)
				{
					sb.append("" + name + "&nbsp;<font color=\"FFFF00\">IS ALIVE!</font><br1>");
				}
				else if (delay < 0)
				{
					sb.append("&nbsp;" + name + "&nbsp;<font color=\"FF0000\">IS DEAD</font><br1>");
				}
				else
				{
					delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss) - Calendar.getInstance().getTimeInMillis();
					sb.append("" + name + "&nbsp;<br1>");
					sb.append("&nbsp;<font color=\"b09979\">Next respawn at&nbsp;" + ConverTime(delay) + "</font><br1>");
				}
			}
			
			long m_delay = RaidBossSpawnManager.getInstance().getRespawntime(MBOSS);
			String m_name = NpcTable.getInstance().getTemplate(MBOSS).getName().toUpperCase();
			
			String mainBossInfo = "";
			
			if (m_delay == 0)
			{
				mainBossInfo = "WE SHOULD HAVE ACTED<br1><font color=\"FFFF00\">" + m_name + "&nbsp;IS ALIVE!</font><br1>";
			}
			else if (m_delay < 0)
			{
				mainBossInfo = "IT'S ALL OVER<br1><font color=\"FF0000\">&nbsp;" + m_name + "&nbsp;IS DEAD</font><br1>";
			}
			else
			{
				m_delay = m_delay - Calendar.getInstance().getTimeInMillis();
				mainBossInfo = "<font color=\"b09979\">" + ConverTime(m_delay) + "</font><br1>UNTIL OBLIVION OPEN!";
			}
			
			html.setHtml(sb.toString());
			html.replace("%bosslist%", sb.toString());
			html.replace("%mboss%", mainBossInfo);
			activeChar.sendPacket(html);
		}
		else
			super.parseCmd(command, activeChar);
	}
	
	private static String ConverTime(long mseconds)
	{
		long remainder = mseconds;
		
		long hours = (long) Math.ceil((mseconds / (60 * 60 * 1000)));
		remainder = mseconds - (hours * 60 * 60 * 1000);
		
		long minutes = (long) Math.ceil((remainder / (60 * 1000)));
		remainder = remainder - (minutes * (60 * 1000));
		
		long seconds = (long) Math.ceil((remainder / 1000));
		
		return hours + " hours, " + minutes + " minutes, " + seconds + " seconds.";
	}
	
	private static class SingletonHolder
	{
		protected static final BossStatusBBSManager _instance = new BossStatusBBSManager();
	}
}