package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.event.TvTEventManager;
import net.sf.l2j.gameserver.event.enums.Team;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.event.tvt.TvTEventTeleporter;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;

/**
 * @author Rootware
 */
public class AdminTvTEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_tvt_add",
		"admin_tvt_remove",
		"admin_tvt_advance"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_tvt_add"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target == null || !(target instanceof Player))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			add(activeChar, (Player) target);
		}
		else if (command.equals("admin_tvt_remove"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target == null || !(target instanceof Player))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			remove(activeChar, (Player) target);
		}
		else if (command.equals("admin_tvt_advance"))
			TvTEventManager.getInstance().skipDelay();
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void add(Player activeChar, Player player)
	{
		if (TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}
		
		if (!TvTEvent.addParticipant(player))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}
		
		if (TvTEvent.isStarted())
			new TvTEventTeleporter(player, TvTEvent.getParticipantTeamLocation(player.getObjectId()), true);
	}
	
	private static void remove(Player activeChar, Player player)
	{
		if (!TvTEvent.removeParticipant(player.getObjectId()))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}
		
		new TvTEventTeleporter(player, (player.getTeam() == Team.BLUE ? Config.TVT_EVENT_TEAM_BLUE_BACK_LOCATION : Config.TVT_EVENT_TEAM_RED_BACK_LOCATION), true);
	}
}