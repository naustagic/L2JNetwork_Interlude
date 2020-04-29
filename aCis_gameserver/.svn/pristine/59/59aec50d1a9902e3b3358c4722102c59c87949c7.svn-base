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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVipStatus;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

/**
 * @author Baggos
 */
public class VipStatusItem implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (Config.ENABLE_VIP_ITEM)
		{
			if (!(playable instanceof Player))
				return;
			
			Player activeChar = (Player) playable;
			if (activeChar.isVip())
			{
				activeChar.sendMessage("Your character has already VIP Status!");
				return;
			}
			if (activeChar.isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			AdminVipStatus.AddVipStatus(activeChar, activeChar, Config.VIP_DAYS);
			activeChar.broadcastPacket(new SocialAction(activeChar, 16));
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	public int getItemIds()
	{
		return Config.VIP_ITEM_ID;
	}
}