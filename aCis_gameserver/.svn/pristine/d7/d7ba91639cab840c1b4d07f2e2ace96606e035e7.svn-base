package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class Secure implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"secure",
		"removecode"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("secure") && Config.SECURE_CMD)
		{
			if (activeChar.getPincheck())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/mods/Secure/Secure.htm");
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendMessage("You have already secure your character!");
		}
		else if (command.equals("removecode") && Config.SECURE_CMD)
		{
			if (!activeChar.getPincheck())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/mods/Secure/RemoveSecure.htm");
				activeChar.sendPacket(html);
			}
			else
				activeChar.sendMessage("Your character Isn't secure!");
			
			return true;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}