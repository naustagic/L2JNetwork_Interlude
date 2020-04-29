package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Baggos
 */
public class Exit implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"exit"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("exit") && activeChar.isInsideZone(ZoneId.PVPZONE))
		{
			if (activeChar.isDead() || activeChar.isFakeDeath())
			{
				activeChar.sendMessage("Stand up and use the command.");
				return false;
			}
			activeChar.setIsParalyzed(true);
			activeChar.sendPacket(new ExShowScreenMessage("You will be teleported in 2 seconds.", 2000, 2, true));
			Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2100, 1, 2000, 0));
			ThreadPool.schedule(() -> activeChar.teleToLocation(-79080, 240328, -3440, 20), 2000);
			ThreadPool.schedule(() -> activeChar.setIsParalyzed(false), 2000);
		}
		else if (command.equals("exit") && activeChar.isInsideZone(ZoneId.SOLO))
		{
			if (activeChar.isDead() || activeChar.isFakeDeath())
			{
				activeChar.sendMessage("Stand up and use the command.");
				return false;
			}
			activeChar.setIsParalyzed(true);
			activeChar.sendPacket(new ExShowScreenMessage("You will be teleported in 2 seconds.", 2000, 2, true));
			Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2100, 1, 2000, 0));
			ThreadPool.schedule(() -> activeChar.teleToLocation(-79080, 240328, -3440, 20), 2000);
			ThreadPool.schedule(() -> activeChar.setIsParalyzed(false), 2000);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
