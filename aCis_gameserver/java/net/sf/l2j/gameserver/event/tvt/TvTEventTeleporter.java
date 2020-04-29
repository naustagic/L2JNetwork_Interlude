package net.sf.l2j.gameserver.event.tvt;

import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.event.enums.Team;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.location.Location;

/**
 * @author Rootware
 * 
 */
public class TvTEventTeleporter implements Runnable
{
	protected static final Logger _log = Logger.getLogger(TvTEventTeleporter.class.getName());
	
	private Player _player;
	private Location _location;
	L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
	
	public TvTEventTeleporter(Player player, Location location, boolean fastSchedule)
	{
		_player = player;
		_location = location;
		
		ThreadPool.schedule(this, fastSchedule ? 0 : (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_DELAY : Config.TVT_EVENT_TELEPORT_DELAY) * 1000);
	}
	
	@Override
	public void run()
	{
		if (_player == null)
			return;
		
		final Summon summon = _player.getPet();
		if (summon != null)
			summon.unSummon(_player);
		
		if (_player.isInDuel())
			_player.setDuelState(DuelState.INTERRUPTED);
		
		if (Config.TVT_EVENT_REMOVE_BUFFS)
		{
			for (L2Effect effect : _player.getAllEffects())
				effect.exit();
		}
		
		if (_player.isDead())
			_player.doRevive();
		
		_player.teleToLocation(_location, 20);
		
		if (TvTEvent.isStarted())
		{
			final int teamId = TvTEvent.getParticipantTeamId(_player.getObjectId()) + 1;
			switch (teamId)
			{
				case 1:
					_player.setTeam(Team.BLUE);
					break;
				case 2:
					_player.setTeam(Team.RED);
					break;
					
				default:
					_log.info("TvTEventTeleporter: Unknown team ID: " + teamId);
					break;
			}
		}
		else
			_player.setTeam(Team.NONE);
		
		_player.setCurrentCp(_player.getMaxCp());
		_player.setCurrentHp(_player.getMaxHp());
		_player.setCurrentMp(_player.getMaxMp());
		noblesse.getEffects(_player, _player);
		
		_player.broadcastStatusUpdate();
		_player.broadcastUserInfo();
	}
}