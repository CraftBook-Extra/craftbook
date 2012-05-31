// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;

import com.sk89q.craftbook.CraftBookEnchantment;
import com.sk89q.craftbook.CraftBookItem;
import com.sk89q.craftbook.EnchantmentType;

/**
 * Library for Minecraft-related functions.
 */
public class UtilItem
{
	public static CraftBookItem parseCBItem(String value)
	{
		return parseCBItem(value, true);
	}
	
	public static CraftBookItem parseCBItem(String value, boolean strict)
	{
		 String[] args = value.split("((?<=[@&])|(?=[@&]))");
		 
		 int id = -1;
		 try
		 {
			 id = Integer.parseInt(args[0]);
		 }
		 catch(NumberFormatException e)
		 {
			 //The reason why this method is here and not in CraftBookItem
			 id = etc.getDataSource().getItem(args[0]);
		 }
		 
		 if(id <= 0)
		 {
			 return null;
		 }
		 
		 ArrayList<CraftBookEnchantment> enchants = new ArrayList<CraftBookEnchantment>();
		 int damage = -1;
		 
         if(args.length > 1)
         {
         	for(int i = 1; i < args.length && i + 1 < args.length; i += 2)
         	{
         		if(args[i].equals("&"))
         		{
         			String[] vals = args[i+1].split("#", 2);
         			EnchantmentType enchant = EnchantmentType.getEnchantment(vals[0]);
         			
         			if(enchant == null)
         			{
         				if(!strict)
         					continue;
         				return null;
         			}
         			
         			int level = 1;
         			if(vals.length > 1)
         			{
         				try
         				{
         					level = Integer.parseInt(vals[1]);
         				}
         				catch(NumberFormatException e)
         				{
         					if(strict)
         					{
         						return null;
         					}
         					
         					level = 1;
         				}
         			}
         			
         			enchants.add(new CraftBookEnchantment(enchant, level));
         		}
         		else if(args[i].equals("@"))
         		{
         			try
         			{
         				damage = Integer.parseInt(args[i+1]);
         			}
         			catch(NumberFormatException e)
         			{
         				if(!strict)
         				{
         					damage = 0;
         					continue;
         				}
         				return null;
         			}
         		}
         		else if(strict)
         		{
         			return null;
         		}
         	}
         }
         
         CraftBookEnchantment[] etypes = null;
         if(enchants.size() > 0)
         {
        	 etypes = new CraftBookEnchantment[enchants.size()];
        	 for(int i = 0; i < etypes.length; i++)
        	 {
        		 etypes[i] = enchants.get(i);
        	 }
         }
         
         return new CraftBookItem(id, damage, etypes);
	}
	
	public static CraftBookEnchantment[] enchantmentsToCBEnchantment(Enchantment[] enchantments)
	{
		if(enchantments == null)
			return null;
		
		CraftBookEnchantment[] cbenchant = new CraftBookEnchantment[enchantments.length];
		
		int pos = 0;
		for(int i = 0; i < enchantments.length; i++)
		{
			EnchantmentType etype = EnchantmentType.getEnchantmentFromId(enchantments[i].getType().getType());
			
			if(etype == null)
			{
				CraftBookListener.logger.warning("CraftBook missing enchantment: "+enchantments[i].getType().getType());
				continue;
			}
			
			cbenchant[pos] = new CraftBookEnchantment(etype, enchantments[i].getLevel());
			pos++;
		}
		
		if(pos < enchantments.length)
		{
			System.arraycopy(cbenchant, 0, cbenchant, 0, pos);
		}
		
		return cbenchant;
	}
	
	public static boolean enchantsAreEqual(Enchantment[] enchantments, CraftBookEnchantment[] cbenchantments)
	{
		if(enchantments == null && cbenchantments == null)
			return true;
		if(enchantments == null && cbenchantments != null)
			return false;
		if(enchantments != null && cbenchantments == null)
			return false;
		
		if(enchantments.length != cbenchantments.length)
			return false;
		if(enchantments.length == 0)
			return true;
		
		CraftBookEnchantment[] cbenchants = new CraftBookEnchantment[cbenchantments.length];
		System.arraycopy(cbenchantments, 0, cbenchants, 0, cbenchantments.length);
		
		enchantLoop:
		for(Enchantment enchant : enchantments)
		{
			for(int i = 0; i < cbenchants.length; i++)
			{
				CraftBookEnchantment cbenchant = cbenchants[i];
				if(cbenchant == null)
					continue;
				
				if(enchant.getType().getType() == cbenchant.enchantment().getId()
					&& enchant.getLevel() == cbenchant.level())
				{
					cbenchants[i] = null;
					continue enchantLoop;
				}
			}
			return false;
		}
		
		return true;
	}
	
	/*
	 * Retooled slightly, allows the same:
	 * <id>:<qty>@<damage>
	 * 
	 * As well as now allowing:
	 * <id>@<damage>
	 * 
	 * Defaulting the <qty> to 1 in the latter case
	 */
	public static int[] getItemInfoFromParts(String[] parts) {
		if (parts == null) return null;
		
		if (parts.length > 1) {
			return oldGetItemInfoFromParts(parts);
		}
		String[] parts2 = parts[0].split("@", 2);
		if (parts2 == null) return null;
		int[] item = new int[3];
		
		try {
			item[0] = Integer.parseInt(parts2[0]);
			if (parts2.length > 1)
				item[1] = Integer.parseInt(parts2[1]);
			else
				item[1] = -1;
			item[2] = 1;
		} catch (NumberFormatException e) {
			return null;
		}
		
		return item;
	}
	
	public static int[] oldGetItemInfoFromParts(String[] parts) {
		if (parts == null || parts.length < 2)
			return null;

		String[] parts2 = parts[1].split("@", 2);
		int[] item = new int[3];

		try {
			item[0] = Integer.parseInt(parts2[0]);
			item[1] = -1;
			if (parts2.length > 1) {
				item[1] = Integer.parseInt(parts2[1]);
				if (item[1] > 15 || item[1] < 0)
					return null;
			}

			item[2] = 1;
			if (parts.length > 2) {
				item[2] = Integer.parseInt(parts[2]);
			}
		} catch (NumberFormatException e) {
			return null;
		}

		return item;
	}

}
