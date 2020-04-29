package net.sf.l2j.gameserver.scripting.scripts.custom;

import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Baggos
 */
public class ValakasTeleport extends Quest
{
	public static final Logger _log = Logger.getLogger(ValakasTeleport.class.getName());
	
	// NPC
	int ValakasNpc = 50058;
	
	// Floating Stone
	int FloatingStoneId = 7267;
	int FloatingStoneCount = 1;
	
	public ValakasTeleport()
	{
		super(-1, "custom");
		
		addStartNpc(ValakasNpc);
		addFirstTalkId(ValakasNpc);
		addTalkId(ValakasNpc);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		return "50058.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmlText = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equals("teleport"))
		{
			if (st.getQuestItemsCount(FloatingStoneId) >= FloatingStoneCount)
			{
				st.takeItems(FloatingStoneId, FloatingStoneCount);
				player.setIsParalyzed(true);
				player.sendPacket(new ExShowScreenMessage("You will be teleported to Valakas Lair in 3 Seconds!", 3000, 2, true));
				Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, 2100, 1, 3000, 0));
				ThreadPool.schedule(() -> player.teleToLocation(204360, -111768, 56, 20), 3000);
				ThreadPool.schedule(() -> player.setIsParalyzed(false), 3000);
			}
			else
				return "50058-1.htm";
		}
		return htmlText;
	}
}