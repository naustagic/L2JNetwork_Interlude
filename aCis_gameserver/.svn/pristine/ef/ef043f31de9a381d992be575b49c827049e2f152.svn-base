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
package net.sf.l2j.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class RaidBossInfo extends Npc
{
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
	
	public RaidBossInfo(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		generateFirstWindow(player);
	}
	
	public void generateFirstWindow(Player activeChar)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int rboss : RBOSSES)
		{
			
			String name = NpcTable.getInstance().getTemplate(rboss).getName();
			long delay = RaidBossSpawnManager.getInstance().getRespawntime(rboss);
			
			if (delay <= System.currentTimeMillis())
			{
				sb.append("" + name + "&nbsp;<font color=\"4d94ff\">IS ALIVE!</font><br1>");
			}
			else
			{
				sb.append("" + name + "&nbsp;<br1>");
				sb.append("&nbsp;<font color=\"FFFFFF\">" + " " + "Respawn at:</font>" + "" + "<font color=\"FF9900\"> " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(delay) + "</font><br>");
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(getHtmlPath(getNpcId(), 0));
		html.replace("%objectId%", getObjectId());
		html.replace("%bosslist%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename;
		
		if (val == 0)
			filename = "data/html/mods/RaidBossStatus/" + npcId + ".htm";
		else
			filename = "data/html/mods/RaidBossStatus/" + npcId + "-" + val + ".htm";
		
		if (HtmCache.getInstance().isLoadable(filename))
			return filename;
		
		return "data/html/mods/RaidBossStatus/" + npcId + ".htm";
	}
}