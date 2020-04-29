package net.sf.l2j.gameserver.model.location;

/**
 * A datatype used to retain a 3D (x/y/z) point. It got the capability to be set and cleaned.
 */
public class Location
{
	public static final Location DUMMY_LOC = new Location(0, 0, 0);
	
	protected volatile int _x;
	protected volatile int _y;
	protected volatile int _z;
	protected volatile int _h;
	
	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
		_h = 0;
	}
	
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_h = heading;
	}
	
	public Location(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_h = loc.getHeading();
	}
	
	@Override
	public String toString()
	{
		return _x + ", " + _y + ", " + _z + ", " + _h;
	}
	
	@Override
	public int hashCode()
	{
		return _x ^ _y ^ _z ^ _h;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Location)
		{
			Location loc = (Location) o;
			return (loc.getX() == _x && loc.getY() == _y && loc.getZ() == _z && loc.getHeading() == _h);
		}
		
		return false;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getHeading()
	{
		return _h;
	}
	
	public void set(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
		_h = 0;
	}
	
	public void set(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_h = heading;
	}
	
	public void set(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_h = loc.getHeading();
	}
	
	public void clean()
	{
		_x = 0;
		_y = 0;
		_z = 0;
		_h = 0;
	}
}