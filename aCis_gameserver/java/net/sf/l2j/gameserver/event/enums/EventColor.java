package net.sf.l2j.gameserver.event.enums;

/**
 * @author Rootware
 *
 */
public enum EventColor
{
	WHITE(Integer.decode("0xFFFFFF")),
	RED(Integer.decode("0x0000FF")),
	BLUE(Integer.decode("0xDF0101"));
	
	private int _color;
	
	EventColor(int color)
	{
		_color = color;
	}
	
	public int getColor()
	{
		return _color;
	}
}