package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.FloodProtectors.Action;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		8
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.TRADE_CHAT))
			return;
		
		if (Config.ALLOW_PVP_CHAT)
			if ((activeChar.getPvpKills() < Config.PVPS_TO_TALK_ON_TRADE) && !activeChar.isGM())
			{
				activeChar.sendMessage("You must have at least " + Config.PVPS_TO_TALK_ON_TRADE + " pvp kills in order to speak in trade chat.");
				return;
			}
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		
		for (Player player : World.getInstance().getPlayers())
		{
			if (!BlockList.isBlocked(player, activeChar) && region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
				player.sendPacket(cs);
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}