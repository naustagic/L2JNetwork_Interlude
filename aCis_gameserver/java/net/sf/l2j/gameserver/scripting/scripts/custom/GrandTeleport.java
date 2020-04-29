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
public class GrandTeleport extends Quest
{
	public static final Logger _log = Logger.getLogger(GrandTeleport.class.getName());
	
	// NPC
	int AntharasNpc = 50056;
	
	// Portal Stone
	int PortalStoneId = 3865;
	int PortalStoneCount = 1;
	
	public GrandTeleport()
	{
		super(-1, "custom");
		
		addStartNpc(AntharasNpc);
		addFirstTalkId(AntharasNpc);
		addTalkId(AntharasNpc);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		return "50056.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmlText = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equals("teleport"))
		{
			if (st.getQuestItemsCount(PortalStoneId) >= PortalStoneCount)
			{
				st.takeItems(PortalStoneId, PortalStoneCount);
				player.setIsParalyzed(true);
				player.sendPacket(new ExShowScreenMessage("You will be teleported to Antharas Lair in 3 Seconds!", 3000, 2, true));
				Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, 2100, 1, 3000, 0));
				ThreadPool.schedule(() -> player.teleToLocation(174504, 114664, -7704, 20), 3000);
				ThreadPool.schedule(() -> player.setIsParalyzed(false), 3000);
			}
			else
				return "50056-1.htm";
		}
		return htmlText;
	}
}