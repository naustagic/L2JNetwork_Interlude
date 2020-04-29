package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isSubmitingPin())
		{
			activeChar.sendMessage("Unable to do any action while PIN is not submitted");
			return;
		}
		
		activeChar.sendPacket(ShowMiniMap.REGULAR_MAP);
	}
}