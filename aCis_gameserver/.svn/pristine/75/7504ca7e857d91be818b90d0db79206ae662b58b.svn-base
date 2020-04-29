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
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSetAio;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;

/**
 * @author Reborn12
 */
public class AioItem implements IItemHandler
{
	private static final int[] ITEM_IDS = new int[]
	{
		Config.AIO_COIN_ID
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (Config.ENABLE_AIO_COIN)
		{
			if (!(playable instanceof Player))
				return;
			
			Player player = (Player) playable;
			if (player.isAio())
				player.sendMessage("You are Already An AIO");
			else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegisteredInComp(player))
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			else if (player.destroyItemByItemId("aio", Config.AIO_COIN_ID, 1, null, true))
				AdminSetAio.doAio(player, player, Config.AIO_COIN_DAYS);
		}
	}
	
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
