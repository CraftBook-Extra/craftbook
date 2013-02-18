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



import java.util.Map;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Destination
 *
 * @author sk89q
 */
public class MCX113 extends BaseIC {
	
	private final String TITLE = "DESTINATION";
	/**
     * Data store.
     */
    public static Map<String,WorldLocation> airwaves =
            new HistoryHashMap<String,WorldLocation>(100);
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

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
            return "Specify a band name on the third line.";
        }
        
        //if(airwaves.containsKey(id))
        	//return "Band name already exists. Please use another name.";

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        String id = chip.getText().getLine3();
        
        if (!id.isEmpty())
        {
        	Vector pt = chip.getPosition();
        	
        	boolean turnOn;
        	if(chip.inputAmount() == 0)
        	{
        		turnOn = chip.getText().getLine1().charAt(0) == 'D';
        	}
        	else
        	{
        		turnOn = chip.getIn(1).is();
        		
        		if(turnOn && chip.getText().getLine1().charAt(0) == '-')
        		{
        			chip.getText().setLine1(TITLE);
        			chip.getText().supressUpdate();
        		}
        		else if(!turnOn && chip.getText().getLine1().charAt(0) == 'D')
        		{
        			chip.getText().setLine1("-"+TITLE);
        			chip.getText().supressUpdate();
        		}
        	}
        	
        	if(turnOn && !airwaves.containsKey(id))
        	{
	        	Vector bpos = chip.getBlockPosition();
				
	        	float rotation;
	        	if(bpos.getBlockX() - pt.getBlockX() > 0)
					rotation = 90;
				else if(bpos.getBlockX() - pt.getBlockX() < 0)
					rotation = 270;
				else if(bpos.getBlockZ() - pt.getBlockZ() > 0)
					rotation = 180;
				else
					rotation = 0;
	        	
	        	WorldLocation loc = new WorldLocation(chip.getCBWorld(), bpos.getX(), bpos.getY(), bpos.getZ(), rotation, 0);
				
	            airwaves.put(id, loc);
        	}
        	else if(!turnOn && airwaves.containsKey(id))
        	{
        		airwaves.remove(id);
        	}
        	
        	chip.getOut(1).set(false);
        } else {
            chip.getOut(1).set(false);
        }
    }
    
    public String clear(Vector pos, SignText sign)
    {
    	if(airwaves.containsKey(sign.getLine3()))
    	{
    		airwaves.remove(sign.getLine3());
    	}
    	
    	return null;
    }
}
