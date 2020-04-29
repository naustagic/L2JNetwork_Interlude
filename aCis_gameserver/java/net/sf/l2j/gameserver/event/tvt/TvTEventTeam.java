package net.sf.l2j.gameserver.event.tvt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.location.Location;

/**
 * @author Rootware
 *
 */
public class TvTEventTeam
{
	private final String _name;
	private Location _location;
	private int _points;
	private Map<Integer, Player> _participatedPlayers = new ConcurrentHashMap<>();
	
	public TvTEventTeam(String name, Location location)
	{
		_name = name;
		_location = location;
		_points = 0;
	}
	
	public boolean addPlayer(Player player)
	{
		if (player == null)
			return false;
		
		_participatedPlayers.put(player.getObjectId(), player);
		
		return true;
	}
	
	public void removePlayer(int objectId)
	{
		_participatedPlayers.remove(objectId);
	}
	
	public void increasePoints()
	{
		_points += 1;
	}
	
	public void cleanMe()
	{
		_points = 0;
		_participatedPlayers.clear();
	}
	
	public boolean containsPlayer(int objectId)
	{
		return _participatedPlayers.containsKey(objectId);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Location getLocation()
	{
		return _location;
	}
	
	public int getPoints()
	{
		return _points;
	}
	
	public Map<Integer, Player> getParticipatedPlayers()
	{
		return _participatedPlayers;
	}
	
	public int getParticipatedPlayerCount()
	{
		return _participatedPlayers.size();
	}
}