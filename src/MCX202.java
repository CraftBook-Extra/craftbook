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
public class MCX202 extends MCX201 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "CHEST DISPENSER";
    }
    
    @Override
    protected int getQuantity(String value, int defaultOut)
    {
    	int quantity;
    	
		try
		{
			quantity = Math.min(64, Math.max(1, Integer.parseInt(value)));
		}
		catch (NumberFormatException e)
		{
			return defaultOut;
		}
		
        return quantity;
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
            return;
        }
        
        NearbyChestBlockBag source = new NearbyChestBlockBag(chip.getCBWorld(), chip.getPosition());
        source.addSourcePosition(chip.getCBWorld(), chip.getPosition());
        
        String id = chip.getText().getLine3();
        
        String[] args = id.split(":", 2);
        int color = getColor(args);
        
        if(color >= 0)
        	id = args[0];
        
        int quantity = getQuantity(chip.getText().getLine4(), 1);

        int item = getItem(id);

        World world = CraftBook.getWorld(chip.getCBWorld());
        Vector pos = chip.getBlockPosition();
        int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();

        for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z))) {
                int n = 0;
                for(n=0;n<quantity;n++)
                    try {
                        source.fetchBlock(item);
                    } catch (BlockSourceException e) {
                        break;
                    }
                if(n!=0)
                {
                	if(color >= 0)
                		world.dropItem(x, y, z, item, n, color);
                	else
                		world.dropItem(x, y, z, item, n);
                }
                return;
            }
        }
        }
    
}
