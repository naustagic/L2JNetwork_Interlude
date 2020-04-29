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
package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.concurrent.TimeUnit;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.taskmanager.HeroTaskManager;

/**
 * @author Baggos
 */
public class HeroItem implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player activeChar = (Player) playable;
		if (activeChar.isHero())
		{
			activeChar.sendMessage("Your character has already Hero Status!");
			return;
		}
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (activeChar.isInFunEvent())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		AddHeroStatus(activeChar, activeChar, Config.HERO_DAYS);
		activeChar.broadcastPacket(new SocialAction(activeChar, 16));
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	public static void AddHeroStatus(Player target, Player player, int time)
	{
		target.broadcastPacket(new SocialAction(target, 16));
		target.setHero(true);
		HeroTaskManager.getInstance().add(target);
		long remainingTime = target.getMemos().getLong("TimeOfHero", 0);
		if (remainingTime > 0)
		{
			target.getMemos().set("TimeOfHero", remainingTime + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "Hero Manager", "Dear " + player.getName() + ", your Hero status has been extended by " + time + " day(s)."));
		}
		else
		{
			target.getMemos().set("TimeOfHero", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "Hero Manager", "Dear " + player.getName() + ", you got Hero Status for " + time + " day(s)."));
			target.broadcastUserInfo();
		}
	}
	
	public static void RemoveHeroStatus(Player target)
	{
		HeroTaskManager.getInstance().remove(target);
		target.getMemos().set("TimeOfHero", 0);
		target.setHero(false);
		target.broadcastPacket(new SocialAction(target, 13));
		target.broadcastUserInfo();
	}
}