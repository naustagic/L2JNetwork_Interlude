package net.sf.l2j.gameserver.event.tvt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.colorsystem.ColorSystem;
import net.sf.l2j.gameserver.data.DoorTable;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.NpcTable;
import net.sf.l2j.gameserver.data.SpawnTable;
import net.sf.l2j.gameserver.event.enums.EventColor;
import net.sf.l2j.gameserver.event.enums.EventState;
import net.sf.l2j.gameserver.event.enums.Team;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Rootware
 */
public class TvTEvent
{
	protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());
	public static List<String> VotedPlayers = new ArrayList<>();
	
	private static TvTEventTeam[] _teams = new TvTEventTeam[2];
	
	private static EventState _state = EventState.INACTIVE;
	
	private static Npc _npc;
	
	private TvTEvent()
	{
		// Do nothing
	}
	
	public static void init()
	{
		_teams[0] = new TvTEventTeam(Config.TVT_EVENT_TEAM_BLUE_NAME, Config.TVT_EVENT_TEAM_BLUE_SPAWN_LOCATION);
		_teams[1] = new TvTEventTeam(Config.TVT_EVENT_TEAM_RED_NAME, Config.TVT_EVENT_TEAM_RED_SPAWN_LOCATION);
	}
	
	public static boolean startParticipation()
	{
		if (_npc == null)
		{
			try
			{
				final NpcTemplate template = NpcTable.getInstance().getTemplate(Config.TVT_EVENT_PARTICIPATION_NPC_ID);
				final Location loc = Config.TVT_EVENT_PARTICIPATION_NPC_LOCATION;
				final L2Spawn spawn = new L2Spawn(template);
				
				spawn.setLoc(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading());
				spawn.setRespawnDelay(60000);
				spawn.setRespawnState(false);
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				
				_npc = spawn.doSpawn(false);
				_npc.setCurrentHp(_npc.getMaxHp());
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "TvTEvent: startParticipation() exception: " + e.getMessage(), e);
				return false;
			}
		}
		
		_npc.spawnMe();
		
		setState(EventState.PARTICIPATING);
		
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, Player> players)
	{
		int maxLevel = Integer.MIN_VALUE;
		int maxLevelId = -1;
		for (Player player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		
		return maxLevelId;
	}
	
	public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Randomize and balance team distribution
		final Map<Integer, Player> allParticipants = new HashMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		
		if (needParticipationFee())
		{
			final Iterator<Player> iterator = allParticipants.values().iterator();
			while (iterator.hasNext())
			{
				final Player player = iterator.next();
				if (!hasParticipationFee(player))
					iterator.remove();
			}
		}
		
		int balance[] =
		{
			0,
			0
		}, priority = 0, highestLevelPlayerId;
		
		Player highestLevelPlayer;
		while (!allParticipants.isEmpty())
		{
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			
			_teams[priority].addPlayer(highestLevelPlayer);
			
			balance[priority] += highestLevelPlayer.getLevel();
			
			// Exiting if no more players
			if (allParticipants.isEmpty())
				break;
			
			// The other team gets one player
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			
			_teams[priority].addPlayer(highestLevelPlayer);
			
			balance[priority] += highestLevelPlayer.getLevel();
			
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		// not enought participants
		if (_teams[0].getParticipatedPlayerCount() < Config.TVT_EVENT_MINIMAL_PLAYERS_IN_TEAM || _teams[1].getParticipatedPlayerCount() < Config.TVT_EVENT_MINIMAL_PLAYERS_IN_TEAM)
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			
			// Unspawn the event NPC
			unSpawnNpc();
			
			return false;
		}
		
		if (needParticipationFee())
		{
			Iterator<Player> iterator = _teams[0].getParticipatedPlayers().values().iterator();
			while (iterator.hasNext())
			{
				final Player player = iterator.next();
				if (!payParticipationFee(player))
					iterator.remove();
			}
			
			iterator = _teams[1].getParticipatedPlayers().values().iterator();
			while (iterator.hasNext())
			{
				final Player player = iterator.next();
				if (!payParticipationFee(player))
					iterator.remove();
			}
		}
		
		// Closes all doors specified in configs for tvt
		closeDoors();
		
		// Set state STARTED
		setState(EventState.STARTED);
		ClearVotedPlayers();
		
		TvTEvent.prepareNameAndTitle();
		
		// Iterate over all teams
		for (TvTEventTeam team : _teams)
		{
			// Iterate over all participated player instances in this team
			for (Player player : team.getParticipatedPlayers().values())
			{
				if (player != null)
				{
					// Leave party
					if (player.isInParty())
						player.getParty().disband();
					
					// Teleporter implements Runnable and starts itself
					new TvTEventTeleporter(player, team.getLocation(), false);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br>
	 * <br>
	 * @return String<br>
	 */
	public static String calculateRewards()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
			return "[Team vs Team] The event has ended with both teams tied.";
		
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		// Get team which has more points
		TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		rewardTeam(team);
		
		return "[Team vs Team] Event finish. Team " + team.getName() + " won with " + team.getPoints() + " kills.";
	}
	
	private static void rewardTeam(TvTEventTeam team)
	{
		for (Player player : team.getParticipatedPlayers().values())
		{
			if (player == null)
				continue;
			
			if (player.getEventScore() < Config.TVT_EVENT_KILLS_REQUIRED)
			{
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Your need " + Config.TVT_EVENT_KILLS_REQUIRED + " kills or more for reward.</body></html>");
				player.sendPacket(npcHtmlMessage);
				continue;
			}
			
			// Iterate over all tvt event rewards
			for (int[] reward : Config.TVT_EVENT_REWARDS)
			{
				PcInventory inv = player.getInventory();
				
				// Check for stackable item, non stackabe items need to be added one by one
				if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
					inv.addItem("TvT Event", reward[0], reward[1], player, player);
				else
				{
					for (int i = 0; i < reward[1]; i++)
						inv.addItem("TvT Event", reward[0], 1, player, player);
				}
				
				if (reward[1] > 1)
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(reward[0]).addNumber(reward[1]));
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(reward[0]));
			}
			
			StatusUpdate statusUpdate = new StatusUpdate(player);
			
			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
			player.sendPacket(statusUpdate);
			
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Your team won the event. Look in your inventory, there should be your reward.</body></html>");
			player.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE<br>
	 */
	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		
		// Unspawn event npc
		unSpawnNpc();
		
		// Opens all doors specified in configs for tvt
		openDoors();
		
		for (TvTEventTeam team : _teams)
		{
			for (Player player : team.getParticipatedPlayers().values())
			{
				// Check for nullpointer
				if (player != null)
				{
					player.getAppearance().setVisibleTitle(null);
					player.getAppearance().setTitleColor(0);
                    player.getAppearance().setNameColor(0);
					ColorSystem pvpcolor = new ColorSystem();
					pvpcolor.updateNameColor(player);
					pvpcolor.updateTitleColor(player);
					player.clearEventScore();
					player.sitDown();
					
					// Teleport back.
					new TvTEventTeleporter(player, (player.getTeam() == Team.BLUE ? Config.TVT_EVENT_TEAM_BLUE_BACK_LOCATION : Config.TVT_EVENT_TEAM_RED_BACK_LOCATION), false);
				}
			}
		}
		
		// Cleanup of teams
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		ClearVotedPlayers();
		
		// Set state INACTIVE
		setState(EventState.INACTIVE);
	}
	
	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team
	 * @param player
	 * @return boolean
	 */
	public static synchronized boolean addParticipant(Player player)
	{
		if (player == null || TvTEvent.isPlayerParticipant(player.getObjectId()))
			return false;
		
		if (HasVoted(player))
		{
			player.sendMessage("You have already joined the event with another character.");
			return false;
		}
		
		AddVotedPlayer(player);
		
		byte teamId = 0;
		
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
			teamId = (byte) (Rnd.get(2));
		else
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		
		return _teams[teamId].addPlayer(player);
	}
	
	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team
	 * @param playerObjectId
	 * @return boolean
	 */
	public static boolean removeParticipant(int playerObjectId)
	{
		final byte teamId = getParticipantTeamId(playerObjectId);
		if (teamId == -1)
			return false;
		
		_teams[teamId].removePlayer(playerObjectId);
		ClearVotedPlayers();
		
		return true;
	}
	
	public static void AddVotedPlayer(Player player)
	{
		if (player != null && !player.isGM())
			VotedPlayers.add(player.getClient().getConnection().getInetAddress().getHostAddress());
	}
	
	public static boolean HasVoted(Player player)
	{
		if (player != null && VotedPlayers.contains(player.getClient().getConnection().getInetAddress().getHostAddress()))
			return true;
		
		return false;
	}
	
	public static void ClearVotedPlayers()
	{
		if (VotedPlayers.size() > 0)
			VotedPlayers.clear();
	}
	
	public static boolean needParticipationFee()
	{
		return (Config.TVT_EVENT_PARTICIPATION_FEE[0][0] != 0) && (Config.TVT_EVENT_PARTICIPATION_FEE[0][1] != 0);
	}
	
	public static boolean hasParticipationFee(Player playerInstance)
	{
		return playerInstance.getInventory().getInventoryItemCount(Config.TVT_EVENT_PARTICIPATION_FEE[0][0], -1) >= Config.TVT_EVENT_PARTICIPATION_FEE[0][1];
	}
	
	public static boolean payParticipationFee(Player playerInstance)
	{
		return playerInstance.destroyItemByItemId("TvT Participation Fee", Config.TVT_EVENT_PARTICIPATION_FEE[0][0], Config.TVT_EVENT_PARTICIPATION_FEE[0][1], _npc, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = Config.TVT_EVENT_PARTICIPATION_FEE[0][0];
		int itemNum = Config.TVT_EVENT_PARTICIPATION_FEE[0][1];
		
		if (itemId == 0 || itemNum == 0)
			return "-";
		
		return new String(itemNum + " " + ItemTable.getInstance().getTemplate(itemId).getName());
	}
	
	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two
	 * @param message
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		for (Player playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "[Team vs Team]", message));
		}
		
		for (Player playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "[Team vs Team]", message));
		}
	}
	
	/**
	 * Send a Audio to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two
	 * @param audio
	 */
	public static void sysAudioToAllParticipants(String audio)
	{
		for (Player playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendPacket(new PlaySound(1, audio));
		}
		
		for (Player playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendPacket(new PlaySound(1, audio));
		}
	}
	
	public static void prepareNameAndTitle()
	{
		for (Player player : _teams[0].getParticipatedPlayers().values())
		{
			if (player != null)
			{
				player.getAppearance().setVisibleTitle(" ");
				player.getAppearance().setTitleColor(EventColor.WHITE.getColor());
				player.getAppearance().setNameColor(EventColor.BLUE.getColor());
			}
		}
		
		for (Player player : _teams[1].getParticipatedPlayers().values())
		{
			if (player != null)
			{
				player.getAppearance().setVisibleTitle(" ");
				player.getAppearance().setTitleColor(EventColor.WHITE.getColor());
				player.getAppearance().setNameColor(EventColor.RED.getColor());
			}
		}
	}
	
	/**
	 * Close doors specified in configs
	 */
	public static void closeDoors()
	{
		for (int doorId : Config.TVT_EVENT_DOOR_IDS)
		{
			final Door door = DoorTable.getInstance().getDoor(doorId);
			if (door != null)
				door.closeMe();
		}
	}
	
	/**
	 * Open doors specified in configs
	 */
	public static void openDoors()
	{
		for (int doorId : Config.TVT_EVENT_DOOR_IDS)
		{
			final Door door = DoorTable.getInstance().getDoor(doorId);
			if (door != null)
				door.openMe();
		}
	}
	
	/**
	 * UnSpawns the TvTEvent npc
	 */
	private static void unSpawnNpc()
	{
		_npc.decayMe();
	}
	
	/**
	 * Called when a player logs in
	 * @param player
	 */
	public static void onLogin(Player player)
	{
		if (player == null || (!isStarting() && !isStarted()))
			return;
		
		final byte teamId = getParticipantTeamId(player.getObjectId());
		if (teamId == -1)
			return;
		
		_teams[teamId].addPlayer(player);
		
		new TvTEventTeleporter(player, _teams[teamId].getLocation(), true);
	}
	
	/**
	 * Called when a player logs out
	 * @param player
	 */
	public static void onLogout(Player player)
	{
		if (player == null || (!isStarting() && !isStarted()))
			return;
		
		if (removeParticipant(player.getObjectId()))
		{
			final Location loc = player.getTeam() == Team.BLUE ? Config.TVT_EVENT_TEAM_BLUE_BACK_LOCATION : Config.TVT_EVENT_TEAM_RED_BACK_LOCATION;
			player.setXYZInvisible((loc.getX() + Rnd.get(101)) - 50, (loc.getY() + Rnd.get(101)) - 50, loc.getZ());
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance
	 * @param player
	 * @param targetedPlayerObjectId
	 * @return boolean
	 */
	public static boolean onAction(Player player, int targetedPlayerObjectId)
	{
		if (player == null || !isStarted())
			return true;
		
		if (player.isGM())
			return true;
		
		final byte playerTeamId = getParticipantTeamId(player.getObjectId());
		final byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if ((playerTeamId != -1 && targetedPlayerTeamId == -1) || (playerTeamId == -1 && targetedPlayerTeamId != -1))
			return false;
		
		if (playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && player.getObjectId() != targetedPlayerObjectId && !Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every summon item use
	 * @param playerObjectId
	 * @return boolean
	 */
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every scroll use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use scroll, otherwise false
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_SCROLL_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every potion use
	 * @param playerObjectId
	 * @return boolean: true if player is allowed to use potions, otherwise false
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_POTIONS_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Is called when a player is killed
	 * @param killerCreature
	 * @param victim
	 */
	public static void onKill(Creature killerCreature, Player victim)
	{
		if (killerCreature == null || victim == null || (!(killerCreature instanceof Player) && !(killerCreature instanceof Pet) && !(killerCreature instanceof Servitor)) || !isStarted())
			return;
		
		final Player killer = (killerCreature instanceof Pet || killerCreature instanceof Servitor) ? ((Summon) killerCreature).getOwner() : (Player) killerCreature;
		
		final byte killerTeamId = getParticipantTeamId(killer.getObjectId());
		final byte killedTeamId = getParticipantTeamId(victim.getObjectId());
		
		// Legal enemy kill
		if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
		{
			// Count the private kill
			killer.increaseEventScore();
			killer.getAppearance().setVisibleTitle("Kills: " + killer.getEventScore());
			killer.broadcastTitleInfo();
			
			// Kill reward
			if (Config.TVT_KILLS_REWARD_ENABLED)
			{
				for (int[] rewardKills : Config.TVT_KILLS_REWARD)
				{
					switch (killer.getEventScore())
					{
						case 2: // Reward after 2 kills
						case 4: // Reward after 4 kills
						case 6: // Reward after 4 kills
						case 8: // Reward after 4 kills
						case 10: // Reward after 4 kills
						case 12: // Reward after 4 kills
						case 14: // Reward after 4 kills
						case 15: // Reward after 4 kills
						case 16: // Reward after 4 kills
						case 17: // Reward after 4 kills
						case 18: // Reward after 4 kills
						case 19: // Reward after 4 kills
						case 20: // Reward after 4 kills
						{
							killer.getInventory().addItem("TvT Event", rewardKills[0], rewardKills[1], killer, killer);
							killer.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "TVT", +killer.getEventScore() + " kills. You has been rewarded!"));
							killer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(rewardKills[0]).addNumber(rewardKills[1]));
							sysMsgToAllParticipants(killer.getName() + " get reward for " + killer.getEventScore() + " kills");
							break;
						}
					}
				}
			}
			
			// Count the team kill
			_teams[killerTeamId].increasePoints();
		}
		// If kill the same team remove and teleport.
		else if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId == killedTeamId)
		{
			sysMsgToAllParticipants(killer.getName() + " removed from event by kill the same team.");
			
			if (killerTeamId != -1)
			{
				final Location coord = killer.getTeam() == Team.BLUE ? Config.TVT_EVENT_TEAM_BLUE_BACK_LOCATION : Config.TVT_EVENT_TEAM_RED_BACK_LOCATION;
				killer.getAppearance().setVisibleTitle(null);
				killer.getAppearance().setTitleColor(0);
				killer.getAppearance().setNameColor(0);
				ColorSystem pvpcolor = new ColorSystem();
				pvpcolor.updateNameColor(killer);
				pvpcolor.updateTitleColor(killer);
				killer.clearEventScore();
				killer.setTeam(Team.NONE);
				
				_teams[killerTeamId].removePlayer(killer.getObjectId());
				
				new TvTEventTeleporter(killer, coord, false);
			}
		}
		
		if (killedTeamId != -1)
			new TvTEventTeleporter(victim, _teams[killedTeamId].getLocation(), false);
	}
	
	/**
	 * Sets the TvT Event state
	 * @param state
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	/**
	 * Is TvT Event inactive?
	 * @return boolean
	 */
	public static boolean isInactive()
	{
		synchronized (_state)
		{
			return _state == EventState.INACTIVE;
		}
	}
	
	/**
	 * Is TvT Event in inactivating?
	 * @return boolean
	 */
	public static boolean isInactivating()
	{
		synchronized (_state)
		{
			return _state == EventState.INACTIVATING;
		}
	}
	
	/**
	 * Is TvTEvent in participation?
	 * @return boolean
	 */
	public static boolean isParticipating()
	{
		synchronized (_state)
		{
			return _state == EventState.PARTICIPATING;
		}
	}
	
	/**
	 * Is TvT Event starting?
	 * @return boolean
	 */
	public static boolean isStarting()
	{
		synchronized (_state)
		{
			return _state == EventState.STARTING;
		}
	}
	
	/**
	 * Is TvT Event started?
	 * @return boolean
	 */
	public static boolean isStarted()
	{
		synchronized (_state)
		{
			return _state == EventState.STARTED;
		}
	}
	
	/**
	 * Is TvT Event rewarding?
	 * @return boolean
	 */
	public static boolean isRewarding()
	{
		synchronized (_state)
		{
			return _state == EventState.REWARDING;
		}
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1
	 * @param playerObjectId
	 * @return byte
	 */
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null
	 * @param playerObjectId
	 * @return int[]
	 */
	public static Location getParticipantTeamLocation(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getLocation() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getLocation() : null);
	}
	
	/**
	 * Is given player participant of the event?
	 * @param playerObjectId
	 * @return boolean
	 */
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return false;
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	/**
	 * Returns participated player count<br>
	 * <br>
	 * @return int
	 */
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
			return 0;
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names<br>
	 * <br>
	 * @return String[]<br>
	 */
	public static String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	/**
	 * Returns player count of both teams<br>
	 * <br>
	 * @return int[]<br>
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(),
			_teams[1].getParticipatedPlayerCount()
		};
	}
	
	/**
	 * Returns points count of both teams
	 * @return int[]
	 */
	public static int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(),
			_teams[1].getPoints()
		};
	}
	
	public static TvTEventTeam getTeam(int id)
	{
		return _teams[id];
	}
}