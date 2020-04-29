package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;

/**
 * @author SweeTs
 */
public class ClanManagerNpc extends Npc
{
	public ClanManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null)
			return;
		
		ItemInstance item = player.getInventory().getItemByItemId(Config.CLAN_ITEM_ID);
		
		if (command.equalsIgnoreCase("castles"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(getHtmlPath(getNpcId(), 1));
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (command.startsWith("siege_"))
		{
			int castleId = 0;
			
			if (command.startsWith("siege_gludio"))
				castleId = 1;
			else if (command.startsWith("siege_dion"))
				castleId = 2;
			else if (command.startsWith("siege_giran"))
				castleId = 3;
			else if (command.startsWith("siege_oren"))
				castleId = 4;
			else if (command.startsWith("siege_aden"))
				castleId = 5;
			else if (command.startsWith("siege_innadril"))
				castleId = 6;
			else if (command.startsWith("siege_goddard"))
				castleId = 7;
			else if (command.startsWith("siege_rune"))
				castleId = 8;
			else if (command.startsWith("siege_schuttgart"))
				castleId = 9;
			
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			
			if (castle != null && castleId != 0)
				player.sendPacket(new SiegeInfo(castle));
		}
		else if (command.equalsIgnoreCase("level_up"))
		{
			if (item == null || item.getCount() < Config.CLAN_COUNT)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			else if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			else if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
				return;
			}
			else if (player.getClan().getLevel() == 8)
			{
				player.sendMessage("Your Clan has full level.");
				return;
			}
			
			player.destroyItemByItemId("Consume", Config.CLAN_ITEM_ID, Config.CLAN_COUNT, null, true);
			player.getClan().changeLevel(player.getClan().getLevel() + 1);
			player.getClan().broadcastClanStatus();
			player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 1000, 0));
			
		}
		else if (command.equalsIgnoreCase("clan_rep"))
		{
			if (item == null || item.getCount() < Config.CLAN_COUNT)
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			else if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			else if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
				return;
			}
			
			player.destroyItemByItemId("Consume", Config.CLAN_ITEM_ID, Config.CLAN_COUNT, null, true);
			player.getClan().addReputationScore(5000);
			player.getClan().broadcastClanStatus();
			player.sendMessage("Your clan reputation score has been increased.");
		}
		else if (command.equalsIgnoreCase("learn_clan_skills"))
		{
			VillageMaster.showPledgeSkillList(player);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/mods/clanManager/" + filename + ".htm";
	}
}