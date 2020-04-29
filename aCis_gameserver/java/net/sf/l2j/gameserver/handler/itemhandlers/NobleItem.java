package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

/**
 * @author Baggos
 */
public class NobleItem implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player activeChar = (Player) playable;
		if (activeChar.isNoble())
		{
			activeChar.sendMessage("You Are Already A Noblesse!");
			return;
		}
		
		activeChar.broadcastPacket(new SocialAction(activeChar, 16));
		activeChar.setNoble(true, true);
		activeChar.sendMessage("You Are Now a Noble! Check your skills.");
		activeChar.broadcastUserInfo();
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
}