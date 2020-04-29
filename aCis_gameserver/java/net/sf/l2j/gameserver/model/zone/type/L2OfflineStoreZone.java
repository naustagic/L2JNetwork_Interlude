package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;

public class L2OfflineStoreZone extends L2ZoneType
{
	public L2OfflineStoreZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature Creature)
	{
		if (Creature instanceof Player)
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);

		Creature.setInsideZone(ZoneId.TOWN, true);
		Creature.setInsideZone(ZoneId.NO_STORE, false);
	}
	
	@Override
	protected void onExit(Creature Creature)
	{
		if (Creature instanceof Player)
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);

		Creature.setInsideZone(ZoneId.PARTY, false);
		Creature.setInsideZone(ZoneId.NO_STORE, true);
	}
	
	@Override
	public void onDieInside(Creature Creature)
	{
	}
	
	@Override
	public void onReviveInside(Creature Creature)
	{
	}
}