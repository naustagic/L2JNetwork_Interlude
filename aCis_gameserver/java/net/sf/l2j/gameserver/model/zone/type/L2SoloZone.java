package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.instancemanager.SoloZoneManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.group.Party.MessageType;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;

/**
 * @author Baggos
 */
public class L2SoloZone extends L2SpawnZone
{
	
	public L2SoloZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		// Solo zone
		character.setInsideZone(ZoneId.SOLO, true);
		// Restrict making a store
		character.setInsideZone(ZoneId.NO_STORE, true);
		// Restrict summon
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		if (character instanceof Player)
		{
			Player player = (Player) character;
			
			if (isRestrictedClass(player.getActiveClass()) && !player.isGM())
			{
				player.sendMessage("You cannot enter in pvp solo zone with a support class.");
				OustPlayer(player);
				return;
			}
			if (!SoloZoneManager.getInstance().getIsZoneOpen())
			{
				if (!player.isGM())
				{
					player.sendMessage("The PvP Solo Zone is currently closed!");
					OustPlayer(player);
					return;
				}
			}
			// Player is in solo zone.
			if (player.getParty() != null)
				player.getParty().removePartyMember(player, MessageType.DISCONNECTED);
			
			player.setInSoloZone(true);
			player.updatePvPFlag(1);
			BuffPlayer(character);
		}
		
	}
	
	private static void OustPlayer(Player player)
	{
		player.teleToLocation(Integer.parseInt(Config.SOLO_ZONE_EXIT_LOCATION[0].trim()), Integer.parseInt(Config.SOLO_ZONE_EXIT_LOCATION[1].trim()), Integer.parseInt(Config.SOLO_ZONE_EXIT_LOCATION[2].trim()), 1);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		// Solo zone
		character.setInsideZone(ZoneId.SOLO, false);
		// Allow making a store
		character.setInsideZone(ZoneId.NO_STORE, true);
		// Allow summon
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		if (character instanceof Player)
		{
			Player player = (Player) character;
			player.setInSoloZone(false);
			player.updatePvPFlag(0);
			SoloZoneManager.getInstance().setPlayersInside(SoloZoneManager.getInstance().getPlayersInside() - 1);
		}
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
		if (character instanceof Player)
		{
			// Reset the flag, just to make sure player stays flagged.
			((Player) character).updatePvPFlag(1);
		}
		BuffPlayer(character);
	}
	
	private static void BuffPlayer(Creature character)
	{
		if (!(character instanceof Player))
		{
			return;
		}
		if (Config.SOLO_ZONE_NOBLESS)
		{
			// Give Nobless (1323 ID)
			L2Skill no = SkillTable.getInstance().getInfo(1323, 1);
			no.getEffects(character, character);
		}
		if (Config.SOLO_ZONE_CLEANSE)
		{
			L2Skill no = SkillTable.getInstance().getInfo(1409, 1);
			no.getEffects(character, character);
			
		}
		if (Config.SOLO_ZONE_HEAL)
		{
			character.setCurrentCp(character.getMaxCp());
			character.setCurrentHp(character.getMaxHp());
			character.setCurrentMp(character.getMaxMp());
		}
	}
	
	/*
	 * Checks if player active class is restricted from solo zone.
	 */
	private static boolean isRestrictedClass(int classId)
	{
		for (String s : Config.SOLO_ZONE_RESTRICTED_CLASSES)
		{
			int Id = Integer.parseInt(s);
			if (Id == classId)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDieInside(Creature character)
	{
		
	}
	
}