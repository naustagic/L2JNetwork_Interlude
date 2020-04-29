package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class Menu implements IVoicedCommandHandler
{
	private final String[] _voicedCommands =
	{
		"menu"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		htmlWindows(activeChar);
		return true;
	}
	
	private static void htmlWindows(Player activeChar)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder html = new StringBuilder("");
		html.append("<html><head><title>Menu</title></head><body><center>");
		html.append("<img src=\"networklogo1.networklogo1\" width=294 height=90>");
		html.append("<br>");
		html.append("<table width=305 height=20 bgcolor=000000>");
		html.append("<tr>");
		html.append("<td align=center>Player Personal Menu</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
		html.append("<br><br>");
		html.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
		html.append("<table width=305 height=20 bgcolor=000000>");
		html.append("<tr>");
		html.append("<td align=left>Anti Buff Shield</td>");
		html.append("<td align=center><button value=\"" + (activeChar.isBuffProtected() ? "Activated" : "Disabled") + "\" action=\"bypass -h menu_commands antibuff\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		html.append("<table width=307 height=20 bgcolor=000000>");
		html.append("<tr>");
		html.append("<td align=left>Trade Refusal</td>");
		html.append("<td width=8></td>");
		html.append("<td align=center><button value=\"" + (activeChar.getTradeRefusal() ? "Activated" : "Disabled") + "\" action=\"bypass -h menu_commands traderefusal\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		html.append("<table width=307 height=20 bgcolor=000000>");
		html.append("<tr>");
		html.append("<td align=left>PM Refusal</td>");
		html.append("<td width=20></td>");
		html.append("<td align=center><button value=\"" + (activeChar.isInRefusalMode() ? "Activated" : "Disabled") + "\" action=\"bypass -h menu_commands pmrefusal\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
		html.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
		html.append("<br>");
		html.append("<table width=310 height=20 bgcolor=000000>");
		html.append("<tr>");
		html.append("<td align=left>Server Features</td>");
		html.append("<td width=5></td>");
		html.append("<td align=center><button value=\"Open" + "\" action=\"bypass voiced_info\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td align=left>Raid Boss Info Spawn</td>");
		html.append("<td width=5></td>");
		html.append("<td align=center><button value=\"Open" + "\" action=\"bypass voiced_raidinfo\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td align=left>Repair Character</td>");
		html.append("<td width=5></td>");
		html.append("<td align=center><button value=\"Open" + "\" action=\"bypass voiced_repair\" width=90 height=22 back=\"SS_L2jNetwork.smallb_down\" fore=\"SS_L2jNetwork.smallb\"></td>");
		html.append("</tr>");
		html.append("</table>");
		
		html.append("<img src=\"SS_L2jNetwork.lineo\" width=300 height=3>");
		nhm.setHtml(html.toString());
		activeChar.sendPacket(nhm);
	}
	
	public static void bypass(Player activeChar, String command, StringTokenizer st)
	{
		if (command.equals("antibuff"))
		{
			activeChar.setIsBuffProtected(!activeChar.isBuffProtected());
			activeChar.sendMessage("The anti buff mode " + (activeChar.isBuffProtected() ? "Activated" : "Disabled") + ".");
		}
		else if (command.equals("pmrefusal"))
		{
			activeChar.setInRefusalMode(!activeChar.isInRefusalMode());
			activeChar.sendMessage("The chat/pm refusal mode " + (activeChar.isInRefusalMode() ? "Activated" : "Disabled") + ".");
		}
		else if (command.equals("traderefusal"))
		{
			activeChar.setTradeRefusal(!activeChar.getTradeRefusal());
			activeChar.sendMessage("The trade refusal mode " + (activeChar.getTradeRefusal() ? "Activated" : "Disabled") + ".");
		}
		htmlWindows(activeChar);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}