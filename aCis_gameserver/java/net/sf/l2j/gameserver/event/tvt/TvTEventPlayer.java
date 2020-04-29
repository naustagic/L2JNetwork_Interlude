package net.sf.l2j.gameserver.event.tvt;

import net.sf.l2j.gameserver.model.actor.instance.Player;

/**
 * @author Rootware
 * 
 */
public class TvTEventPlayer
{
	private final Player _player;
	
	protected TvTEventPlayer(Player player)
	{
		_player = player;
		
	}
	
	public boolean isOnEvent()
	{
		return TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(getPlayer().getObjectId());
	}
	
	public boolean isBlockingExit()
	{
		return true;
	}
	
	public boolean isBlockingDeathPenalty()
	{
		return true;
	}
	
	public boolean canRevive()
	{
		return false;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
}