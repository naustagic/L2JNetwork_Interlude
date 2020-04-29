package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Baggos
 */
public class Info implements IVoicedCommandHandler
{
	private final String[] _voicedCommands =
	{
		"info"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equalsIgnoreCase("info"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/servnews.htm");
			activeChar.sendPacket(html);
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}