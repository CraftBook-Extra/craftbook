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
public class MCX255 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "LIGHTNING";
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
        if (sign.getLine3().length() != 0) {
            try
            {
            	int y = Integer.parseInt(sign.getLine3());
            	if(y < -CraftBook.MAP_BLOCK_HEIGHT + 2 || y > CraftBook.MAP_BLOCK_HEIGHT - 1)
            		return "Third line needs to be a number from "+(-CraftBook.MAP_BLOCK_HEIGHT + 2)+" to "+(CraftBook.MAP_BLOCK_HEIGHT - 1);
            }
            catch(NumberFormatException e)
            {
            	return "Third line needs to be a number or blank.";
            }
        }

        if (sign.getLine4().length() != 0) {
            return "Fourth line needs to be blank";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
        	chip.getOut(1).set(false);
            return;
        }
        
        Vector pos = chip.getBlockPosition();
        
        int y = pos.getBlockY();
        if(chip.getText().getLine3().length() > 0)
        {
        	y += Integer.parseInt(chip.getText().getLine3());
        	if(y > CraftBook.MAP_BLOCK_HEIGHT - 1)
        		y = CraftBook.MAP_BLOCK_HEIGHT - 1;
        	else if(y < 1)
        		y = 1; //make sure it lands on at least one block
        }
        else
        {
        	y++;
        }
        
        OWorldServer world = CraftBook.getOWorldServer(chip.getCBWorld());
        world.c(new OEntityLightningBolt(world, pos.getX(), y, pos.getZ()));

        chip.getOut(1).set(true);
    }
}
