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

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sets the server time to day or night, repeats the signal.
 *
 * @author Shaun (sturmeh)
 */
public class MCX233 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "WEATHER CONTROL";
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

        if (id.length() > 0)
        {
            try
            {
            	int duration = Integer.parseInt(id);
            	if(duration < 1 || duration > 24000)
            		return "Duration must be from 1 to 24000";
            }
            catch(NumberFormatException e)
            {
            	return "The third line must be a number.";
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
    	
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	int duration;
    	
    	if(chip.getIn(1).is())
    	{
    		if(chip.getText().getLine3().length() > 0)
    			duration = Integer.parseInt(chip.getText().getLine3());
    		else
    			duration = 24000;
    		
    		etc.getMCServer().ab().a(new OPacket70GameEvent(1, 0));
    	}
    	else
    	{
    		duration = 0;
    		etc.getMCServer().ab().a(new OPacket70GameEvent(2, 0));
    	}
    	
    	world.setRainTime(duration);
    	world.setRaining(chip.getIn(1).is());
    	
    	if(chip.getMode() != 't')
    		duration = 0;
    	
    	world.setThunderTime(duration);
    	world.setThundering(chip.getIn(1).is() && chip.getMode() == 't');
        
        chip.getOut(1).set(chip.getIn(1).is());
    }
}
