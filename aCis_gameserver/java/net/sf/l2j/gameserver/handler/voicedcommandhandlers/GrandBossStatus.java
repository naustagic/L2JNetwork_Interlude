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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.Calendar;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Baggos
 */
public class GrandBossStatus implements IVoicedCommandHandler
{
	
	private static final String[] _voicedCommands =
	{
		"raidinfo"
	};
	
	private static final int[] RBOSSES =
	{
		25001,
		25325,
		25309,
		25299,
		25302,
		25450,
		25305,
		25269,
		51007,
		51008,
		51010
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equals("raidinfo") && Config.EPIC_INFO)
		{
			final StringBuilder sb = new StringBuilder();
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			sb.append("<html><head><title>Raid Boss Manager</title></head><body>");
			sb.append("<img src=\"networklogo1.networklogo1\" width=294 height=90>");
			sb.append("<br>");
			for (int rboss : RBOSSES)
			{
				String name = NpcTable.getInstance().getTemplate(rboss).getName();
				long delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss);
				
				if (delay <= System.currentTimeMillis())
				{
					sb.append("<table bgcolor=000000 width=320>");
					sb.append("<tr><td>");
					sb.append("<center><font color=\"01A9DB\">" + name + "</font>:&nbsp;<font color=\"4d94ff\">Is Alive!</font><br1></center>");
					sb.append("</tr></td>");
					sb.append("</table>");
					sb.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
				}
				else
				{
					delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss) - Calendar.getInstance().getTimeInMillis();
					sb.append("<table bgcolor=000000 width=320>");
					sb.append("<tr><td>");
					sb.append("<center><font color=\"01A9DB\">" + name + "</font>:&nbsp;<font color=\"4d94ff\">Is Dead!</font><br1></center><br1>");
					sb.append("<center>&nbsp;<font color=\"FFFF00\">Next respawn at&nbsp;" + ConverTime(delay) + "</font><br1></center>");
					sb.append("</td></tr>");
					sb.append("</table>");
					sb.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
				}
			}
			html.setHtml(sb.toString());
			html.replace("%bosslist%", sb.toString());
			activeChar.sendPacket(html);
		}
		return true;
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
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
