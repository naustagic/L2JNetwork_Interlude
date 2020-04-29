/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.VoteHandler;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Reborn12
 */

public class VoteNpc extends Folk
{
	public VoteNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("votetopzone"))
		{
			VoteHandler.tzvote(player);
		}
		else if (command.startsWith("votehopzone"))
		{
			VoteHandler.HZvote(player);
		}
		else if (command.startsWith("votenetwork"))
		{
			VoteHandler.NZvote(player);
		}
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		if (Config.VOTE_MANAGER_ENABLED)
			mainWindow(player);
		else
			ShowOfflineWindow(player);
	}
	
	public void ShowOfflineWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/voteManager/voteOffline.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%charname%", player.getName());
		player.sendPacket(html);
	}
	
	public void mainWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/voteManager/vote.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%charname%", player.getName());
		html.replace("%whoisvoting%", VoteHandler.whoIsVoting());
		player.sendPacket(html);
	}
	
}
