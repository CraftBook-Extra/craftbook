


import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
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


/**
 * Between Time
 * @author Shaun (sturmeh)
 */
public class MCX027 extends BaseIC {
	
	private static final int MAX_TIME = 24000;
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "BETWEEN TIME";
    }
    
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	String line3 = sign.getLine3();
    	String line4 = sign.getLine4();
    	
        if(line3.isEmpty())
        {
        	return "Line 3 must have a number from 0 to "+MAX_TIME;
        }
        
        if(!line3.matches("^[0-9\\*]*[0-9]+[0-9\\*]*$") || line3.length() > 5)
        {
        	return "Line 3 must have a number from 0 to "+MAX_TIME;
        }
        
        while(line3.length() < 5)
        {
        	line3 = "0"+line3;
        }
        
        sign.setLine3(line3);
        
        if(!line4.isEmpty())
        {
        	if(!line4.matches("^[0-9\\*]*[0-9]+[0-9\\*]*$") || line4.length() > 5)
            {
            	return "Line 4 must have a number from 0 to "+MAX_TIME;
            }
        	
        	while(line4.length() < 5)
            {
        		line4 = "0"+line4;
            }
        	
        	sign.setLine4(line4);
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
        if(chip.inputAmount() == 0 || chip.getIn(1).is())
        {
        	long worldTime = CraftBook.getWorld(chip.getCBWorld()).getRelativeTime() % 24001;
        	if(chip.getText().getLine3().indexOf("*") == -1 && chip.getText().getLine4().indexOf("*") == -1)
        	{
	        	int time = Integer.parseInt(chip.getText().getLine3());
	        	int timeEnd = MAX_TIME;
	        	if(!chip.getText().getLine4().isEmpty())
	        		timeEnd = Integer.parseInt(chip.getText().getLine4());
	        	
	        	if(time <= timeEnd)
	        	{
	        		chip.getOut(1).set(worldTime >= time && worldTime <= timeEnd);
	        	}
	        	else
	        	{
	        		chip.getOut(1).set(worldTime >= time || worldTime <= timeEnd);
	        	}
        	}
        	else
        	{
        		boolean intime = true;
        		String wtime = Long.toString(worldTime);
        		while(wtime.length() < 5)
                {
        			wtime = "0"+wtime;
                }
        		String line3 = chip.getText().getLine3();
        		String line4 = chip.getText().getLine4();
        		
        		for(int i = 0; i < line3.length(); i++)
        		{
        			if(line3.charAt(i) == '*')
        				continue;
        			
        			int val1 = Integer.parseInt(Character.toString(line3.charAt(i)));
        			int val2 = Integer.parseInt(Character.toString(wtime.charAt(i)));
        			if(val2 < val1 || val2 > val1)
        			{
        				intime = val2 > val1;
        				break;
        			}
        		}
        		
        		if(intime && !line4.isEmpty())
        		{
        			for(int i = 0; i < line4.length(); i++)
        			{
        				if(line4.charAt(i) == '*')
        					continue;
        				
        				int val1 = Integer.parseInt(Character.toString(line4.charAt(i)));
            			int val2 = Integer.parseInt(Character.toString(wtime.charAt(i)));
            			if(val2 > val1 || val2 < val1)
            			{
            				intime = val2 < val1;
            				break;
            			}
        			}
        		}
        		
        		chip.getOut(1).set(intime);
        	}
        }
    }
}
