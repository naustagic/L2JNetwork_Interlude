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

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Baggos
 */
public class ServerNewsBBSManager extends BaseBBSManager
{
	public static ServerNewsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbsShowServerNews"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/servnews.htm");
			activeChar.sendPacket(html);
		}
		else
			super.parseCmd(command, activeChar);
	}
	
	private static class SingletonHolder
	{
		protected static final ServerNewsBBSManager _instance = new ServerNewsBBSManager();
	}
}