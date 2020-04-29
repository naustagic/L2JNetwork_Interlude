package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Baggos
 */
public class ClanItem implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		Player player = (Player) playable;
		
		if (player.getClan() == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
			return;
		}
		
		if (player.getClan().getLevel() == 8)
		{
			player.getClan().addReputationScore(1000000);
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));
			player.getClan().broadcastClanStatus();
			player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 1000, 0));
			player.sendMessage("Successful! Your clan is now full.");
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else
		{
			player.getClan().changeLevel(8);
			player.getClan().addReputationScore(1000000);
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));
			player.getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));
			player.getClan().broadcastClanStatus();
			player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 1000, 0));
			player.sendMessage("Successful! Your clan is now full.");
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
}