package net.sf.l2j.gameserver.network.serverpackets;

public class ObservationReturn extends L2GameServerPacket
{
	private final int _x, _y, _z;
	
	public ObservationReturn(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}