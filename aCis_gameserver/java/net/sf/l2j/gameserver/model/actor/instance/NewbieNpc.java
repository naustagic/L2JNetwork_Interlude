package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Arrays;
import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.CharTemplateTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Baggos
 */
public class NewbieNpc extends Npc
{
	public NewbieNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null)
			return;
		if (!Config.ALLOW_CLASS_MASTERS)
			return;
		
		if (command.equalsIgnoreCase("change"))
		{
			String filename = "data/html/mods/NewbieNpc/changeclass.htm";
			
			if (Config.ALLOW_CLASS_MASTERS)
				filename = "data/html/mods/NewbieNpc/changeclass.htm";
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		if (command.startsWith("1stClass"))
			ClassMaster.showHtmlMenu(player, getObjectId(), 1);
		else if (command.startsWith("2ndClass"))
			ClassMaster.showHtmlMenu(player, getObjectId(), 2);
		else if (command.startsWith("3rdClass"))
			ClassMaster.showHtmlMenu(player, getObjectId(), 3);
		else if (command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));
			
			if (ClassMaster.checkAndChangeClass(player, val))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/classmaster/ok.htm");
				html.replace("%name%", CharTemplateTable.getInstance().getClassNameById(val));
				player.sendPacket(html);
			}
		}
		else if (command.equalsIgnoreCase("LevelUp"))
		{
			final ClassId currentClassId = player.getClassId();
			if (currentClassId.level() > 1)
			{
				player.sendMessage("Level up available only for new players!");
				return;
			}
			player.addExpAndSp(Experience.LEVEL[Config.NEWBIE_LVL], 0);
		}
		else if (command.equalsIgnoreCase("items"))
		{
			final ClassId currentClassId = player.getClassId();
			if (currentClassId.level() < 3)
			{
				player.sendMessage("First complete your Third Class!");
				return;
			}
			if (player.getSp() >= 1)
			{
				player.sendMessage("You already took Items!");
				return;
			}
			
			List<Integer> classes = Arrays.asList(88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118);
			if (classes.contains(player.getClassId().getId()))
			{
				NewbiesItems(player);
				player.addExpAndSp(Experience.LEVEL[0], 1);
			}
		}
		
		else if (command.equalsIgnoreCase("buffs"))
		{
			for (int id : (player.isMageClass() || player.getClassId() == ClassId.DOMINATOR || player.getClassId() == ClassId.DOOMCRYER) ? Config.NEWBIE_MAGE_BUFFS : Config.NEWBIE_FIGHTER_BUFFS)
			{
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentMp(player.getMaxMp());
				L2Skill buff = SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id));
				buff.getEffects(player, player);
				player.broadcastPacket(new MagicSkillUse(player, player, id, buff.getLevel(), 0, 0));
			}
		}
		else if (command.equalsIgnoreCase("teleport"))
		{
			final ClassId currentClassId = player.getClassId();
			if (currentClassId.level() < 3)
			{
				player.sendMessage("You Can't Leave! Your Character Isin't Complete!");
				return;
			}
			player.teleToLocation(Config.TELE_TO_LOCATION[0], Config.TELE_TO_LOCATION[1], Config.TELE_TO_LOCATION[2], 0);
			player.sendPacket(new ExShowScreenMessage("Your character is ready for our world!", 4000, 2, true));
		}
	}
	
	/**
	 * @param player
	 */
	private static void NewbiesItems(Player player)
	{
		ItemInstance items = null;
		ClassId classes = player.getClassId();
		switch (classes)
		{
			case ADVENTURER:
			case WIND_RIDER:
			case GHOST_HUNTER:
				List<Integer> DaggerArmors = Arrays.asList(6590, 6379, 6380, 6381, 6382, 920, 858, 889, 858, 889);
				for (int id : DaggerArmors)
				{
					if (DaggerArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case SAGGITARIUS:
			case GHOST_SENTINEL:
			case MOONLIGHT_SENTINEL:
				List<Integer> ArcherArmors = Arrays.asList(7577, 6379, 6380, 6381, 6382, 920, 858, 889, 858, 889);
				for (int id : ArcherArmors)
				{
					if (ArcherArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case ARCHMAGE:
			case SOULTAKER:
			case ARCANA_LORD:
			case CARDINAL:
			case HIEROPHANT:
			case MYSTIC_MUSE:
			case ELEMENTAL_MASTER:
			case EVAS_SAINT:
			case STORM_SCREAMER:
			case SPECTRAL_MASTER:
			case SHILLIEN_SAINT:
			case DOMINATOR:
			case DOOMCRYER:
				List<Integer> MageArmors = Arrays.asList(6608, 2407, 5767, 5779, 512, 920, 858, 889, 858, 889);
				for (int id : MageArmors)
				{
					if (MageArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case DUELIST:
				List<Integer> DuelistArmor = Arrays.asList(6580, 6373, 6374, 6375, 6376, 6378, 920, 858, 889, 858, 889);
				for (int id : DuelistArmor)
				{
					if (DuelistArmor.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case TITAN:
				List<Integer> TitanArmor = Arrays.asList(6605, 6373, 6374, 6375, 6376, 6378, 920, 858, 889, 858, 889);
				for (int id : TitanArmor)
				{
					if (TitanArmor.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case GRAND_KHAVATARI:
				List<Integer> GrandKhaArmors = Arrays.asList(6604, 6379, 6380, 6381, 6382, 920, 858, 889, 858, 889);
				for (int id : GrandKhaArmors)
				{
					if (GrandKhaArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case PHOENIX_KNIGHT:
			case HELL_KNIGHT:
			case EVAS_TEMPLAR:
			case SHILLIEN_TEMPLAR:
				List<Integer> TankArmors = Arrays.asList(6581, 6373, 6374, 6375, 6376, 6377, 6378, 920, 858, 889, 858, 889);
				for (int id : TankArmors)
				{
					if (TankArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case FORTUNE_SEEKER:
			case MAESTRO:
				List<Integer> DwarfArmors = Arrays.asList(6585, 6373, 6374, 6375, 6376, 6377, 6378, 920, 858, 889, 858, 889);
				for (int id : DwarfArmors)
				{
					if (DwarfArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case DREADNOUGHT:
				List<Integer> DreadArmors = Arrays.asList(6601, 6373, 6374, 6375, 6376, 6378, 920, 858, 889, 858, 889);
				for (int id : DreadArmors)
				{
					if (DreadArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
			case SPECTRAL_DANCER:
			case SWORD_MUSE:
				List<Integer> DancerArmors = Arrays.asList(6580, 6379, 6380, 6381, 6382, 920, 858, 889, 858, 889);
				for (int id : DancerArmors)
				{
					if (DancerArmors.contains(id))
					{
						player.getInventory().addItem("Armors", id, 1, player, null);
						items = player.getInventory().getItemByItemId(id);
						player.getInventory().equipItemAndRecord(items);
						player.getInventory().reloadEquippedItems();
						player.broadcastCharInfo();
						new InventoryUpdate();
					}
				}
				break;
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
		
		return "data/html/mods/newbieNpc/" + filename + ".htm";
	}
}