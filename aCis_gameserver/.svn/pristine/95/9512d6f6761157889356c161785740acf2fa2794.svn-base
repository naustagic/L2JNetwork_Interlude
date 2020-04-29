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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.SevenSignsFestival;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public class OfflineShop implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"offline"
	};
	
	@SuppressWarnings("null")
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (player == null)
			return false;
		
		// Message like L2OFF
		if ((!player.isInStoreMode() && (!player.isCrafting())) || !player.isSitting())
		{
			player.sendMessage("You are not running a private store or private work shop.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		TradeList storeListBuy = player.getBuyList();
		if (storeListBuy == null && storeListBuy.getItems().size() == 0)
		{
			player.sendMessage("Your buy list is empty.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		TradeList storeListSell = player.getSellList();
		if (storeListSell == null && storeListSell.getItems().size() == 0)
		{
			player.sendMessage("Your sell list is empty.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		player.getInventory().updateDatabase();
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player))
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Dont allow leaving if player is in combat
		if (player.isInCombat())
		{
			player.sendMessage("You cannot Logout while is in Combat mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Dont allow leaving if player is teleporting
		if (player.isTeleporting())
		{
			player.sendMessage("You cannot Logout while is Teleporting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can't Logout in Olympiad mode.");
			return false;
		}
		
		// Prevent player from logging out if they are a festival participant nd it is in progress,
		// otherwise notify party members that the player is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot Logout while you are a participant in a Festival.");
				return false;
			}
			
			Party playerParty = player.getParty();
			if (playerParty != null)
				player.getParty().broadcastToPartyMembers(player, SystemMessage.sendString(player.getName() + " has been removed from the upcoming Festival."));
		}
		
		if (player.isFlying())
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		
		if (!L2GameClient.offlineMode(player))
		{
			player.sendMessage("You cannot logout to offline player.");
			return false;
		}
		
		// Sleep effect, not official feature but however L2OFF features (like offline trade)
		if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE))
		{
			player.startAbnormalEffect(0x000080);
			player.sitDown();
			player.logout();
			return true;
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}