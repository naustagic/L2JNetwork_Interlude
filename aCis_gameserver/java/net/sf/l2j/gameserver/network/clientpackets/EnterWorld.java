
package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.colorsystem.ColorSystem;
import net.sf.l2j.gameserver.communitybbs.Manager.MailBBSManager;
import net.sf.l2j.gameserver.data.MapRegionTable.TeleportType;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSetAio;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminVipStatus;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.CabalType;
import net.sf.l2j.gameserver.instancemanager.SevenSigns.SealType;
import net.sf.l2j.gameserver.instancemanager.StartupManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.MultiNpc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Siege.SiegeSide;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.Clan.SubPledge;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.taskmanager.AfkTaskManager;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;
import net.sf.l2j.gameserver.util.Broadcast;

public class EnterWorld extends L2GameClientPacket
{
	private static final String LOAD_PLAYER_QUESTS = "SELECT name,var,value FROM character_quests WHERE charId=?";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		
		ColorSystem pvpcolor = new ColorSystem();
		pvpcolor.updateNameColor(activeChar);
		pvpcolor.updateTitleColor(activeChar);
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		// Set NewChar
		switch (activeChar.getClassId().getId())
		{
			case 0:
			case 10:
			case 18:
			case 25:
			case 31:
			case 38:
			case 44:
			case 49:
			case 53:
				Player.doNewChar(activeChar, 1);
				break;
		}
		
		int color = activeChar.isColor();
		switch (color)
		{
			case 1:
				activeChar.setColor(1);
				activeChar.getAppearance().setNameColor(0x009900);
				activeChar.broadcastUserInfo();
				break;
			case 2:
				activeChar.setColor(2);
				activeChar.getAppearance().setNameColor(0xff7f00);
				activeChar.broadcastUserInfo();
				break;
			case 3:
				activeChar.setColor(3);
				activeChar.getAppearance().setNameColor(0xbf00ff);
				activeChar.broadcastUserInfo();
				break;
			case 4:
				activeChar.setColor(4);
				activeChar.getAppearance().setNameColor(0x00ffff);
				activeChar.broadcastUserInfo();
				break;
			case 5:
				activeChar.setColor(5);
				activeChar.getAppearance().setNameColor(0x0099ff);
				activeChar.broadcastUserInfo();
				break;
		}
		
		final int objectId = activeChar.getObjectId();
		
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
				activeChar.setIsInvul(true);
			
			if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_hide", activeChar.getAccessLevel()))
				activeChar.getAppearance().setInvisible();
			
			if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
				activeChar.setInRefusalMode(true);
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmlist", activeChar.getAccessLevel()))
				AdminData.getInstance().addGm(activeChar, false);
			else
				AdminData.getInstance().addGm(activeChar, true);
		}
		
		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
			activeChar.setIsDead(true);
		
		// Clan checks.
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
			
			// Refresh player instance.
			clan.getClanMember(objectId).setPlayerInstance(activeChar);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(activeChar);
			final PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(activeChar);
			
			// Send packets to others members.
			for (Player member : clan.getOnlineMembers())
			
			{
				if (member == activeChar)
					continue;
				
				member.sendPacket(msg);
				member.sendPacket(update);
			}
			
			// Send a login notification to sponsor or apprentice, if logged.
			if (activeChar.getSponsor() != 0)
			{
				final Player sponsor = World.getInstance().getPlayer(activeChar.getSponsor());
				if (sponsor != null)
					sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			else if (activeChar.getApprentice() != 0)
			{
				final Player apprentice = World.getInstance().getPlayer(activeChar.getApprentice());
				if (apprentice != null)
					apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(activeChar));
			}
			
			// Add message at connexion if clanHall not paid.
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null && !clanHall.getPaid())
				activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final Siege siege = castle.getSiege();
				if (!siege.isInProgress())
					continue;
				
				final SiegeSide type = siege.getSide(clan);
				if (type == SiegeSide.ATTACKER)
					activeChar.setSiegeState((byte) 1);
				else if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER)
					activeChar.setSiegeState((byte) 2);
			}
			
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.sendPacket(new PledgeStatusChanged(clan));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SealType.STRIFE) != CabalType.NORMAL)
		{
			CabalType cabal = SevenSigns.getInstance().getPlayerCabal(objectId);
			if (cabal != CabalType.NORMAL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SealType.STRIFE))
					activeChar.addSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				else
					activeChar.addSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
			}
		}
		else
		{
			activeChar.removeSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setSpawnProtection(true);
		
		activeChar.spawnMe();
		
		// engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			for (Entry<Integer, IntIntHolder> coupleEntry : CoupleManager.getInstance().getCouples().entrySet())
			{
				final IntIntHolder couple = coupleEntry.getValue();
				if (couple.getId() == objectId || couple.getValue() == objectId)
				
				{
					activeChar.setCoupleId(coupleEntry.getKey());
					break;
					
				}
			}
		}
		
		if (activeChar.isNewChar() && Config.WELCOME_EFFECT)
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				final MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2025, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				activeChar.useMagic(skill, false, false);
			}
		}
		
		if (!activeChar.getPincheck())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile("data/html/mods/Secure/CheckCode.htm");
			activeChar.sendPacket(html);
			activeChar.setIsSubmitingPin(true);
			activeChar.setIsImmobilized(true);
		}
		
		// Announcements, welcome & Seven signs period messages
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		activeChar.sendPacket(SevenSigns.getInstance().getCurrentPeriod().getMessageId());
		AnnouncementData.getInstance().showAnnouncements(activeChar, false);
		
		if (Config.OLYMPIAD_END_ANNOUNE)
			Olympiad.getInstance().olympiadEnd(activeChar);
		
		if (Config.ANNOUNCE_CASTLE_LORDS)
			notifyCastleOwner(activeChar);
		
		if (Config.ANNOUNCE_HEROES)
			if (activeChar.isHero())
				Broadcast.announceToOnlinePlayers("Hero " + activeChar.getName() + " has been logged in.", true);
			
		// if player is DE, check for shadow sense skill at night
		if (activeChar.getRace() == ClassRace.DARK_ELF && activeChar.getSkillLevel(294) == 1)
			activeChar.sendPacket(SystemMessage.getSystemMessage((GameTimeTaskManager.getInstance().isNight()) ? SystemMessageId.NIGHT_S1_EFFECT_APPLIES : SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(294));
		
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new UserInfo(activeChar));
		activeChar.sendPacket(new HennaInfo(activeChar));
		activeChar.sendPacket(new FriendList(activeChar));
		// activeChar.queryGameGuard();
		activeChar.sendPacket(new ItemList(activeChar, false));
		activeChar.sendPacket(new ShortCutInit(activeChar));
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		// no broadcast needed since the player will already spawn dead to others
		if (activeChar.isAlikeDead())
			activeChar.sendPacket(new Die(activeChar));
		
		activeChar.updateEffectIcons();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.sendSkillList();
		// update SA on Enter Character
		activeChar.getInventory().reloadEquippedItems();
		
		if (activeChar.getMemos().getLong("newEndTime", 0) > 0)
			onEnterNewChar(activeChar);
		if (activeChar.getMemos().getLong("aioEndTime", 0) > 0)
			onEnterAio(activeChar);
		if (activeChar.getMemos().getLong("TimeOfVip", 0) > 0)
			onEnterVip(activeChar);
		if (activeChar.getMemos().getLong("TimeOfHero", 0) > 0)
			HeroStatus(activeChar);
		
		// Load quests.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(LOAD_PLAYER_QUESTS);
			statement.setInt(1, objectId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				final String questName = rs.getString("name");
				
				// Test quest existence.
				final Quest quest = ScriptManager.getInstance().getQuest(questName);
				if (quest == null)
				{
					_log.warning("Quest: Unknown quest " + questName + " for player " + activeChar.getName());
					continue;
				}
				
				// Each quest get a single state ; create one QuestState per found <state> variable.
				final String var = rs.getString("var");
				if (var.equals("<state>"))
				{
					new QuestState(activeChar, quest, rs.getByte("value"));
					
					// Notify quest for enterworld event, if quest allows it.
					if (quest.getOnEnterWorld())
						quest.notifyEnterWorld(activeChar);
				}
				// Feed an existing quest state.
				else
				{
					final QuestState qs = activeChar.getQuestState(questName);
					if (qs == null)
					{
						_log.warning("Quest: Unknown quest state " + questName + " for player " + activeChar.getName());
						continue;
					}
					
					qs.setInternal(var, rs.getString("value"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Quest: could not insert char quest:", e);
		}
		
		activeChar.sendPacket(new QuestList(activeChar));
		
		// Unread mails make a popup appears.
		if (Config.ENABLE_COMMUNITY_BOARD && MailBBSManager.getInstance().checkUnreadMail(activeChar) > 0)
		{
			activeChar.sendPacket(SystemMessageId.NEW_MAIL);
			activeChar.sendPacket(new PlaySound("systemmsg_e.1233"));
			activeChar.sendPacket(ExMailArrived.STATIC_PACKET);
		}
		
		if (Config.PM_MESSAGE)
		{
			activeChar.sendPacket(new CreatureSay(0, Say2.TELL, Config.PM_TEXT1, Config.PM_SERVER_NAME));
			activeChar.sendPacket(new CreatureSay(0, Say2.TELL, activeChar.getName(), Config.PM_TEXT2));
		}
		
		// Clan notice, if active.
		if (Config.ENABLE_COMMUNITY_BOARD && clan != null && clan.isNoticeEnabled())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/clan_notice.htm");
			html.replace("%clan_name%", clan.getName());
			html.replace("%notice_text%", clan.getNotice().replaceAll("\r\n", "<br>").replaceAll("action", "").replaceAll("bypass", ""));
			sendPacket(html);
		}
		else if (Config.SERVER_NEWS && activeChar.getPincheck())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/servnews.htm");
			sendPacket(html);
		}
		if (activeChar.getMemos().getLong("equipEndTime", 0) > 0)
			StartupManager.onEnterEquip(activeChar);
		if (activeChar.getMemos().getLong("weaponEndTime", 0) > 0)
			StartupManager.onEnterWepEquip(activeChar);
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		AfkTaskManager.getInstance().add(activeChar);
		
		activeChar.onPlayerEnter();
		onCheckNewbieStep(activeChar);
		activeChar.setAutoCpMpHpSystem(true);
		// Check player participate.
		TvTEvent.onLogin(activeChar);
		
		sendPacket(new SkillCoolTime(activeChar));
		
		// If player logs back in a stadium, port him in nearest town.
		if (Olympiad.getInstance().playerInStadia(activeChar))
			activeChar.teleToLocation(TeleportType.TOWN);
		// If player logs back in flag zone, port him back to port
		if (activeChar.isInsideZone(ZoneId.FLAG))
			activeChar.teleToLocation(112664, 14104, 10072, 20);
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		
		// Attacker or spectator logging into a siege zone will be ported at town.
		if (!activeChar.isGM() && (!activeChar.isInSiege() || activeChar.getSiegeState() < 2) && activeChar.isInsideZone(ZoneId.SIEGE))
			activeChar.teleToLocation(TeleportType.TOWN);
		
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void onEnterNewChar(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("newEndTime");
		
		if (now > endDay)
			Player.removeNewChar(activeChar);
		else
		{
			activeChar.setNewChar(true);
			activeChar.broadcastUserInfo();
		}
	}
	
	private static void onEnterAio(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("aioEndTime");
		
		if (now > endDay)
			AdminSetAio.removeAio(activeChar, activeChar);
		else
		{
			activeChar.setAio(true);
			activeChar.addAioSkills();
			activeChar.broadcastUserInfo();
			sendReEnterMessageAio(activeChar);
		}
	}
	
	private static void notifyCastleOwner(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		if (clan != null)
		{
			if (clan.hasCastle())
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
					Broadcast.announceToOnlinePlayers("Lord " + activeChar.getName() + " Of " + castle.getName() + " Castle is now online!", true);
			}
		}
	}
	
	private static void onCheckNewbieStep(Player player)
	{
		if (!Config.USE_CUSTOM_CAMERA)
			// Starting System
			if (player.getLevel() == Config.START_LEVEL)
				if (Config.ENABLE_STARTUP)
				{
					player.getAppearance().setInvisible();
					StartupManager.Welcome(player);
				}
			
		if (Config.RANDOM_SPAWN_CHAR == 1)
			if (player.isInsideRadius(Config.CUSTOM_SPAWN1[0], Config.CUSTOM_SPAWN1[1], 1250, false) && player.getClassId().level() < 3 && player.getLevel() == Config.START_LEVEL)
				if (Config.USE_CUSTOM_CAMERA)
					player.TutorialCameraOnStart(player);
		if (Config.RANDOM_SPAWN_CHAR == 2)
			if ((player.isInsideRadius(Config.CUSTOM_SPAWN1[0], Config.CUSTOM_SPAWN1[1], 1250, false) || player.isInsideRadius(Config.CUSTOM_SPAWN2[0], Config.CUSTOM_SPAWN2[1], 1250, false)) && player.getClassId().level() < 3 && player.getLevel() == Config.START_LEVEL)
				if (Config.USE_CUSTOM_CAMERA)
					player.TutorialCameraOnStart(player);
		if (Config.RANDOM_SPAWN_CHAR == 3)
			if ((player.isInsideRadius(Config.CUSTOM_SPAWN1[0], Config.CUSTOM_SPAWN1[1], 1250, false) || player.isInsideRadius(Config.CUSTOM_SPAWN2[0], Config.CUSTOM_SPAWN2[1], 1250, false) || player.isInsideRadius(Config.CUSTOM_SPAWN3[0], Config.CUSTOM_SPAWN3[1], 1250, false)) && player.getClassId().level() < 3 && player.getLevel() == Config.START_LEVEL)
				if (Config.USE_CUSTOM_CAMERA)
					player.TutorialCameraOnStart(player);
	}
	
	private static void sendReEnterMessageAio(Player activeChar)
	{
		long delay = activeChar.getMemos().getLong("aioEndTime", 0);
		
		activeChar.sendMessage("Your AIO status ends at: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(delay) + "");
	}
	
	private static void onEnterVip(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("TimeOfVip");
		
		if (now > endDay)
			AdminVipStatus.RemoveVipStatus(activeChar);
		else
		{
			activeChar.setVip(true);
			activeChar.broadcastUserInfo();
		}
	}
	
	private static void HeroStatus(Player activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getMemos().getLong("TimeOfHero");
		
		if (now > endDay)
			MultiNpc.RemoveHeroStatus(activeChar);
		else
		{
			activeChar.setHero(true);
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}