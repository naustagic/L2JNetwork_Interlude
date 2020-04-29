package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;

/**
 * @author Baggos
 */
public class L2StoreZone extends L2ZoneType
{
	public L2StoreZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.STORE, true);
			character.setInsideZone(ZoneId.NO_STORE, false);
			character.sendMessage("You entered a store zone.");
		}
	}
	
	@Override
	protected void onExit(final Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.STORE, false);
			character.setInsideZone(ZoneId.NO_STORE, true);
			character.sendMessage("You left a store zone.");
		}
	}
	
	@Override
	public void onDieInside(final Creature character)
	{
	}
	
	@Override
	public void onReviveInside(final Creature character)
	{
	}
}