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
package net.sf.l2j.gameserver.colorsystem;

import java.util.Set;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.Player;

public class ColorSystem
{
	public void updateNameColor(Player player)
	{
		Set<Integer> pvpscolors = Config.PVPS_COLORS_LIST.keySet();
		for (Integer i : pvpscolors)
		{
			if (player.getPvpKills() >= i && !player.isGM())
				player.getAppearance().setNameColor(Config.PVPS_COLORS_LIST.get(i));
		}
	}
	
	public void updateTitleColor(Player player)
	{
		Set<Integer> pkscolors = Config.PKS_COLORS_LIST.keySet();
		for (Integer i : pkscolors)
		{
			if (player.getPkKills() >= i && !player.isGM())
				player.getAppearance().setTitleColor(Config.PKS_COLORS_LIST.get(i));
		}
	}
	
	public static ColorSystem getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ColorSystem instance = new ColorSystem();
	}
}
