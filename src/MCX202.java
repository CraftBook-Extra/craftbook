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
 * Chest Dispenser.
 *
 * @author sk89q
 * @author Stefan Steinheimer (nosefish)
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
        
        String[] args = id.split(":", 2);
        int color = getColor(args);
        
        if(color >= 0)
        	id = args[0];
        else if(color == -2)
        	return "Not a valid color/damage value: " + args[1] + ".";
        
        if (getItem(id) < -1) {
            return "Not a valid item type: " + sign.getLine3() + ".";
        }

        if (sign.getLine4().length() > 0 && getQuantity(sign.getLine4(), -2) == -2)
        {
        	return "Not a valid quantity: " + sign.getLine4() + ".";
        }

        return null;
    }
	
	
    @Override
    protected int getQuantity(String value, int defaultOut)
    {
    	int quantity;
		try	{
			quantity = Math.max(1, Integer.parseInt(value));
		}
		catch (NumberFormatException e)	{
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
        
        //find drop position
        World world = CraftBook.getWorld(chip.getCBWorld());
        Vector pos = chip.getBlockPosition();
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        if (!CraftBook.getWorld(chip.getCBWorld()).isChunkLoaded(x, 0, z)) return;
        int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
        int dropY = -1;
        for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z))) {
            	dropY = y;
            	break;
            }
        }
        if (dropY < 0) return; // no space to drop items found
        
        //find chest
        WorldBlockVector wbv = new WorldBlockVector(chip.getCBWorld(), chip.getPosition());
        CBXItemStorage storage = new CBXItemStorage();
        storage.addAllowedStorageBlockType(Block.Type.Chest);
        storage.addAllowedStorageBlockType(Block.Type.TrappedChest);
        boolean found = storage.addNearbyStorageBlocks(wbv);
        if (! found) return; // no chest found
        
        // get item id, datavalue, quantity from sign
        String id = chip.getText().getLine3();
        String[] args = id.split(":", 2);
        int color = getColor(args);
        if(color >= 0)
        	id = args[0];
        int item = getItem(id);
        int quantity = getQuantity(chip.getText().getLine4(), 1);

        //fetch items from chests and drop them
        while (quantity > 0) {
        	Item dropStack = storage.fetchItem(item, color, quantity);
        	if (dropStack == null) break; //no more items to drop in chests
        	quantity -= dropStack.getAmount();
        	world.dropItem(x, dropY, z, dropStack);
        }
        storage.update();
    }
    
}
