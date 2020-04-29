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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.taskmanager.AioTaskManager;

/**
 * @author Baggos
 */
public class AdminSetAio implements IAdminCommandHandler
{
	private static String[] _adminCommands = new String[]
	{
		"admin_setaio",
		"admin_removeaio"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int time = 1;
		Player target = null;
		if (st.hasMoreTokens())
		{
			player = st.nextToken();
			target = World.getInstance().getPlayer(player);
			if (st.hasMoreTokens())
			{
				try
				{
					time = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player)
			target = (Player) activeChar.getTarget();
		
		if (command.startsWith("admin_setaio"))
		{
			if (target == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //setaio <char_name> [duration_days]");
				return false;
			}
			if (target != null)
			{
				AdminSetAio.doAio(target, activeChar, time);
				activeChar.sendMessage(target.getName() + " got AIO status for " + time + " day(s).");
			}
		}
		else if (command.startsWith("admin_removeaio"))
		{
			if (target == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //removeaio <char_name>");
				return false;
			}
			if (target != null)
			{
				if (target.isAio())
				{
					AdminSetAio.removeAio(target, activeChar);
					activeChar.sendMessage("Removed the AIO status from " + target.getName() + ".");
				}
				activeChar.sendMessage(target.getName() + " haven't AIO status.");
			}
		}
		return true;
	}
	
	public static void doAio(Player target, Player player, int time)
	{
		target.getStat().addExp(target.getStat().getExpForLevel(81));
		target.broadcastPacket(new SocialAction(target, 3));
		target.setAio(true);
		
		AioTaskManager.getInstance().add(target);
		long remainingTime = target.getMemos().getLong("aioEndTime", 0);
		if (remainingTime > 0)
		{
			target.getMemos().set("aioEndTime", remainingTime + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "AIO Manager", "Dear " + player.getName() + ", your AIO status has been extended by " + time + " day(s)."));
		}
		else
		{
			target.getMemos().set("aioEndTime", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "AIO Manager", "Dear " + player.getName() + ", you got AIO Status for " + time + " day(s)."));
			
			for (L2Skill skill : target.getSkills().values())
				target.removeSkill(skill);
			
			if (Config.AIO_ITEM_ID != 0)
			{
				target.addItem("Add", Config.AIO_ITEM_ID, 1, target, true);
				target.getInventory().equipItemAndRecord(target.getInventory().getItemByItemId(Config.AIO_ITEM_ID));
			}
			target.addAioSkills();
			target.broadcastUserInfo();
		}
	}
	
	public static void removeAio(Player target, Player player)
	{
		AioTaskManager.getInstance().remove(target);
		target.getMemos().set("aioEndTime", 0);
		target.setAio(false);
		for (L2Skill skill : target.getSkills().values())
			target.removeSkill(skill);
		
		if (Config.AIO_ITEM_ID != 0)
			target.destroyItemByItemId("Destroy", Config.AIO_ITEM_ID, 1, target, true);
		
		target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "AIO Manager", "Dear " + player.getName() + ", Your AIO period is over. You will be disconected in 3 seconds."));
		target.setColor(0);
		target.broadcastPacket(new SocialAction(target, 13));
		target.sendSkillList();
		target.broadcastUserInfo();
		ThreadPool.schedule(() -> target.logout(false), 3000);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}