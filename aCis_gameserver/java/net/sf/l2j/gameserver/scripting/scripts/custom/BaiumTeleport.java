package net.sf.l2j.gameserver.scripting.scripts.custom;

import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Baggos
 */
public class BaiumTeleport extends Quest
{
	public static final Logger _log = Logger.getLogger(BaiumTeleport.class.getName());
	
	// NPC
	int BaiumsNpc = 50057;
	
	// Adena
	int AdenaId = 57;
	int AdenaCount = 5000;
	
	public BaiumTeleport()
	{
		super(-1, "custom");
		
		addStartNpc(BaiumsNpc);
		addFirstTalkId(BaiumsNpc);
		addTalkId(BaiumsNpc);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		return "50057.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmlText = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equals("teleport"))
		{
			if (!conditions(player))
				return "50057-1.htm";
			
			if (st.getQuestItemsCount(AdenaId) >= AdenaCount)
			{
				st.takeItems(AdenaId, AdenaCount);
				player.setIsParalyzed(true);
				player.sendPacket(new ExShowScreenMessage("You will be teleported to Arena in 3 Seconds!", 3000, 2, true));
				Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, 2100, 1, 3000, 0));
				ThreadPool.schedule(() -> player.teleToLocation(113144, 14568, 10080, 20), 3000);
				ThreadPool.schedule(() -> player.setIsParalyzed(false), 3000);
				ThreadPool.schedule(() -> player.sendPacket(new CreatureSay(0, Say2.TELL, "Arena", "If you want to leave type .leave")), 6000);
			}
			else
				return "50057-1.htm";
		}
		return htmlText;
	}
	
	/*
	 * Checks if player active class is restricted from solo zone.
	 */
	private static boolean isRestrictedClass(int classId)
	{
		for (String s : Config.SOLO_ZONE_RESTRICTED_CLASSES)
		{
			int Id = Integer.parseInt(s);
			if (Id == classId)
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean conditions(Player player)
	{
		if (isRestrictedClass(player.getActiveClass()) && !player.isGM())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml("<html><body>Sorry! Change class and come back to enjoy the arena.</body>");
			player.sendPacket(html);
			return false;
		}
		
		else if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("You cannot fight while you have been registered for olympiad match.");
			return false;
		}
		else if (!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can not fight when you registering in TvTEvent.");
			return false;
		}
		else if (player.getKarma() > 0)
		{
			player.sendMessage("Go away red!");
			return false;
		}
		return true;
	}
}