package net.sf.l2j.gameserver.votesystem;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;

/**
 * @author Baggos
 */

public class OnlinePoint implements Runnable
{
	Logger _log = Logger.getLogger(OnlinePoint.class.getName());
	private static OnlinePoint _instance;
	static Map<String, Integer> playerIps = new HashMap<>();
	
	public static OnlinePoint getInstance()
	{
		if (_instance == null)
			_instance = new OnlinePoint();
		
		return _instance;
	}
	
	@Override
	public void run()
	{
		for (Player p : World.getInstance().getPlayers())
		{
			if (p.getClient() == null || p.getClient().isDetached()) // offline shops protection
				continue;
			
			boolean canReward = false;
			String pIp = p.getClient().getConnection().getInetAddress().getHostAddress();
			
			if (playerIps.containsKey(pIp))
			{
				int count = playerIps.get(pIp);
				if (count < 1)
				{
					playerIps.remove(pIp);
					playerIps.put(pIp, count + 1);
					canReward = true;
				}
			}
			else
			{
				canReward = true;
				playerIps.put(pIp, 1);
			}
			
			if (canReward)
				for (int i : Config.ONLINE_REWARD.keySet())
				{
					p.addItem("Vote reward.", i, Config.ONLINE_REWARD.get(i), p, true);
					p.sendMessage("As active player, you have been rewarded.");
				}
		}
		playerIps.clear();
	}
}