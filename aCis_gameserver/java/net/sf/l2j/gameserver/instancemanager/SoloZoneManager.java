package net.sf.l2j.gameserver.instancemanager;

/**
 * @author George
 *
 */
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author George
 */
public class SoloZoneManager
{
	static Logger _log = Logger.getLogger(SoloZoneManager.class.getName());
	private boolean isZoneOpen = false;
	private static ArrayList<String> _rndNames = new ArrayList<>();
	private final static int RANDOM_NAMES = 500;
	private final static String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private int _playersInSoloZone = 0;
	private final ArrayList<Player[]> _kills = new ArrayList<>();
	
	private void addKill(Player[] kill)
	{
		_kills.add(kill);
		// Clear the kill.
		clearKill(kill);
	}
	
	private void clearKill(Player[] kill)
	{
		ThreadPool.schedule(() -> {
			_kills.remove(kill);
			
		}, Config.SOLO_ZONE_KILLS_DURATION * 1000);
	}
	
	public int getPlayersInside()
	{
		return _playersInSoloZone;
	}
	
	public void setPlayersInside(int val)
	{
		_playersInSoloZone = val;
	}
	
	/*
	 * Reward kill in solo zone.
	 */
	public void GiveSoloZoneReward(Player character, Creature killed)
	{
		// No reward for pets/summons.
		if (!(killed instanceof Player))
		{
			return;
		}
		Player killedPc = (Player) killed;
		// No reward for same IP.
		// TODO: HWID restriction.
		if (getIP(character).equals(getIP(killedPc)) && Config.SOLO_ZONE_RESTRICT_SAMEIP)
		{
			return;
		}
		// One of the players is not inside the solo zone.
		if (!character.isInSoloZone() || !killedPc.isInSoloZone())
		{
			return;
		}
		Player[] kill =
		{
			character,
			killedPc
		};
		// Restrict reward for recently killed players.
		/** @See Config.SOLO_ZONE_KILLS_DURATION */
		for (Player[] kills : _kills)
		{
			if (kills[0].equals(kill[0]) && kills[1].equals(kill[1]))
			{
				return;
			}
		}
		for (String element : Config.SOLO_ZONE_ITEMS_REWARD)
		{
			if ((element != null) && (element.split(",").length == 2))
			{
				int itemId = Integer.parseInt(element.split(",")[0]);
				int quantity = Integer.parseInt(element.split(",")[1]);
				character.addItem("solo_kill", itemId, quantity, character, true);
			}
		}
		// Save the kill
		if (Config.SOLO_ZONE_KILLS_DURATION > 0)
		{
			addKill(kill);
		}
	}
	
	private static String getIP(Player p)
	{
		return p.getClient().getConnection().getInetAddress().getHostAddress();
	}
	
	private SoloZoneManager()
	{
		_log.info("Solo Zone System: Loading...");
		// Solo zone opened at specific hours of the day.
		if (Config.SOLO_ZONE_SCHEDULED)
		{
			ThreadPool.schedule(new scheduleEventStart(), 10000L);
		}
		// Solo zone always opened.
		else
		{
			setIsZoneOpen(true);
			_log.info("Solo Zone System: Solo Zone permantly opened.");
		}
		for (int i = 0; i < RANDOM_NAMES; i++)
		{
			_rndNames.add(generateName());
		}
		_log.info("Solo Zone System: Loaded.");
	}
	
	public String getAName()
	{
		return _rndNames.get(Rnd.get(5, RANDOM_NAMES - 5));
	}
	
	private static String generateName()
	{
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(15);
		for (int i = 0; i < 15; i++)
		{
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}
	
	private class CloseSoloZone implements Runnable
	{
		public CloseSoloZone()
		{
			
		}
		
		@Override
		public void run()
		{
			try
			{
				setIsZoneOpen(false);
				Broadcast.announceToOnlinePlayers("PvP Solo Zone is now closed!", true);
				Collection<Player> pls = World.getInstance().getPlayers();
				for (Player onlinePlayer : pls)
				{
					if (onlinePlayer.isInSoloZone() && !(onlinePlayer.isGM()) && !(SoloZoneManager.getInstance().getIsZoneOpen()))
					{
						// Teleport all players from the solo zone to giran.
						onlinePlayer.teleToLocation(81220, 148588, -3472, 20); // giran
					}
				}
				// Calculate next opening
				ThreadPool.schedule(new scheduleEventStart(), 5000L);
			}
			catch (Exception e)
			{
				SoloZoneManager._log.info(getClass().getSimpleName() + "Error in running SoloZoneManager CloseSoloZone: " + e);
			}
		}
	}
	
	private class OpenSoloZone implements Runnable
	{
		long close_delay = 60000L * Config.SOLO_ZONE_DURATION;
		
		public OpenSoloZone()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				SoloZoneManager._log.info(getClass().getSimpleName() + ":Solo Zone is now Open and closes in " + Config.SOLO_ZONE_DURATION + " min(s)!");
				ThreadPool.schedule(new CloseSoloZone(), this.close_delay);
				// Announce Solo Zone
				Broadcast.announceToOnlinePlayers("PvP Solo Zone is now opened for " + Config.SOLO_ZONE_DURATION + " minutes. Fight and get reward per kill.", true);
				// Solo zone is now opened.
				setIsZoneOpen(true);
			}
			catch (Exception e)
			{
				_log.info(getClass().getSimpleName() + "Error in running SoloZoneManager OpenSoloZone: " + e);
			}
		}
	}
	
	private class scheduleEventStart implements Runnable
	{
		public scheduleEventStart()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				Calendar currentTime = Calendar.getInstance();
				Calendar nextStartTime = null;
				Calendar testStartTime = null;
				for (String timeOfDay : Config.SOLO_ZONE_HOURS)
				{
					testStartTime = Calendar.getInstance();
					testStartTime.setLenient(true);
					String[] splitTimeOfDay = timeOfDay.split(":");
					testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
					testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
					if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					{
						testStartTime.add(5, 1);
					}
					if ((nextStartTime == null) || (testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()))
					{
						nextStartTime = testStartTime;
					}
				}
				@SuppressWarnings("null")
				long the_delay = nextStartTime.getTimeInMillis() - System.currentTimeMillis();
				ThreadPool.schedule(new OpenSoloZone(), the_delay);
			}
			catch (Exception e)
			{
				SoloZoneManager._log.warning("SoloZoneManager.scheduleEventStart()]: Error figuring out a start time. Check OpeningHours in config file." + e);
			}
		}
		
	}
	
	public boolean getIsZoneOpen()
	{
		return this.isZoneOpen;
	}
	
	public void setIsZoneOpen(boolean val)
	{
		this.isZoneOpen = val;
	}
	
	public static SoloZoneManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		@SuppressWarnings("synthetic-access")
		protected static final SoloZoneManager _instance = new SoloZoneManager();
	}
}