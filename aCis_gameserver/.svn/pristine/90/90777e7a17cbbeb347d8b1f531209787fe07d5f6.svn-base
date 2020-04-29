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
import java.util.Base64;
import java.util.StringTokenizer;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author SweeTs
 */
public class PasswordNpc extends Npc
{
	public PasswordNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("change_password"))
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
	}
	
	private static boolean conditions(String newPass, String repeatNewPass, Player player)
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
		
		return true;
	}
	
	@Override
	public void showChatWindow(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><title>Account Manager</title>");
		sb.append("<body><center>");
		sb.append("<br>");
		sb.append("New password: <edit var=\"new\" width=100 height=15><br>");
		sb.append("Repeat: <edit var=\"repeatnew\" width=100 height=15><br>");
		sb.append("<br><br>");
		sb.append("<a action=\"bypass -h npc_%objectId%_change_password $new $repeatnew\">Change password</a>");
		sb.append("</center></body></html>");
		
		html.setHtml(sb.toString());
		html.replace("%objectId%", getObjectId());
		activeChar.sendPacket(html);
	}
	
	private static void changePassword(String newPass, String repeatNewPass, Player activeChar)
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
			_log.warning("There was an error while updating account:" + e);
		}
	}
}