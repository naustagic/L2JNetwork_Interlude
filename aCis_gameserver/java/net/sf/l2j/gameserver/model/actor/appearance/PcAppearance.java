package net.sf.l2j.gameserver.model.actor.appearance;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.base.Sex;

public final class PcAppearance
{
	private Player _owner;
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	private Sex _sex;
	private boolean _invisible = false;
	private int _nameColor = 0;
	private int _titleColor = 0;
	private String _visibleTitle = null;
	private String _visibleName;
	
	public PcAppearance(byte face, byte hColor, byte hStyle, Sex sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}
	
	public byte getFace()
	{
		return _face;
	}
	
	public void setFace(int value)
	{
		_face = (byte) value;
	}
	
	public byte getHairColor()
	{
		return _hairColor;
	}
	
	public void setHairColor(int value)
	{
		_hairColor = (byte) value;
	}
	
	public byte getHairStyle()
	{
		return _hairStyle;
	}
	
	public void setHairStyle(int value)
	{
		_hairStyle = (byte) value;
	}
	
	public Sex getSex()
	{
		return _sex;
	}
	
	public void setSex(Sex sex)
	{
		_sex = sex;
	}
	
	public boolean getInvisible()
	{
		return _invisible;
	}
	
	public void setInvisible()
	{
		_invisible = true;
	}
	
	public void setVisible()
	{
		_invisible = false;
	}
	
	public int getNameColor()
	{
		if (_nameColor != 0)
			return _nameColor;
		
		return _owner.getNameColor();
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public int getTitleColor()
	{
		if (_titleColor != 0)
			return _titleColor;
		
		return _owner.getTitleColor();
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public final void setVisibleName(String visibleName)
	{
		_visibleName = visibleName;
	}
	
	public final String getVisibleName()
	{
		if (_visibleName == null)
		{
			_visibleName = getOwner().getName();
		}
		return _visibleName;
	}
	
	public void setVisibleTitle(String title)
	{
		_visibleTitle = title;
	}
	
	public String getVisibleTitle()
	{
		if (_visibleTitle != null)
			return _visibleTitle;
		
		return _owner.getTitle();
	}
	
	public void setOwner(Player owner)
	{
		_owner = owner;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
}