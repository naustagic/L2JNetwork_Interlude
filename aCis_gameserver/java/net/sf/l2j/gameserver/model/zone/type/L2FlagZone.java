package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.group.Party.MessageType;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class L2FlagZone extends L2SpawnZone
{
	L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
	
	public L2FlagZone(int id)
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
			player.setInFlagZone(true);
			((Player) Creature).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			// leave party
			if (player.getParty() != null)
				player.getParty().removePartyMember(player, MessageType.DISCONNECTED);
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		}
		Creature.setInsideZone(ZoneId.FLAG, true);
		Creature.setInsideZone(ZoneId.NO_STORE, false);
		noblesse.getEffects(Creature, Creature);
	}
	
	@Override
	protected void onExit(Creature Creature)
	{
		if (Creature instanceof Player)
		{
			Player player = Creature.getActingPlayer();
			PvpFlagTaskManager.getInstance().add(player, Config.PVP_NORMAL_TIME);
			player.updatePvPFlag(1);
			player.setInFlagZone(false);
			((Player) Creature).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			Creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		}
		Creature.setInsideZone(ZoneId.FLAG, false);
		Creature.setInsideZone(ZoneId.NO_STORE, true);
	}
	
	@Override
	public void onDieInside(Creature Creature)
	{
	}
	
	@Override
	public void onReviveInside(Creature Creature)
	{
		if (Creature instanceof Player)
		{
			final Player player = (Player) Creature;
			noblesse.getEffects(player, player);
			
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentMp(player.getMaxMp());
		}
	}
}