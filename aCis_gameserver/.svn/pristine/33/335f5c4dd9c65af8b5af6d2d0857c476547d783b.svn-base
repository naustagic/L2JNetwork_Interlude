package net.sf.l2j.gameserver.model.actor.instance;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author Baggos
 */
public class CasinoManager extends Folk
{
	public CasinoManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (!canInteract(player))
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));
				
				showMainWindow(player, "index");
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	private void showMainWindow(Player activeChar, String name)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/mods/casinomanager/" + name + ".htm");
		html.replace("%rates%", Config.CASINO_CHANCE);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		activeChar.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("sendlist"))
			showMainWindow(player, "list");
		else if (actualCommand.equalsIgnoreCase("bet"))
		{
			try
			{
				int id = Integer.valueOf(st.nextToken());
				int amount = Integer.valueOf(st.nextToken());
				
				if (amount > 0)
				{
					if (player.getInventory().getInventoryItemCount(id, 0) >= amount)
					{
						Bettingonstuckable(player, id, amount);
					}
					else
					{
						player.sendMessage("You must have the amount of this item to play.");
						return;
					}
				}
				else
				{
					player.sendMessage("You must put more than 0 amount.");
					return;
				}
			}
			catch (NoSuchElementException e)
			{
				player.sendMessage("You must fill the amount to continue.");
				return;
			}
			catch (NumberFormatException e)
			{
				player.sendMessage("You can fill only numbers on amount.");
				return;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public static void displayWinBoard(Player player)
	{
		player.broadcastPacket(new SocialAction(player, 3));
		player.sendPacket(new ExShowScreenMessage("Congratulations you win!", 3000, 2, true));
		player.sendMessage("Congratulations you win!");
	}
	
	public static void displayLoseBoard(Player player)
	{
		player.broadcastPacket(new SocialAction(player, 13));
		player.sendPacket(new ExShowScreenMessage("You lost!", 3000, 2, true));
		player.sendMessage("You lost!");
	}
	
	public static void Bettingonstuckable(Player activeChar, int id, int amount)
	{
		int unstuckTimer = (1 * 1000);
		activeChar.setTarget(activeChar);
		SetupGauge sg = new SetupGauge(GaugeColor.BLUE, unstuckTimer);
		activeChar.sendPacket(sg);
		CasinoStuckable ef = new CasinoStuckable(activeChar, id, amount);
		ThreadPool.schedule(ef, 1 * 1000);
	}
	
	static class CasinoStuckable implements Runnable
	{
		private Player _activeChar;
		private int _id;
		private int _amount;
		
		CasinoStuckable(Player activeChar, int id, int amount)
		{
			_activeChar = activeChar;
			_id = id;
			_amount = amount;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
				return;
			
			try
			{
				int chance = Rnd.get(1, Config.CASINO_CHANCE);
				
				if (chance == 1)
				{
					displayWinBoard(_activeChar);
					_activeChar.getInventory().addItem("RewardItem", _id, (_amount * 2), _activeChar, null);
				}
				else
				{
					displayLoseBoard(_activeChar);
					_activeChar.getInventory().destroyItemByItemId("Destroy", _id, _amount, _activeChar, null);
				}
				
				_activeChar.getInventory().updateDatabase();
				_activeChar.sendPacket(new ItemList(_activeChar, true));
				_activeChar.broadcastUserInfo();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}