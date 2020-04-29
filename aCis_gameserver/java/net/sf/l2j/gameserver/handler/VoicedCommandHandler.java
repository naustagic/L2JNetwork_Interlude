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
package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.handler.voicedcommandhandlers.AioMenu;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.BankingCommand;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Exit;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.ExitArena;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.GrandBossStatus;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Info;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Menu;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.OfflineShop;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Online;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Repair;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Secure;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.TvTEventCommand;

/**
 * @author Baggos
 */
public class VoicedCommandHandler
{
	private final Map<Integer, IVoicedCommandHandler> _datatable = new HashMap<>();
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected VoicedCommandHandler()
	{
		registerHandler(new TvTEventCommand());
		registerHandler(new Online());
		registerHandler(new BankingCommand());
		registerHandler(new Menu());
		registerHandler(new GrandBossStatus());
		registerHandler(new Secure());
		registerHandler(new Repair());
		registerHandler(new AioMenu());
		registerHandler(new OfflineShop());
		registerHandler(new Info());
		registerHandler(new Exit());
		registerHandler(new ExitArena());
	}
	
	public void registerHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		
		for (int i = 0; i < ids.length; i++)
			_datatable.put(ids[i].hashCode(), handler);
	}
	
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		
		if (voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}