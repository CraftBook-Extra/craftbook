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
import com.sk89q.craftbook.ic.*;

/**
 * Time control.
 *
 * @author sk89q
 */
public class MCT233 extends BaseIC {
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

	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
        if (sign.getLine3().length() != 0) {
        	return "Third line needs to be blank";
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
	@Override
    public void think(ChipState chip) {
    	
    	if (chip.getIn(1).isTriggered() && chip.getIn(1).is())
    	{
    		World world = CraftBook.getWorld(chip.getCBWorld());
    		
	    	int duration;
	    	
	    	if(chip.getIn(2).is())
	    	{
	    		duration = 24000;
	    		
	    		if(!world.isRaining())
	    			etc.getMCServer().ab().a(new OPacket70GameEvent(1, 0));
	    	}
	    	else
	    	{
	    		duration = 0;
	    		
	    		if(world.isRaining())
	    			etc.getMCServer().ab().a(new OPacket70GameEvent(2, 0));
	    	}
	    	
	    	world.setRainTime(duration);
	    	world.setRaining(chip.getIn(2).is());
	    	
	    	if(!chip.getIn(3).is())
	    		duration = 0;
	    	
	    	world.setThunderTime(duration);
	    	world.setThundering(chip.getIn(3).is());
    	}
    }
}
