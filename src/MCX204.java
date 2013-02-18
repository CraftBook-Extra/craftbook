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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX204 extends MCX201 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "DISPENSER 3.0";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
	@Override
    public boolean requiresPermission() {
        return true;
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a item type on the third line.";
        }
        
        CraftBookItem cbitem = UtilItem.parseCBItem(id);
        
        if (cbitem == null || cbitem.id() < 1) {
            return "Not a valid item: " + sign.getLine3();
        }
        
        if(cbitem.hasEnchantments())
        {
	        for(int i = 0; i < cbitem.enchantments().length; i++)
			{
				CraftBookEnchantment cbenchant = cbitem.enchantment(i);
				
				if(!cbenchant.enchantment().allowed)
				{
					return "Enchantment not allowed: ID# "+cbenchant.enchantment().getId();
				}
				
				Enchantment.Type enchant = Enchantment.Type.fromId(cbenchant.enchantment().getId());
				if(!Enchantment.isValid(enchant, cbenchant.level()))
				{
					if(cbenchant.level() > Enchantment.getMaxLevel(enchant))
					{
						return "Enchantment level is too high. Max for Enchantment ID# "+cbenchant.enchantment().getId()+" is: "+Enchantment.getMaxLevel(enchant);
					}
					if(cbenchant.level() < Enchantment.getMinLevel(enchant))
					{
						return "Enchantment level is too low. Min for Enchantment ID# "+cbenchant.enchantment().getId()+" is: "+Enchantment.getMinLevel(enchant);
					}
					return "Invalid Enchantment: ID# "+cbenchant.enchantment().getId();
				}
			}
        }

        if (!sign.getLine4().isEmpty() && getQuantity(sign.getLine4(), -2) == -2)
        {
        	return "Not a valid quantity: " + sign.getLine4();
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
        	chip.getOut(1).set(false);
            return;
        }
        
        CraftBookItem cbitem = UtilItem.parseCBItem(chip.getText().getLine3());
        
        if(cbitem == null || cbitem.id() <= 0 || Item.Type.fromId(cbitem.id()) == null)
        	return;
        
        int quantity = getQuantity(chip.getText().getLine4(), 1);

        World world = CraftBook.getWorld(chip.getCBWorld());
        
        Vector pos = chip.getBlockPosition();
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        if (!CraftBook.getWorld(chip.getCBWorld()).isChunkLoaded(x, 0, z)) return;
        int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);

        
        for (int y = pos.getBlockY() + 1; y <= maxY; y++)
        {
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)))
            {
            	ItemEntity eitem = world.dropItem(x, y, z, cbitem.id(), quantity, cbitem.color());
            	if(cbitem.hasEnchantments() && eitem != null)
            	{
            		Item item = eitem.getItem();
            		if(item != null)
            		{
            			for(int i = 0; i < cbitem.enchantments().length; i++)
            			{
            				CraftBookEnchantment cbenchant = cbitem.enchantment(i);
            				
            				if(!cbenchant.enchantment().allowed)
            					continue;
            				
            				Enchantment enchant = new Enchantment(Enchantment.Type.fromId(cbenchant.enchantment().getId()), cbenchant.level());
            				
            				if(!enchant.isValid())
            					continue;
            				
            				item.addEnchantment(enchant);
            			}
            		}
            	}
            	chip.getOut(1).set(true);
                return;
            }
        }
        chip.getOut(1).set(false);
    }
}
