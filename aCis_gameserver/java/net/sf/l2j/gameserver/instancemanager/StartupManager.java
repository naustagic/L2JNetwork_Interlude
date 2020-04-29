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
package net.sf.l2j.gameserver.instancemanager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.taskmanager.ArmorTaskManager;
import net.sf.l2j.gameserver.taskmanager.WeaponTaskManager;

/**
 * @author Baggos
 */
public class StartupManager
{
	public static final StartupManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public static void Welcome(Player player)
	{
		if (player.getClassId().getId() >= 0)
		{
			start(player);
			player.setIsParalyzed(true);
			player.getAppearance().setInvisible();
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					player.sendPacket(new ExShowScreenMessage("Complete your character and get ready for our world!", 10000, 2, true));
				}
			}, 1000 * 2);
		}
	}
	
	public static void doEquip(Player player, int time)
	{
		player.setEquip(true);
		player.setIsParalyzed(true);
		player.getAppearance().setInvisible();
		ArmorTaskManager.getInstance().add(player);
		long remainingTime = player.getMemos().getLong("equipEndTime", 0);
		if (remainingTime > 0)
		{
			player.getMemos().set("equipEndTime", remainingTime + TimeUnit.HOURS.toMillis(time));
		}
		else
		{
			player.getMemos().set("equipEndTime", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(time));
			player.broadcastUserInfo();
		}
	}
	
	public static void removeEquip(Player player)
	{
		ArmorTaskManager.getInstance().remove(player);
		player.getMemos().set("equipEndTime", 0);
		player.setEquip(false);
		player.broadcastUserInfo();
		
	}
	
	public static void doWepEquip(Player player, int time)
	{
		player.setEquip(true);
		player.setIsParalyzed(true);
		player.getAppearance().setInvisible();
		WeaponTaskManager.getInstance().add(player);
		long remainingTime = player.getMemos().getLong("weaponEndTime", 0);
		if (remainingTime > 0)
		{
			player.getMemos().set("weaponEndTime", remainingTime + TimeUnit.HOURS.toMillis(time));
		}
		else
		{
			player.getMemos().set("weaponEndTime", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(time));
			player.broadcastUserInfo();
		}
	}
	
	public static void removeWepEquip(Player player)
	{
		WeaponTaskManager.getInstance().remove(player);
		player.getMemos().set("weaponEndTime", 0);
		player.setEquip(false);
		player.broadcastUserInfo();
	}
	
	public static void onEnterEquip(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("equipEndTime");
		ClassId classes = activeChar.getClassId();
		
		if (now > endDay)
			StartupManager.removeEquip(activeChar);
		if (activeChar.isMageClass())
		{
			activeChar.setEquip(true);
			activeChar.setIsParalyzed(true);
			activeChar.getAppearance().setInvisible();
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/startup/armors/magearmors.htm");
			activeChar.sendPacket(html);
		}
		else if (classes == ClassId.TREASURE_HUNTER || classes == ClassId.HAWKEYE || classes == ClassId.PLAINS_WALKER || classes == ClassId.SILVER_RANGER || classes == ClassId.ABYSS_WALKER || classes == ClassId.PHANTOM_RANGER)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/startup/armors/lightarmors.htm");
			activeChar.sendPacket(html);
		}
		else
		{
			activeChar.setEquip(true);
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/startup/armors/fighterarmors.htm");
			activeChar.sendPacket(html);
		}
	}
	
	public static void onEnterWepEquip(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("weaponEndTime");
		if (now > endDay)
			StartupManager.removeWepEquip(activeChar);
		else if (activeChar.isMageClass())
		{
			activeChar.setEquip(true);
			activeChar.setIsParalyzed(true);
			activeChar.getAppearance().setInvisible();
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/startup/weapons/weapons.htm");
			activeChar.sendPacket(html);
		}
		else
		{
			activeChar.setEquip(true);
			activeChar.setIsParalyzed(true);
			activeChar.getAppearance().setInvisible();
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/startup/weapons/weapons.htm");
			activeChar.sendPacket(html);
		}
	}
	
	public static void start(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		switch (player.getClassId().getId())
		{
			case 0:
				html.setFile("data/html/mods/startup/classes/humanclasses.htm");
				player.sendPacket(html);
				break;
			case 10:
				html.setFile("data/html/mods/startup/classes/humanmageclasses.htm");
				player.sendPacket(html);
				break;
			case 18:
				html.setFile("data/html/mods/startup/classes/elfclasses.htm");
				player.sendPacket(html);
				break;
			case 25:
				html.setFile("data/html/mods/startup/classes/elfmageclasses.htm");
				player.sendPacket(html);
				break;
			case 31:
				html.setFile("data/html/mods/startup/classes/darkelfclasses.htm");
				player.sendPacket(html);
				break;
			case 38:
				html.setFile("data/html/mods/startup/classes/darkelfmageclasses.htm");
				player.sendPacket(html);
				break;
			case 44:
				html.setFile("data/html/mods/startup/classes/orcclasses.htm");
				player.sendPacket(html);
				break;
			case 49:
				html.setFile("data/html/mods/startup/classes/orcmageclasses.htm");
				player.sendPacket(html);
				break;
			case 53:
				html.setFile("data/html/mods/startup/classes/dwarfclasses.htm");
				player.sendPacket(html);
				break;
		}
		
	}
	
	public void MageClasses(String command, Player player)
	{
		String params = command.substring(command.indexOf("_") + 1);
		switch (params)
		{
			case "necromancer":
				player.setClassId(13);
				player.setBaseClass(13);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Necromancer!", 3000, 2, true));
				break;
			case "sorceror":
				player.setClassId(12);
				player.setBaseClass(12);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Sorceror!", 3000, 2, true));
				break;
			case "warlock":
				player.setClassId(14);
				player.setBaseClass(14);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Warlock!", 3000, 2, true));
				break;
			case "cleric":
				player.setClassId(15);
				player.setBaseClass(15);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Cleric!", 3000, 2, true));
				break;
			case "bishop":
				player.setClassId(16);
				player.setBaseClass(16);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Bishop!", 3000, 2, true));
				break;
			case "prophet":
				player.setClassId(17);
				player.setBaseClass(17);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Prophet!", 3000, 2, true));
				break;
			case "spellsinger":
				player.setClassId(27);
				player.setBaseClass(27);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Spellsinger!", 3000, 2, true));
				break;
			case "elemental":
				player.setClassId(28);
				player.setBaseClass(28);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Elemental Summoner!", 3000, 2, true));
				break;
			case "elder":
				player.setClassId(30);
				player.setBaseClass(30);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Elven Elder!", 3000, 2, true));
				break;
			case "spellhowler":
				player.setClassId(40);
				player.setBaseClass(40);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Spellhowler!", 3000, 2, true));
				break;
			case "shilliene":
				player.setClassId(43);
				player.setBaseClass(43);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Shillien Elder!", 3000, 2, true));
				break;
			case "overlord":
				player.setClassId(51);
				player.setBaseClass(51);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Overlord!", 3000, 2, true));
				break;
			case "warcryer":
				player.setClassId(52);
				player.setBaseClass(52);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Warcryer!", 3000, 2, true));
				break;
		}
		player.addExpAndSp(Experience.LEVEL[Config.NEWBIE_LVL], 0);
		player.broadcastPacket(new SocialAction(player, 3));
		player.refreshOverloaded();
		player.store();
		player.sendPacket(new HennaInfo(player));
		player.broadcastUserInfo();
		doEquip(player, 90);
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/startup/armors/magearmors.htm");
		player.sendPacket(html);
	}
	
	public void FighterClasses(String command, Player player)
	{
		ClassId classes = player.getClassId();
		
		String params = command.substring(command.indexOf("_") + 1);
		switch (params)
		{
			case "gladiator":
				player.setClassId(2);
				player.setBaseClass(2);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Gladiator!", 4000, 2, true));
				break;
			case "warlord":
				player.setClassId(3);
				player.setBaseClass(3);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Warlord!", 4000, 2, true));
				break;
			case "paladin":
				player.setClassId(5);
				player.setBaseClass(5);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Paladin!", 4000, 2, true));
				break;
			case "darkavenger":
				player.setClassId(6);
				player.setBaseClass(6);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Dark Avenger!", 4000, 2, true));
				break;
			case "temple":
				player.setClassId(20);
				player.setBaseClass(20);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Temple Knight!", 4000, 2, true));
				break;
			case "swordsinger":
				player.setClassId(21);
				player.setBaseClass(21);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Swordsinger!", 4000, 2, true));
				break;
			case "shillien":
				player.setClassId(33);
				player.setBaseClass(33);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Shillien Knight!", 4000, 2, true));
				break;
			case "bladedancer":
				player.setClassId(34);
				player.setBaseClass(34);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Bladedancer!", 4000, 2, true));
				break;
			case "phantoms":
				player.setClassId(41);
				player.setBaseClass(41);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Phantom Summoner!", 4000, 2, true));
				break;
			case "destroyer":
				player.setClassId(46);
				player.setBaseClass(46);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Destroyer!", 4000, 2, true));
				break;
			case "tyrant":
				player.setClassId(48);
				player.setBaseClass(48);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Tyrant!", 4000, 2, true));
				break;
			case "bounty":
				player.setClassId(55);
				player.setBaseClass(55);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Bounty Hunter!", 4000, 2, true));
				break;
			case "warsmith":
				player.setClassId(57);
				player.setBaseClass(57);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Warsmith!", 4000, 2, true));
				break;
		}
		player.addExpAndSp(Experience.LEVEL[Config.NEWBIE_LVL], 0);
		player.broadcastPacket(new SocialAction(player, 3));
		player.refreshOverloaded();
		player.store();
		player.sendPacket(new HennaInfo(player));
		player.broadcastUserInfo();
		doEquip(player, 90);
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		if (classes == ClassId.TREASURE_HUNTER || classes == ClassId.HAWKEYE || classes == ClassId.PLAINS_WALKER || classes == ClassId.SILVER_RANGER || classes == ClassId.ABYSS_WALKER || classes == ClassId.PHANTOM_RANGER)
			html.setFile("data/html/mods/startup/armors/lightarmors.htm");
		html.setFile("data/html/mods/startup/armors/fighterarmors.htm");
		player.sendPacket(html);
	}
	
	public void LightClasses(String command, Player player)
	{
		String params = command.substring(command.indexOf("_") + 1);
		switch (params)
		{
			case "treasurehunter":
				player.setClassId(8);
				player.setBaseClass(8);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Treasure Hunter!", 4000, 2, true));
				break;
			case "hawkeye":
				player.setClassId(9);
				player.setBaseClass(9);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Hawkeye!", 4000, 2, true));
				break;
			case "plain":
				player.setClassId(23);
				player.setBaseClass(23);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Plainswalker!", 4000, 2, true));
				break;
			case "silver":
				player.setClassId(24);
				player.setBaseClass(24);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Silver Ranger!", 4000, 2, true));
				break;
			case "abyss":
				player.setClassId(36);
				player.setBaseClass(36);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Abyss Walker!", 4000, 2, true));
				break;
			case "phantom":
				player.setClassId(37);
				player.setBaseClass(37);
				player.sendPacket(new ExShowScreenMessage("Your class has changed to Phantom Ranger!", 4000, 2, true));
				break;
		}
		player.addExpAndSp(Experience.LEVEL[Config.NEWBIE_LVL], 0);
		player.broadcastPacket(new SocialAction(player, 3));
		player.refreshOverloaded();
		player.store();
		player.sendPacket(new HennaInfo(player));
		player.broadcastUserInfo();
		doEquip(player, 90);
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/startup/armors/lightarmors.htm");
		player.sendPacket(html);
	}
	
	public void Classes(String command, Player player)
	{
		String params = command.substring(command.indexOf("_") + 1);
		if (params.startsWith("tlh"))
		{
			removeEquip(player);
			List<Integer> TallumH = Arrays.asList(2382, 547, 5768, 5780, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : TallumH)
			{
				if (TallumH.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("majheavy"))
		{
			List<Integer> MAJH = Arrays.asList(2383, 2419, 5774, 5786, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : MAJH)
			{
				if (MAJH.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("mjlight"))
		{
			List<Integer> MJL = Arrays.asList(2395, 2419, 5775, 5787, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : MJL)
			{
				if (MJL.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("nightlight"))
		{
			List<Integer> MJL = Arrays.asList(2418, 2394, 5772, 5784, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : MJL)
			{
				if (MJL.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("tll"))
		{
			List<Integer> TLL = Arrays.asList(2393, 547, 5769, 5781, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : TLL)
			{
				if (TLL.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("dc"))
		{
			List<Integer> MageArmorDC = Arrays.asList(2407, 512, 5767, 5779, 924, 862, 893, 871, 902, 8563);
			ItemInstance items = null;
			for (int id : MageArmorDC)
			{
				if (MageArmorDC.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("tl"))
		{
			List<Integer> MageArmorTL = Arrays.asList(2400, 2405, 547, 5770, 5782, 924, 862, 893, 871, 902, 8563);
			ItemInstance items = null;
			for (int id : MageArmorTL)
			{
				if (MageArmorTL.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("darkheavy"))
		{
			List<Integer> darkheavy = Arrays.asList(365, 388, 512, 5765, 5777, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : darkheavy)
			{
				if (darkheavy.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("nmh"))
		{
			List<Integer> NMH = Arrays.asList(374, 2418, 5771, 5783, 924, 862, 893, 871, 902, 8185);
			ItemInstance items = null;
			for (int id : NMH)
			{
				if (NMH.contains(id))
				{
					player.getInventory().addItem("Armors", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					removeEquip(player);
					doWepEquip(player, 90);
					player.broadcastCharInfo();
					new InventoryUpdate();
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile("data/html/mods/startup/weapons/weapons.htm");
					player.sendPacket(html);
				}
			}
		}
		else if (params.startsWith("darkhealth"))
		{
			List<Integer> darkhealth = Arrays.asList(5648, 2498);
			ItemInstance items = null;
			for (int id : darkhealth)
			{
				if (darkhealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("darkcdmg"))
		{
			List<Integer> darkcdmg = Arrays.asList(5647, 2498);
			ItemInstance items = null;
			for (int id : darkcdmg)
			{
				if (darkcdmg.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("darkrfocus"))
		{
			List<Integer> darkrfocus = Arrays.asList(5649, 2498);
			ItemInstance items = null;
			for (int id : darkrfocus)
			{
				if (darkrfocus.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("dragonhealth"))
		{
			List<Integer> dragonhealth = Arrays.asList(5644);
			ItemInstance items = null;
			for (int id : dragonhealth)
			{
				if (dragonhealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("dragoncbleed"))
		{
			List<Integer> dragoncbleed = Arrays.asList(5645);
			ItemInstance items = null;
			for (int id : dragoncbleed)
			{
				if (dragoncbleed.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("dragoncdrain"))
		{
			List<Integer> dragoncdrain = Arrays.asList(5646);
			ItemInstance items = null;
			for (int id : dragoncdrain)
			{
				if (dragoncdrain.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("doomanger"))
		{
			List<Integer> doomanger = Arrays.asList(8136);
			ItemInstance items = null;
			for (int id : doomanger)
			{
				if (doomanger.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("doomhealth"))
		{
			List<Integer> doomhealth = Arrays.asList(8135);
			ItemInstance items = null;
			for (int id : doomhealth)
			{
				if (doomhealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("doomrhaste"))
		{
			List<Integer> doomrhaste = Arrays.asList(8137);
			ItemInstance items = null;
			for (int id : doomrhaste)
			{
				if (doomrhaste.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("sepacdamage"))
		{
			List<Integer> sepacdamage = Arrays.asList(5618);
			ItemInstance items = null;
			for (int id : sepacdamage)
			{
				if (sepacdamage.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("sepaguidance"))
		{
			List<Integer> sepaguidance = Arrays.asList(5617);
			ItemInstance items = null;
			for (int id : sepaguidance)
			{
				if (sepaguidance.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("separhaste"))
		{
			List<Integer> separhaste = Arrays.asList(5619);
			ItemInstance items = null;
			for (int id : separhaste)
			{
				if (separhaste.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("soulshot"))
		{
			List<Integer> soulshot = Arrays.asList(5611, 1344);
			ItemInstance items = null;
			for (int id : soulshot)
			{
				if (soulshot.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("soulpoison"))
		{
			List<Integer> soulpoison = Arrays.asList(5613, 1344);
			ItemInstance items = null;
			for (int id : soulpoison)
			{
				if (soulpoison.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("soulrecov"))
		{
			List<Integer> soulrecov = Arrays.asList(5612, 1344);
			ItemInstance items = null;
			for (int id : soulrecov)
			{
				if (soulrecov.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("grindergui"))
		{
			List<Integer> grindergui = Arrays.asList(5624);
			ItemInstance items = null;
			for (int id : grindergui)
			{
				if (grindergui.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("grinderhealth"))
		{
			List<Integer> grinderhealth = Arrays.asList(5625);
			ItemInstance items = null;
			for (int id : grinderhealth)
			{
				if (grinderhealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("grinderrevas"))
		{
			List<Integer> grinderrevas = Arrays.asList(5623);
			ItemInstance items = null;
			for (int id : grinderrevas)
			{
				if (grinderrevas.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("tallumguid"))
		{
			List<Integer> tallumguid = Arrays.asList(5632);
			ItemInstance items = null;
			for (int id : tallumguid)
			{
				if (tallumguid.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("tallumhealth"))
		{
			List<Integer> tallumhealth = Arrays.asList(5633);
			ItemInstance items = null;
			for (int id : tallumhealth)
			{
				if (tallumhealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("tallumblow"))
		{
			List<Integer> tallumblow = Arrays.asList(5634);
			ItemInstance items = null;
			for (int id : tallumblow)
			{
				if (tallumblow.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("somacume"))
		{
			List<Integer> somacume = Arrays.asList(5643, 2498);
			ItemInstance items = null;
			for (int id : somacume)
			{
				if (somacume.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("sompower"))
		{
			List<Integer> sompower = Arrays.asList(5641, 2498);
			ItemInstance items = null;
			for (int id : sompower)
			{
				if (sompower.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("somsilence"))
		{
			List<Integer> somsilence = Arrays.asList(5642, 2498);
			ItemInstance items = null;
			for (int id : somsilence)
			{
				if (somsilence.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("elysiananger"))
		{
			List<Integer> elysiananger = Arrays.asList(5603, 2498);
			ItemInstance items = null;
			for (int id : elysiananger)
			{
				if (elysiananger.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("elysiacdrain"))
		{
			List<Integer> elysiacdrain = Arrays.asList(5604, 2498);
			ItemInstance items = null;
			for (int id : elysiacdrain)
			{
				if (elysiacdrain.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("elysiahealth"))
		{
			List<Integer> elysiahealth = Arrays.asList(5602, 2498);
			ItemInstance items = null;
			for (int id : elysiahealth)
			{
				if (elysiahealth.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("damascusdual"))
		{
			List<Integer> damascusdual = Arrays.asList(5706);
			ItemInstance items = null;
			for (int id : damascusdual)
			{
				if (damascusdual.contains(id))
				{
					player.getInventory().addItem("Weapon", id, 1, player, null);
					items = player.getInventory().getItemByItemId(id);
					player.getInventory().equipItemAndRecord(items);
					player.getInventory().reloadEquippedItems();
					getEnchantEffect(player);
					removeWepEquip(player);
					Buff(player);
					player.broadcastCharInfo();
					new InventoryUpdate();
				}
			}
		}
		else if (params.startsWith("teleport"))
		{
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					player.sendPacket(new ExShowScreenMessage("DONE! ALWAYS RESPECT THE OTHER PLAYERS!", 5000, 2, true));
				}
			}, 1000 * 1);
			ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					StartupManager.Teleport(player);
				}
			}, 1000 * 5);
			
		}
	}
	
	public void getEnchantEffect(Player player)
	{
		final ItemInstance wpn = player.getActiveWeaponInstance();
		if (wpn == null)
			return;
		wpn.setEnchantLevel(4);
	}
	
	public static void HtmlTeleport(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/startup/teleport.htm");
		player.sendPacket(html);
	}
	
	public static void WeaponsPage(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/startup/weapons/weapons.htm");
		player.sendPacket(html);
	}
	
	public static void Buff(Player player)
	{
		for (int id : (player.isMageClass() || player.getClassId() == ClassId.DOMINATOR || player.getClassId() == ClassId.DOOMCRYER) ? Config.NEWBIE_MAGE_BUFFS : Config.NEWBIE_FIGHTER_BUFFS)
		{
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentMp(player.getMaxMp());
			L2Skill buff = SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id));
			buff.getEffects(player, player);
			player.broadcastPacket(new SocialAction(player, 9));
			
			HtmlTeleport(player);
		}
	}
	
	public static void Teleport(Player player)
	{
		player.getAppearance().setVisible();
		player.setIsParalyzed(false);
		player.setIsInvul(false);
		player.teleToLocation(Config.TELE_TO_LOCATION[0], Config.TELE_TO_LOCATION[1], Config.TELE_TO_LOCATION[2], 40);
		removeEquip(player);
		removeWepEquip(player);
	}
	
	private static class SingletonHolder
	{
		protected static final StartupManager _instance = new StartupManager();
	}
}
