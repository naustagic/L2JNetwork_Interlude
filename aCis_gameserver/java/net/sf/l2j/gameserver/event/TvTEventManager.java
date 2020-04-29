package net.sf.l2j.gameserver.event;

import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Rootware
 *
 */
public class TvTEventManager implements Runnable
{
	protected static final Logger _log = Logger.getLogger(TvTEventManager.class.getName());
	
	private EngineState _state;
	
	private int _tick;
	
	protected enum EngineState
	{
		AWAITING,
		REGISTRATION,
		PROCESSING,
		INACTIVE
	}
	
	protected TvTEventManager()
	{
		if (Config.TVT_EVENT_ENABLED)
		{
			TvTEvent.init();
			
			_state = EngineState.AWAITING;
			
			ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
			
			_log.info("TvTEventManager: Engine is enable.");
		}
		else
		{
			_state = EngineState.INACTIVE;
			
			_log.info("TvTEventManager: Engine is disabled.");
		}
	}
	
	@Override
	public void run()
	{
		if (_state == EngineState.AWAITING)
		{
			final Calendar calendar = Calendar.getInstance();
			final int hour = calendar.get(Calendar.HOUR_OF_DAY);
			final int minute = calendar.get(Calendar.MINUTE);
			
			for (String time : Config.TVT_EVENT_SCHEDULER_TIMES)
			{
				final String[] splitTime = time.split(":");
				if (Integer.parseInt(splitTime[0]) == hour && Integer.parseInt(splitTime[1]) == minute)
				{
					startRegistration();
					break;
				}
			}
		}
		else if (_state == EngineState.REGISTRATION)
		{
			switch (_tick)
			{
				case 7200:
				case 3600:
					Broadcast.announceToOnlinePlayers("[TVT] " + _tick / 3600 + " hour(s) until registration is closed!", true);
					break;
					
				case 1800:
				case 900:
				case 600:
					Broadcast.announceToOnlinePlayers("[TVT] Registration is now opened for 10 minute(s).", true);
					Broadcast.announcePm("TVT registration opened. Type .tvtjoin / .tvtleave");
					break;
				case 300:
				case 60:
					Broadcast.announceToOnlinePlayers("[TVT] " + _tick / 60 + " minute(s) until registration is closed!", true);
					break;
					
				case 30:
				case 10:
					Broadcast.announceToOnlinePlayers("[TVT] " + _tick + " second(s) until registration is closed!", true);
					break;
			}
			
			if (_tick == 0)
				startEvent();
			
			_tick--;
		}
		else if (_state == EngineState.PROCESSING)
		{
			switch (_tick)
			{
				case 7200:
				case 3600:
					TvTEvent.sysMsgToAllParticipants(_tick / 3600 + " hour(s) until event is finished!");
					break;
					
				case 1800:
				case 900:
				case 600:
				case 300:
				case 60:
					TvTEvent.sysMsgToAllParticipants(_tick / 60 + " minute(s) until the event is finished!");
					break;
					
				case 30:
				case 10:
					TvTEvent.sysMsgToAllParticipants(_tick + " second(s) until the event is finished!");
					break;
			}
			
			if (_tick == 0)
				endEvent();
			
			_tick--;
		}
	}
	
	public void startRegistration()
	{
		if (TvTEvent.startParticipation())
		{
			_state = EngineState.REGISTRATION;
			_tick = Config.TVT_EVENT_PARTICIPATION_TIME * 60;
		}
		else
			_state = EngineState.AWAITING;
	}
	
	public void startEvent()
	{
		if (TvTEvent.startFight())
		{
			Broadcast.announceToOnlinePlayers("[TVT] Registration closed! Next Event in 1 hour.", true);
			TvTEvent.sysMsgToAllParticipants("Teleporting participants to an arena in " + Config.TVT_EVENT_TELEPORT_DELAY + " second(s).");
			TvTEvent.sysAudioToAllParticipants("prepareforbattle");
			
			_state = EngineState.PROCESSING;
			_tick = Config.TVT_EVENT_RUNNING_TIME * 60;
		}
		else
		{
			Broadcast.announceToOnlinePlayers("[TVT] Event cancelled due to lack of Participation.", true);
			_state = EngineState.AWAITING;
		}
	}
	
	public void endEvent()
	{
		Broadcast.announceToOnlinePlayers(TvTEvent.calculateRewards(), true);
		TvTEvent.sysMsgToAllParticipants("You will be teleported back after " + Config.TVT_EVENT_TELEPORT_DELAY + " second(s).");
		TvTEvent.stopFight();
		_state = EngineState.AWAITING;
	}
	
	public void skipDelay()
	{
		_tick = 0;
	}
	
	public static TvTEventManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TvTEventManager INSTANCE = new TvTEventManager();
	}
}