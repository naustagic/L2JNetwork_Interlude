package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class L2PartyZone extends L2ZoneType
{
	public L2PartyZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature Creature)
	{
		if (Creature instanceof Player)
		{
			Player player = Creature.getActingPlayer();
			PvpFlagTaskManager.getInstance().remove(player);
			player.updatePvPFlag(1);
			((Player) Creature).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		}
		Creature.setInsideZone(ZoneId.PARTY, true);
	}
	
	@Override
	protected void onExit(Creature Creature)
	{
		if (Creature instanceof Player)
		{
			Player player = Creature.getActingPlayer();
			PvpFlagTaskManager.getInstance().add(player, Config.PVP_NORMAL_TIME);
			player.updatePvPFlag(1);
			((Player) Creature).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		}
		Creature.setInsideZone(ZoneId.PARTY, false);
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