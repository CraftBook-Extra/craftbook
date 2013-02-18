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
public class MCX205 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "DETECT BLOCK";
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
        String[] direction = sign.getLine4().split(":",2);

        if (id.length() == 0) {
            return "Specify a block type on the third line.";
        } else if (getItem(id) < 0) {
            return "Not a valid block type: " + sign.getLine3() + ".";
        }

        if(!direction[0].equalsIgnoreCase("up") && !direction[0].equalsIgnoreCase("down"))
        {
        	return "4th line must be UP or DOWN";
        }
        
        if(direction.length > 1)
        {
        	try
        	{
        		int distance = Integer.parseInt(direction[1]);
        		
        		if(distance < 1)
        			return "Distance value must be 1 or greater.";
        	}
        	catch(NumberFormatException e)
        	{
        		return "Distance value must be a number";
        	}
        }

        return null;
    }

    /**
     * Get an item from its name or ID.
     *
     * @param id
     * @return
     */
    private int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
    	if(chip.inputAmount() != 0 && !chip.getIn(1).is())
    	{
    		return;
    	}

        String id = chip.getText().getLine3();
        String[] direction = chip.getText().getLine4().split(":",2);
        
        int distance;
        if(direction.length > 1)
        	distance = Integer.parseInt(direction[1]);
        else
        	distance = CraftBook.MAP_BLOCK_HEIGHT - 1;
        
        if(direction[0].equalsIgnoreCase("up"))
        {
        	distance = chip.getBlockPosition().getBlockY() + distance;
        }
        else
        {
        	distance = chip.getBlockPosition().getBlockY() - distance;
        }
        
        int item = getItem(id);
        
        World world = CraftBook.getWorld(chip.getCBWorld());
        char mode;
        if(chip.inputAmount() == 0)
        {
        	//since self-updates don't get mode, we need to get it
    		mode = ' ';
    		if(chip.getText().getLine2().length() > 8)
    			mode = chip.getText().getLine2().charAt(8);
        }
        else
        {
        	mode = chip.getMode();
        }
        
        int y;
        if(direction.length > 1 && mode == '=')
        {
        	y = searchForBlock(item, world,
				        		chip.getBlockPosition().getBlockX(),
				        		chip.getBlockPosition().getBlockZ(),
				        		direction[0].equalsIgnoreCase("up") ? distance - 1 : distance + 1,
				        		direction[0].equalsIgnoreCase("up") ? distance + 1 : distance);
        }
        else
        {
        	y = searchForBlock(item, world,
				        		chip.getBlockPosition().getBlockX(),
				        		chip.getBlockPosition().getBlockZ(),
				        		chip.getBlockPosition().getBlockY(),
				        		distance);
        }

        chip.getOut(1).set(y != -1);
    }
    
    protected int searchForBlock(int id, World world, int x, int z, int start, int end)
    {
    	if(start < 0)
    		start = 0;
    	else if(start > CraftBook.MAP_BLOCK_HEIGHT - 1)
    		start = CraftBook.MAP_BLOCK_HEIGHT - 1;
    	if(end < 0)
    		end = 0;
    	else if(end > CraftBook.MAP_BLOCK_HEIGHT - 1)
    		end = CraftBook.MAP_BLOCK_HEIGHT - 1;
    	
    	int direction;
    	if(end >= start)
    		direction = 1;
    	else
    		direction = -1;
    	
    	for(int y = start + direction; (direction > 0 && y < end) || (direction < 0 && y >= end); y += direction)
    	{
    		if(id == CraftBook.getBlockID(world, x, y, z))
    			return y;
    	}
    	
    	return -1;
    }
}

