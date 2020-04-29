package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2EnchantScroll;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.type.CrystalType;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Hasha
 */
public class EnchantData
{
	private static Logger _log = Logger.getLogger(EnchantData.class.getName());
	
	private static final Map<Integer, L2EnchantScroll> _map = new HashMap<>();
	
	public static EnchantData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected EnchantData()
	{
		try
		{
			File f = new File("./data/xml/enchants/enchants.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("enchant".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							
							final int id = Integer.valueOf(attrs.getNamedItem("id").getNodeValue());
							final CrystalType grade = Enum.valueOf(CrystalType.class, attrs.getNamedItem("grade").getNodeValue());
							final boolean weapon = Boolean.valueOf(attrs.getNamedItem("weapon").getNodeValue());
							final boolean breaks = Boolean.valueOf(attrs.getNamedItem("break").getNodeValue());
							final boolean maintain = Boolean.valueOf(attrs.getNamedItem("maintain").getNodeValue());
							
							final String[] list = attrs.getNamedItem("chance").getNodeValue().split(";");
							final byte[] chance = new byte[list.length];
							for (int i = 0; i < list.length; i++)
								chance[i] = Byte.valueOf(list[i]);
							
							_map.put(id, new L2EnchantScroll(grade, weapon, breaks, maintain, chance));
						}
					}
				}
			}
			
			_log.info("EnchantTable: Loaded " + _map.size() + " enchants.");
		}
		catch (Exception e)
		{
			_log.warning("EnchantTable: Error while loading enchant table: " + e);
		}
	}
	
	public L2EnchantScroll getEnchantScroll(ItemInstance item)
	{
		return _map.get(item.getItemId());
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantData _instance = new EnchantData();
	}
}