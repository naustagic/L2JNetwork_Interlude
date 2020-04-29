package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Updates and clears PvP flag of {@link Player} after specified time.
 * @author Tryskell, Hasha
 */
public final class ArenaTaskManager implements Runnable
{
	private final Map<Player, Long> _players = new ConcurrentHashMap<>();
	
	public static final ArenaTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ArenaTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	/**
	 * Adds {@link Player} to the PvpFlagTask.
	 * @param player : Player to be added and checked.
	 * @param time : Time in ms, after which the PvP flag is removed.
	 */
	public final void add(Player player, long time)
	{
		_players.put(player, System.currentTimeMillis() + time);
	}
	
	/**
	 * Removes {@link Player} from the PvpFlagTask.
	 * @param player : {@link Player} to be removed.
	 */
	public final void remove(Player player)
	{
		_players.remove(player);
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_players.isEmpty())
			return;
		
		// Get current time.
		final long currentTime = System.currentTimeMillis();
		
		// Loop all players.
		for (Map.Entry<Player, Long> entry : _players.entrySet())
		{
			// Get time left and check.
			final Player player = entry.getKey();
			final long timeLeft = entry.getValue();
			// Time is running out, clear PvP flag and remove from list.
			if (currentTime > timeLeft)
			{
				player.teleToLocation(112664, 14104, 10072, 20);
				_players.remove(player);
			}
			// Time almost runned out, update to blinking PvP flag.
			else if (currentTime >= (timeLeft - 10000) && (!(currentTime >= (timeLeft - 2000))))
				player.sendPacket(new ExShowScreenMessage("Arena: Time is running out.", 1000, 8, true));
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ArenaTaskManager _instance = new ArenaTaskManager();
	}
}