package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Rootware
 *
 */
public class TvTEventManagerNpc extends Npc
{
	public TvTEventManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null || !TvTEvent.isParticipating())
			return;
		
		if (command.equals("tvt_event_participation"))
		{
			if (player.isCursedWeaponEquipped())
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/cursed_weapon_restricted.htm");
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CURSED_WEAPON_RESTRICTED_FOR_PARTICIPATE));
			}
			else if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/olympiad_restricted.htm");
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_RESTRICTED_FOR_PARTICIPATE));
			}
			else if (player.getKarma() > 0)
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/karma_restricted.htm");
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.KARMA_RESTRICTED_FOR_PARTICIPATE));
			}
			else if (TvTEvent.getTeam(0).getParticipatedPlayerCount() >= Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM && TvTEvent.getTeam(1).getParticipatedPlayerCount() >= Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM)
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/players_restricted.htm");
					html.replace("%number%", Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM);
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_PARTICIPATE_EVENT_TEAM_IS_FULL_S1).addNumber(Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM));
			}
			else if (player.getLevel() < Config.TVT_EVENT_MIN_LVL || player.getLevel() > Config.TVT_EVENT_MAX_LVL)
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/level_restricted.htm");
					html.replace("%min%", Config.TVT_EVENT_MIN_LVL);
					html.replace("%max%", Config.TVT_EVENT_MAX_LVL);
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_PARTICIPATE_EVENT_TEAM_LEVEL_S1_TO_S2).addNumber(Config.TVT_EVENT_MIN_LVL).addNumber(Config.TVT_EVENT_MAX_LVL));
			}
			else if (TvTEvent.addParticipant(player))
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/register_done.htm");
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTICIPATE_IS_DONE));
			}
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			if (TvTEvent.removeParticipant(player.getObjectId()))
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/register_cancel.htm");
					player.sendPacket(html);
				}
				else
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTICIPATE_IS_CANCELLED));
			}
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		if (player == null)
			return;
		
		if (player.getClassId().getId() == 16 || player.getClassId().getId() == 97 || player.getClassId().getId() == 105 || player.getClassId().getId() == 30 || player.getClassId().getId() == 112 || player.getClassId().getId() == 43 || player.getClassId().getId() == 4 || player.getClassId().getId() == 5 || player.getClassId().getId() == 6 || player.getClassId().getId() == 90 || player.getClassId().getId() == 91 || player.getClassId().getId() == 19 || player.getClassId().getId() == 20 || player.getClassId().getId() == 99 || player.getClassId().getId() == 32 || player.getClassId().getId() == 33 || player.getClassId().getId() == 106)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body>Sorry! Your class cannot participate in TvT.</body>");
			player.sendPacket(html);
			return;
		}
		
		if (TvTEvent.isParticipating())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/mods/tvt/participation_" + (!TvTEvent.isPlayerParticipant(player.getObjectId()) ? "add" : "remove") + ".htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (TvTEvent.isStarting() || TvTEvent.isStarted())
		{
			final int[] teamPlayers = TvTEvent.getTeamsPlayerCounts();
			final int[] teamPoints = TvTEvent.getTeamsPoints();
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/mods/TvT/event_status.htm");
			html.replace("%team1name%", Config.TVT_EVENT_TEAM_BLUE_NAME);
			html.replace("%team1playercount%", String.valueOf(teamPlayers[0]));
			html.replace("%team1points%", String.valueOf(teamPoints[0]));
			html.replace("%team2name%", Config.TVT_EVENT_TEAM_RED_NAME);
			html.replace("%team2playercount%", String.valueOf(teamPlayers[1]));
			html.replace("%team2points%", String.valueOf(teamPoints[1]));
			player.sendPacket(html);
		}
		else
			_log.info("Someone tries to talk with TvT Event Manager NPC in innactive time.");
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}