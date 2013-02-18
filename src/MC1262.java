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

import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;

/**
 * Checks light levels.
 *
 * @author sk89q
 */
public class MC1262 extends BaseIC {
    /**
     * Trigger only on rising edge.
     */
    private boolean triggerOnRising = false;
    
    /**
     * Construct the object.
     * 
     * @param triggerOnRising
     */
    public MC1262(boolean triggerOnRising) {
        this.triggerOnRising = triggerOnRising;
    }
    
    /**
     * Get the title of the IC.
     *
     * @return
     */
    @Override
    public String getTitle() {
        return "LIGHT SENSOR";
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
        String minLightLine = sign.getLine3();

        try {
            Integer.parseInt(minLightLine);
        } catch (NumberFormatException e) {
            return "The third line must indicate the minimum light level.";
        }
        
        if(sign.getLine4().length() > 0)
        {
        	try
        	{
        		String[] attr = sign.getLine4().split(":", 3);
        		
        		int x = Integer.parseInt(attr[0]);
        		int y = 0;
        		int z = 0;
        		
        		if(attr.length > 1)
        			y = Integer.parseInt(attr[1]);
        		if(attr.length > 2)
        			z = Integer.parseInt(attr[2]);
        		
        		if((x > 20) || (x < -20)
        			|| (y > 20) || (y < -20)
        			|| (z > 20) || (z < -20)
        			)
        		{
        			return "The 4th line offsets must be a number from -20 to 20";
        		}
        	}
        	catch(NumberFormatException e)
        	{
        		return "The 4th line must be blank or contain up to 3 offsets";
        	}
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip){
        if (triggerOnRising && !chip.getIn(1).is()) {
            return;
        }
        
        Vector blockPos = chip.getBlockPosition();
        
        int x = blockPos.getBlockX();
        int y = blockPos.getBlockY();
        int z = blockPos.getBlockZ();
        int minLight;
        
        if(chip.getText().getLine4().length() > 0)
        {
        	try
        	{
        		String[] attr = chip.getText().getLine4().split(":", 3);
        		
        		x += Integer.parseInt(attr[0]);
        		
        		if(attr.length > 1)
        			y += Integer.parseInt(attr[1]);
        		if(attr.length > 2)
        			z += Integer.parseInt(attr[2]);
        	}
        	catch(NumberFormatException e)
        	{
        		return;
        	}
        }

        World world = CraftBook.getWorld(chip.getCBWorld());
        y = Math.min(Math.max(0, y), CraftBook.MAP_BLOCK_HEIGHT - 1);
        if (! world.isChunkLoaded(x, y, z)) return;
        
        try{
            String minLightLine = chip.getText().getLine3();
            minLight = Integer.parseInt(minLightLine);
        } catch (NumberFormatException e) {
            return;
        }
        

        float light = world.getLightLevel(x, y + 1, z);
        
        chip.getOut(1).set(light >= minLight);
    }
}