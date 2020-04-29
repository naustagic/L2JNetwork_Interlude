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
package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;

public class BarakielNobless extends Quest
{
	public BarakielNobless()
	{
		super(-1, "custom");
		
		addKillId(25325);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		if (player.getParty() != null)
		{
			for (Player members : player.getParty().getMembers())
			{
				if (!members.isInsideRadius(npc, 2000, false, false))
				{
					members.sendMessage("Barakiel: Oh.. You were too far away from your party. You missed the chance!");
					continue;
				}
				if (!members.isNoble())
				{
					members.setNoble(true, true);
					members.broadcastPacket(new SocialAction(player, 16));
					members.getInventory().addItem("Noblesse Tiara", 7694, 1, members, null);
					members.sendMessage("Barakiel: Done! I bless you with Noblesse status.");
				}
				members.sendMessage("Barakiel: You are already Noblesse!");
				members.broadcastUserInfo();
			}
		}
		return null;
	}
	
	public static void main(String args[])
	{
		new BarakielNobless();
	}
}