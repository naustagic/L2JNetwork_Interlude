package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class TvTEventCommand implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"tvtjoin",
		"tvtleave"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar.isInsideZone(ZoneId.FLAG))
			return false;
		else if (activeChar.isInsideZone(ZoneId.SOLO))
			return false;
		else if (!TvTEvent.isParticipating())
			return false;
		if (activeChar.isCursedWeaponEquipped())
		{
			if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tvt/cursed_weapon_restricted.htm");
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CURSED_WEAPON_RESTRICTED_FOR_PARTICIPATE));
		}
		else if (OlympiadManager.getInstance().isRegisteredInComp(activeChar))
		{
			if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tvt/olympiad_restricted.htm");
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_RESTRICTED_FOR_PARTICIPATE));
		}
		else if (activeChar.getKarma() > 0)
		{
			if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tvt/karma_restricted.htm");
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.KARMA_RESTRICTED_FOR_PARTICIPATE));
		}
		else if (TvTEvent.getTeam(0).getParticipatedPlayerCount() >= Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM && TvTEvent.getTeam(1).getParticipatedPlayerCount() >= Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM)
		{
			if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tvt/players_restricted.htm");
				html.replace("%number%", Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM);
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_PARTICIPATE_EVENT_TEAM_IS_FULL_S1).addNumber(Config.TVT_EVENT_MAXIMAL_PLAYERS_IN_TEAM));
		}
		else if (activeChar.getLevel() < Config.TVT_EVENT_MIN_LVL || activeChar.getLevel() > Config.TVT_EVENT_MAX_LVL)
		{
			if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tvt/level_restricted.htm");
				html.replace("%min%", Config.TVT_EVENT_MIN_LVL);
				html.replace("%max%", Config.TVT_EVENT_MAX_LVL);
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_PARTICIPATE_EVENT_TEAM_LEVEL_S1_TO_S2).addNumber(Config.TVT_EVENT_MIN_LVL).addNumber(Config.TVT_EVENT_MAX_LVL));
		}
		else if (activeChar.getClassId().getId() == 16 || activeChar.getClassId().getId() == 97 || activeChar.getClassId().getId() == 105 || activeChar.getClassId().getId() == 30 || activeChar.getClassId().getId() == 112 || activeChar.getClassId().getId() == 43 || activeChar.getClassId().getId() == 4 || activeChar.getClassId().getId() == 5 || activeChar.getClassId().getId() == 6 || activeChar.getClassId().getId() == 90 || activeChar.getClassId().getId() == 91 || activeChar.getClassId().getId() == 19 || activeChar.getClassId().getId() == 20 || activeChar.getClassId().getId() == 99 || activeChar.getClassId().getId() == 32 || activeChar.getClassId().getId() == 33 || activeChar.getClassId().getId() == 106)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body>Sorry! Your class cannot participate in TvT.</body>");
			activeChar.sendPacket(html);
			return false;
		}
		
		if (command.equalsIgnoreCase("tvtjoin"))
		{
			if (TvTEvent.addParticipant(activeChar))
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/register_done.htm");
					activeChar.sendPacket(html);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTICIPATE_IS_DONE));
			}
		}
		else if (command.equalsIgnoreCase("tvtleave"))
		{
			if (TvTEvent.removeParticipant(activeChar.getObjectId()))
			{
				if (Config.TVT_EVENT_NPC_DIALOGUE_HTML_ENABLE)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/tvt/register_cancel.htm");
					activeChar.sendPacket(html);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTICIPATE_IS_CANCELLED));
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}