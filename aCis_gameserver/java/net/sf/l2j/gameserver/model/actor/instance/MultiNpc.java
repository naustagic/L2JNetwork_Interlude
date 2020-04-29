/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.model.actor.instance;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.PlayerNameTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVipStatus;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.HeroTaskManager;

/**
 * @author Baggos
 */
public class MultiNpc extends Folk
{
	public MultiNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null)
			return;
		
		if (command.startsWith("donate"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Noblesse":
						Nobless(player);
						break;
					case "ChangeSex":
						Sex(player);
						break;
					case "CleanPk":
						CleanPk(player);
						break;
					case "FullRec":
						Rec(player);
						break;
					case "ChangeClass":
						final NpcHtmlMessage html = new NpcHtmlMessage(0);
						html.setFile("data/html/mods/donateNpc/50091-2.htm");
						player.sendPacket(html);
						break;
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("clan"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "ClanLevel":
						Clanlvl(player);
						break;
					case "ClanRep_20k":
						ClanRep(player);
						break;
					case "ClanSkills":
						ClanSkill(player);
						break;
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("siege"))
		{
			
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				int castleId = 0;
				
				if (type.startsWith("Gludio"))
					castleId = 1;
				else if (type.startsWith("Dion"))
					castleId = 2;
				else if (type.startsWith("Giran"))
					castleId = 3;
				else if (type.startsWith("Oren"))
					castleId = 4;
				else if (type.startsWith("Aden"))
					castleId = 5;
				else if (type.startsWith("Innadril"))
					castleId = 6;
				else if (type.startsWith("Goddard"))
					castleId = 7;
				else if (type.startsWith("Rune"))
					castleId = 8;
				else if (type.startsWith("Schuttgart"))
					castleId = 9;
				
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				
				if (castle != null && castleId != 0)
					player.sendPacket(new SiegeInfo(castle));
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("color"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Green":
						GreenColor(player);
						break;
					case "Blue":
						BlueColor(player);
						break;
					case "Purple":
						PurpleColor(player);
						break;
					case "Yellow":
						YellowColor(player);
						break;
					case "Gold":
						GoldColor(player);
						break;
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("vip"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Vip15Days":
						Vip15(player);
						break;
					case "Vip30Days":
						Vip30(player);
						break;
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("hero"))
		{
			if (player.isHero())
				player.sendMessage("You're already a Hero.");
			else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.VIP7_ITEM_COUNT, player, true))
			{
				AddHeroStatus(player, player, 7);
				player.broadcastPacket(new SocialAction(player, 16));
			}
		}
		else if (command.equalsIgnoreCase("merchant"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/merchant/50056.htm");
			player.sendPacket(html);
		}
		else if (command.startsWith("active"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Might":
						augments(player, 1062079106, 3132, 10);
						break;
					case "Empower":
						augments(player, 1061423766, 3133, 10);
						break;
					case "DuelMight":
						augments(player, 1062406807, 3134, 10);
						break;
					case "Shield":
						augments(player, 968884225, 3135, 10);
						break;
					case "MagicBarrier":
						augments(player, 956760065, 3136, 10);
						break;
					case "WildMagic":
						augments(player, 1067850844, 3142, 10);
						break;
					case "Focus":
						augments(player, 1067523168, 3141, 10);
						break;
					case "BattleRoad":
						augments(player, 968228865, 3125, 10);
						break;
					case "BlessedBody":
						augments(player, 991625216, 3124, 10);
						break;
					case "Agility":
						augments(player, 1060444351, 3139, 10);
						break;
					case "Heal":
						augments(player, 1061361888, 3123, 10);
						break;
					case "HydroBlast":
						augments(player, 1063590051, 3167, 10);
						break;
					case "AuraFlare":
						augments(player, 1063455338, 3172, 10);
						break;
					case "Hurricane":
						augments(player, 1064108032, 3168, 10);
						break;
					case "ReflectDamage":
						augments(player, 1067588698, 3204, 3);
						break;
					case "Celestial":
						augments(player, 974454785, 3158, 1);
						break;
					case "Stone":
						augments(player, 1060640984, 3169, 10);
						break;
					case "HealEmpower":
						augments(player, 1061230760, 3138, 10);
						break;
					case "ShadowFlare":
						augments(player, 1063520931, 3171, 10);
						break;
					case "Prominence":
						augments(player, 1063327898, 3165, 10);
						break;
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Usage : Bar>");
			}
		}
		else if (command.startsWith("passive"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "DuelMight":
						augments(player, 1067260101, 3243, 10);
						break;
					case "Might":
						augments(player, 1067125363, 3240, 10);
						break;
					case "Shield":
						augments(player, 1067194549, 3244, 10);
						break;
					case "MagicBarrier":
						augments(player, 962068481, 3245, 10);
						break;
					case "Empower":
						augments(player, 1066994296, 3241, 10);
						break;
					case "Agility":
						augments(player, 965279745, 3247, 10);
						break;
					case "Guidance":
						augments(player, 1070537767, 3248, 10);
						break;
					case "Focus":
						augments(player, 1070406728, 3249, 10);
						break;
					case "WildMagic":
						augments(player, 1070599653, 3250, 10);
						break;
					case "ReflectDamage":
						augments(player, 1070472227, 3259, 3);
						break;
					case "HealEmpower":
						augments(player, 1066866909, 3246, 10);
						break;
					case "Prayer":
						augments(player, 1066932422, 3238, 10);
						break;
					
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Usage : Bar>");
			}
		}
		else if (command.startsWith("name"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			String newName = "";
			try
			{
				if (st.hasMoreTokens())
				{
					newName = st.nextToken();
				}
			}
			catch (Exception e)
			{
			}
			if (!conditionsname(newName, player))
				return;
			player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.NAME_ITEM_COUNT, player, true);
			player.setName(newName);
			player.store();
			player.sendMessage("Your new character name is " + newName);
			player.broadcastUserInfo();
			player.sendMessage("Added 255 recommends.");
		}
		else if (command.startsWith("password"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			String newPass = "";
			String repeatNewPass = "";
			
			try
			{
				if (st.hasMoreTokens())
				{
					newPass = st.nextToken();
					repeatNewPass = st.nextToken();
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Please fill all the blanks before requesting for a password change.");
				return;
			}
			
			if (!conditions(newPass, repeatNewPass, player))
				return;
			changePassword(newPass, repeatNewPass, player);
		}
		else if (command.startsWith("partytp"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			String val = "";
			try
			{
				if (st.hasMoreTokens())
				{
					val = st.nextToken();
				}
				Player activeChar = World.getInstance().getPlayer(val);
				teleportTo(val, player, activeChar);
			}
			catch (Exception e)
			{
				// Case of empty or missing coordinates
				player.sendMessage("Incorrect target");
			}
		}
		else if (command.startsWith("teleport"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			String clan = "";
			try
			{
				if (st.hasMoreTokens())
				{
					clan = st.nextToken();
				}
				Player activeChar = World.getInstance().getPlayer(clan);
				teleportToClan(clan, player, activeChar);
			}
			catch (Exception e)
			{
				// Case if the player is not in the same clan.
				player.sendMessage("Incorrect target");
			}
		}
		else if (command.startsWith("enchant"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Weapon":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_RHAND);
						break;
					case "Shield":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_LHAND);
						break;
					case "R-Earring":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_REAR);
						break;
					case "L-Earring":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_LEAR);
						break;
					case "R-Ring":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_RFINGER);
						break;
					case "L-Ring":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_LFINGER);
						break;
					case "Necklace":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_NECK);
						break;
					case "Helmet":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_HEAD);
						break;
					case "Boots":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_FEET);
						break;
					case "Gloves":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_GLOVES);
						break;
					case "Chest":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_CHEST);
						break;
					case "Legs":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_LEGS);
						break;
					case "Tattoo":
						Enchant(player, Config.ENCHANT_MAX_VALUE, Inventory.PAPERDOLL_UNDER);
						break;
				}
			}
			
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("Chat"))
			showChatWindow(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		int page = Integer.parseInt(st.nextToken());
		String filename = "data/html/mods/donateNpc/" + page + ".htm";
		filename = "data/html/mods/donateNpc/50091-" + page + ".htm";
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	public static void Nobless(Player player)
	{
		if (player.isNoble())
			player.sendMessage("You Are Already A Noblesse!");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.NOBL_ITEM_COUNT, player, true))
		{
			player.broadcastPacket(new SocialAction(player, 16));
			player.setNoble(true, true);
			player.sendMessage("You Are Now a Noble! Check your skills.");
			player.broadcastUserInfo();
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void Hero7(Player player)
	{
		if (player.isHero())
			player.sendMessage("You're already a Hero.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.VIP7_ITEM_COUNT, player, true))
		{
			AddHeroStatus(player, player, 7);
			player.broadcastPacket(new SocialAction(player, 16));
		}
	}
	
	public static void Vip15(Player player)
	{
		if (player.isVip())
			player.sendMessage("Your character has already Vip Status.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.VIP15_ITEM_COUNT, player, true))
		{
			AdminVipStatus.AddVipStatus(player, player, 15);
			player.broadcastPacket(new SocialAction(player, 16));
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void AddHeroStatus(Player target, Player player, int time)
	{
		target.broadcastPacket(new SocialAction(target, 16));
		target.setHero(true);
		HeroTaskManager.getInstance().add(target);
		long remainingTime = target.getMemos().getLong("TimeOfHero", 0);
		if (remainingTime > 0)
		{
			target.getMemos().set("TimeOfHero", remainingTime + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "Hero Manager", "Dear " + player.getName() + ", your Hero status has been extended by " + time + " day(s)."));
		}
		else
		{
			target.getMemos().set("TimeOfHero", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(time));
			target.sendPacket(new CreatureSay(0, Say2.HERO_VOICE, "Hero Manager", "Dear " + player.getName() + ", you got Hero Status for " + time + " day(s)."));
			target.broadcastUserInfo();
		}
	}
	
	public static void RemoveHeroStatus(Player target)
	{
		HeroTaskManager.getInstance().remove(target);
		target.getMemos().set("TimeOfHero", 0);
		target.setHero(false);
		target.broadcastPacket(new SocialAction(target, 13));
		target.broadcastUserInfo();
	}
	
	public static void Vip30(Player player)
	{
		if (player.isVip())
			player.sendMessage("Your character has already Vip Status.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.VIP30_ITEM_COUNT, player, true))
		{
			AdminVipStatus.AddVipStatus(player, player, 30);
			player.broadcastPacket(new SocialAction(player, 16));
			player.sendMessage("You engage VIP Status for 30 Days.");
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void GreenColor(Player player)
	{
		if (player.destroyItemByItemId("Consume", 9991, 20, player, true))
		{
			player.setColor(1);
			player.getAppearance().setNameColor(0x009900);
			player.broadcastUserInfo();
			player.sendMessage("Your color name has changed!");
		}
		else
			player.sendMessage("You do not have enough Vote Coins.");
	}
	
	public static void BlueColor(Player player)
	{
		if (player.destroyItemByItemId("Consume", 9991, 20, player, true))
		{
			player.setColor(2);
			player.getAppearance().setNameColor(0xff7f00);
			player.broadcastUserInfo();
			player.sendMessage("Your color name has changed!");
		}
		else
			player.sendMessage("You do not have enough Vote Coins.");
	}
	
	public static void PurpleColor(Player player)
	{
		if (player.destroyItemByItemId("Consume", 9991, 20, player, true))
		{
			player.setColor(3);
			player.getAppearance().setNameColor(0xff00ff);
			player.broadcastUserInfo();
			player.sendMessage("Your color name has changed!");
		}
		else
			player.sendMessage("You do not have enough Vote Coins.");
	}
	
	public static void YellowColor(Player player)
	{
		if (player.destroyItemByItemId("Consume", 9991, 20, player, true))
		{
			player.setColor(4);
			player.getAppearance().setNameColor(0x00ffff);
			player.broadcastUserInfo();
			player.sendMessage("Your color name has changed!");
		}
		else
			player.sendMessage("You do not have enough Vote Coins.");
	}
	
	public static void GoldColor(Player player)
	{
		if (player.destroyItemByItemId("Consume", 9991, 20, player, true))
		{
			player.setColor(5);
			player.getAppearance().setNameColor(0x0099ff);
			player.broadcastUserInfo();
			player.sendMessage("Your color name has changed!");
		}
		else
			player.sendMessage("You do not have enough Vote Coins.");
	}
	
	public static void Sex(Player player)
	{
		if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.SEX_ITEM_COUNT, player, true))
		{
			player.getAppearance().setSex(player.getAppearance().getSex() == Sex.MALE ? Sex.FEMALE : Sex.MALE);
			player.sendMessage("Your gender has been changed, you will be disconected in 3 Seconds!");
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe();
			ThreadPool.schedule(() -> player.logout(false), 3000);
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void Rec(Player player)
	{
		if (player.getRecomHave() == 255)
			player.sendMessage("You already have full recommends.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.REC_ITEM_COUNT, player, true))
		{
			player.setRecomHave(255);
			player.getLastRecomUpdate();
			player.broadcastUserInfo();
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void CleanPk(Player player)
	{
		if (player.getPkKills() < 50)
			player.sendMessage("You do not have enough Pk kills for clean.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.PK_ITEM_COUNT, player, true))
		{
			player.setPkKills(player.getPkKills() - Config.PK_CLEAN);
			player.sendMessage("You have successfully clean " + Config.PK_CLEAN + " pks!");
			player.broadcastUserInfo();
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
		
	}
	
	public static void ClanRep(Player player)
	{
		if (player.getClan() == null)
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
		else if (!player.isClanLeader())
			player.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.CLAN_REP_ITEM_COUNT, player, true))
		{
			player.getClan().addReputationScore(Config.CLAN_REPS);
			player.getClan().broadcastClanStatus();
			player.sendMessage("Your clan reputation score has been increased.");
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void Clanlvl(Player player)
	{
		if (player.getClan() == null)
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
		else if (!player.isClanLeader())
			player.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
		if (player.getClan().getLevel() == 8)
			player.sendMessage("Your clan is already level 8.");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.CLAN_ITEM_COUNT, player, true))
		{
			player.getClan().changeLevel(player.getClan().getLevel() + 1);
			player.getClan().broadcastClanStatus();
			player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 1000, 0));
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void augments(Player player, int attributes, int idaugment, int levelaugment)
	{
		ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		if (rhand != null)
		{
			if (rhand.getItem().getCrystalType().getId() == 0 || rhand.getItem().getCrystalType().getId() == 1 || rhand.getItem().getCrystalType().getId() == 2)
				player.sendMessage("Only for weapons B Grade or above.");
			else if (rhand.isHeroItem())
				player.sendMessage("You Cannot be add Augment On " + rhand.getItemName() + " !");
			else if (!rhand.isAugmented() && player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.AUGM_ITEM_COUNT, player, true))
			{
				player.sendMessage("Successfully To Add " + SkillTable.getInstance().getInfo(idaugment, levelaugment).getName() + ".");
				augmentweapondatabase(player, attributes, idaugment, levelaugment);
			}
		}
	}
	
	public static void augmentweapondatabase(Player player, int attributes, int id, int level)
	{
		ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		L2Augmentation augmentation = new L2Augmentation(attributes, id, level);
		augmentation.applyBonus(player);
		item.setAugmentation(augmentation);
		player.disarmWeapons();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("REPLACE INTO augmentations VALUES(?,?,?,?)"))
		{
			statement.setInt(1, item.getObjectId());
			statement.setInt(2, attributes);
			statement.setInt(3, id);
			statement.setInt(4, level);
			InventoryUpdate iu = new InventoryUpdate();
			player.sendPacket(iu);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}
	
	/**
	 * @param player
	 */
	public static void removeAugmentation(Player player)
	{
		ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		if (item == null)
		{
			player.sendMessage("Equipped first a weapon!");
			return;
		}
		
		if (!item.isAugmented())
		{
			player.sendMessage("The weapon is not augmented.");
			return;
		}
		
		item.getAugmentation().removeBonus(player);
		item.removeAugmentation();
		player.disarmWeapons();
		player.sendMessage("Your augmented has been removed!");
	}
	
	public static void ClanSkill(Player player)
	{
		if (!player.isClanLeader())
			player.sendMessage("Only a clan leader can add clan skills.!");
		else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.CLAN_SKILL_ITEM_COUNT, player, true))
		{
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
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	public static void Enchant(Player player, int enchant, int type)
	{
		ItemInstance item = player.getInventory().getPaperdollItem(type);
		
		if (item != null)
		{
			if (item.getEnchantLevel() == Config.ENCHANT_MAX_VALUE || item.getEnchantLevel() == Config.ENCHANT_MAX_VALUE)
				player.sendMessage("Your " + item.getItemName() + " is already on maximun enchant!");
			else if (item.getItem().getCrystalType().getId() == 0)
				player.sendMessage("You can't Enchant under " + item.getItem().getCrystalType() + " Grade Items!");
			else if (item.isHeroItem())
				player.sendMessage("You Cannot be Enchant On " + item.getItemName() + " !");
			else if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.ENCHANT_ITEM_COUNT, player, true))
			{
				item.setEnchantLevel(enchant);
				item.updateDatabase();
				player.sendPacket(new ItemList(player, false));
				player.broadcastUserInfo();
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
			}
			else
				player.sendMessage("You do not have enough Donate Coins.");
		}
		else
			player.sendMessage("That item doesn't exist in your inventory.");
	}
	
	public static boolean conditionsclass(Player player)
	{
		if (player.isSubClassActive())
		{
			player.sendMessage("You cannot change your Main Class while you're with Sub Class.");
			return false;
		}
		else if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("You cannot change your Main Class while you have been registered for olympiad match.");
			return false;
		}
		else if (player.getInventory().getInventoryItemCount(Config.DONATE_ITEM, -1) < Config.CLASS_ITEM_COUNT)
		{
			player.sendMessage("You do not have enough Donate Coins.");
			return false;
		}
		return true;
	}
	
	public static boolean conditionsname(String newName, Player player)
	{
		if (!newName.matches("^[a-zA-Z0-9]+$"))
		{
			player.sendMessage("Incorrect name. Please try again.");
			return false;
		}
		else if (newName.equals(player.getName()))
		{
			player.sendMessage("Please, choose a different name.");
			return false;
		}
		else if (PlayerNameTable.getInstance().getPlayerObjectId(newName) > 0)
		{
			player.sendMessage("The name " + newName + " already exists.");
			return false;
		}
		else if (player.getInventory().getInventoryItemCount(Config.DONATE_ITEM, -1) < Config.NAME_ITEM_COUNT)
		{
			player.sendMessage("You do not have enough Donate Coins.");
			return false;
		}
		return true;
	}
	
	public static boolean conditions(String newPass, String repeatNewPass, Player player)
	{
		if (newPass.length() < 3)
		{
			player.sendMessage("The new password is too short!");
			return false;
		}
		else if (newPass.length() > 45)
		{
			player.sendMessage("The new password is too long!");
			return false;
		}
		else if (!newPass.equals(repeatNewPass))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PASSWORD_ENTERED_INCORRECT2));
			return false;
		}
		else if (player.getInventory().getInventoryItemCount(Config.DONATE_ITEM, -1) < Config.PASSWORD_ITEM_COUNT)
		{
			player.sendMessage("You do not have enough Donate Coins.");
			return false;
		}
		return true;
	}
	
	public static void changePassword(String newPass, String repeatNewPass, Player activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET password=? WHERE login=?"))
		{
			byte[] newPassword = MessageDigest.getInstance("SHA").digest(newPass.getBytes("UTF-8"));
			
			ps.setString(1, Base64.getEncoder().encodeToString(newPassword));
			ps.setString(2, activeChar.getAccountName());
			ps.executeUpdate();
			
			activeChar.sendMessage("Congratulations! Your password has been changed. You will now be disconnected for security reasons. Please login again.");
			ThreadPool.schedule(() -> activeChar.logout(false), 3000);
		}
		catch (Exception e)
		{
			
		}
	}
	
	public static void teleportTo(String val, Player activeChar, Player target)
	{
		if (target.getObjectId() == activeChar.getObjectId())
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		
		// Check if the attacker is not in the same party
		else if (!activeChar.getParty().getMembers().contains(target))
		{
			activeChar.sendMessage("Your target Isn't in your party.");
			return;
		}
		// Simple checks to avoid exploits
		else if (target.isInFunEvent() || target.isInJail() || target.isInOlympiadMode() || target.isInDuel() || target.isFestivalParticipant() || (target.isInParty() && target.getParty().isInDimensionalRift()) || target.inObserverMode())
		{
			activeChar.sendMessage("Due to the current friend's status, the teleportation failed.");
			return;
		}
		
		else if (target.getClan() != null && CastleManager.getInstance().getCastleByOwner(target.getClan()) != null && CastleManager.getInstance().getCastleByOwner(target.getClan()).getSiege().isInProgress())
		{
			activeChar.sendMessage("As your friend is in siege, you can't go to him/her.");
			return;
		}
		else if (activeChar.getPvpFlag() > 0 || activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("Go away! Flag or Pk player can not be teleported.");
			return;
		}
		else
		{
			int x = target.getX();
			int y = target.getY();
			int z = target.getZ();
			
			activeChar.getAI().setIntention(CtrlIntention.IDLE);
			activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
			activeChar.sendPacket(new ExShowScreenMessage("You will be teleported to " + target.getName() + " in 3 Seconds!", 3000, 2, true));
			ThreadPool.schedule(() -> activeChar.teleToLocation(x, y, z, 0), 3000);
			activeChar.sendMessage("You have teleported to " + target.getName() + ".");
		}
	}
	
	public static void teleportToClan(String clan, Player activeChar, Player target)
	{
		if (target.getObjectId() == activeChar.getObjectId())
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		
		// Check if the player is not in the same clan.
		else if (!activeChar.getClan().isMember(target.getObjectId()))
			return;
		
		// Simple checks to avoid exploits
		else if (target.isInJail() || target.isInOlympiadMode() || target.isInDuel() || target.isFestivalParticipant() || (target.isInParty() && target.getParty().isInDimensionalRift()) || target.inObserverMode())
		{
			activeChar.sendMessage("Due to the current clan member's status, the teleportation failed.");
			return;
		}
		
		else if (target.getClan() != null && CastleManager.getInstance().getCastleByOwner(target.getClan()) != null && CastleManager.getInstance().getCastleByOwner(target.getClan()).getSiege().isInProgress())
		{
			activeChar.sendMessage("As your clan member is in siege, you can't go to him/her.");
			return;
		}
		else if (activeChar.getPvpFlag() > 0 || activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("Go away! Flag or Pk player can not be teleported.");
			return;
		}
		int x = target.getX();
		int y = target.getY();
		int z = target.getZ();
		
		activeChar.getAI().setIntention(CtrlIntention.IDLE);
		activeChar.doCast(SkillTable.getInstance().getInfo(2100, 1));
		activeChar.sendPacket(new ExShowScreenMessage("You will be teleported to " + target.getName() + " in 3 Seconds!", 3000, 2, true));
		ThreadPool.schedule(() -> activeChar.teleToLocation(x, y, z, 0), 3000);
		activeChar.sendMessage("You have teleported to " + target.getName() + ".");
	}
	
	public static void Classes(String command, final Player player)
	{
		if (!conditionsclass(player))
			return;
		
		if (player.destroyItemByItemId("Consume", Config.DONATE_ITEM, Config.CLASS_ITEM_COUNT, player, true))
		{
			for (final L2Skill skill : player.getSkills().values())
				player.removeSkill(skill);
			
			String classes = command.substring(command.indexOf("_") + 1);
			switch (classes)
			{
				case "duelist":
					player.setClassId(88);
					player.setBaseClass(88);
					break;
				case "dreadnought":
					player.setClassId(89);
					player.setBaseClass(89);
					break;
				case "phoenix":
					player.setClassId(90);
					player.setBaseClass(90);
					break;
				case "hell":
					player.setClassId(91);
					player.setBaseClass(91);
					break;
				case "sagittarius":
					player.setClassId(92);
					player.setBaseClass(92);
					break;
				case "adventurer":
					player.setClassId(93);
					player.setBaseClass(93);
					break;
				case "archmage":
					player.setClassId(94);
					player.setBaseClass(94);
					break;
				case "soultaker":
					player.setClassId(95);
					player.setBaseClass(95);
					break;
				case "arcana":
					player.setClassId(96);
					player.setBaseClass(96);
					break;
				case "cardinal":
					player.setClassId(97);
					player.setBaseClass(97);
					break;
				case "hierophant":
					player.setClassId(98);
					player.setBaseClass(98);
					break;
				case "evas":
					player.setClassId(99);
					player.setBaseClass(99);
					break;
				case "muse":
					player.setClassId(100);
					player.setBaseClass(100);
					break;
				case "windrider":
					player.setClassId(101);
					player.setBaseClass(101);
					break;
				case "sentinel":
					player.setClassId(102);
					player.setBaseClass(102);
					break;
				case "mystic":
					player.setClassId(103);
					player.setBaseClass(103);
					break;
				case "elemental":
					player.setClassId(104);
					player.setBaseClass(104);
					break;
				case "saint":
					player.setClassId(105);
					player.setBaseClass(105);
					break;
				case "templar":
					player.setClassId(106);
					player.setBaseClass(106);
					break;
				case "dancer":
					player.setClassId(107);
					player.setBaseClass(107);
					break;
				case "hunter":
					player.setClassId(108);
					player.setBaseClass(108);
					break;
				case "gsentinel":
					player.setClassId(109);
					player.setBaseClass(109);
					break;
				case "screamer":
					player.setClassId(110);
					player.setBaseClass(110);
					break;
				case "master":
					player.setClassId(111);
					player.setBaseClass(111);
					break;
				case "ssaint":
					player.setClassId(112);
					player.setBaseClass(112);
					break;
				case "titan":
					player.setClassId(113);
					player.setBaseClass(113);
					break;
				case "khavatari":
					player.setClassId(114);
					player.setBaseClass(114);
					break;
				case "domi":
					player.setClassId(115);
					player.setBaseClass(115);
					break;
				case "doom":
					player.setClassId(116);
					player.setBaseClass(116);
					break;
				case "fortune":
					player.setClassId(117);
					player.setBaseClass(117);
					break;
				case "maestro":
					player.setClassId(118);
					player.setBaseClass(118);
					break;
			}
			player.store();
			player.broadcastUserInfo();
			player.sendSkillList();
			player.giveAvailableSkills();
			player.sendMessage("Your base class has been changed! You will Be Disconected in 5 Seconds!");
			ThreadPool.schedule(() -> player.logout(false), 5000);
		}
		else
			player.sendMessage("You do not have enough Donate Coins.");
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/mods/donateNpc/" + filename + ".htm";
	}
}
