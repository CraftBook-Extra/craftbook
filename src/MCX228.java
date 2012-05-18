// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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


public class MCX228 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
    	return "IC REFRESH";
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
    	
    	if(!sign.getLine3().isEmpty())
    	{
    		return "3rd line must be blank";
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = isValidDimensions(sign.getLine4());
    		if(out != null)
    			return out;
    	}
    	
        return null;
    }
    
    protected static String isValidDimensions(String settings)
    {
    	String[] args = settings.split("/", 2);
		String[] dim = args[0].split(":", 3);
		if(dim.length != 3)
			return "4th line format: width:height:length/x-offset:y-offset:z-offset";
		try
		{
			int width = Integer.parseInt(dim[0]);
			int height = Integer.parseInt(dim[1]);
			int length = Integer.parseInt(dim[2]);
			if(width < 1 || width > 64)
				return "width must be a number from 1 to 64";
			if(height < 1 || height > CraftBook.MAP_BLOCK_HEIGHT-1)
				return "height must be a number from 1 to "+(CraftBook.MAP_BLOCK_HEIGHT - 1);
			if(length < 1 || length > 64)
				return "length must be a number from 1 to 64";
			
			if(args.length > 1)
			{
				String[] offsets = args[1].split(":", 3);
				if(offsets.length != 3)
					return "4th line format: width:height:length/x-offset:y-offset:z-offset";
				
				int offx = Integer.parseInt(offsets[0]);
				int offy = Integer.parseInt(offsets[1]);
				int offz = Integer.parseInt(offsets[2]);
				
				if(offx < -10 || offx > 10
					|| offy < -10 || offy > 10
					|| offz < -10 || offz > 10)
				{
					return "offset values must be a number from -10 to 10";
				}
			}
		}
		catch(NumberFormatException e)
		{
			return "4th line format: width:height:length/x-offset:y-offset:z-offset";
		}
		
		return null;
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
    	if(chip.getIn(1).isTriggered() && chip.getIn(1).is())
    	{
    		int width = 10;
	    	int height = 1;
	    	int length = 10;
	    	int offx = 0;
	    	int offy = 0;
	    	int offz = 0;
	    	
	    	if(!chip.getText().getLine4().isEmpty())
	    	{
	    		String[] args = chip.getText().getLine4().split("/", 2);
	    		String[] dim = args[0].split(":", 3);
	    		
	    		width = Integer.parseInt(dim[0]);
	    		height = Integer.parseInt(dim[1]);
	    		length = Integer.parseInt(dim[2]);
	    		
	    		if(args.length > 1)
	    		{
	    			String[] offsets = args[1].split(":", 3);
	    			offx = Integer.parseInt(offsets[0]);
	    			offy = Integer.parseInt(offsets[1]);
	    			offz = Integer.parseInt(offsets[2]);
	    		}
	    	}
	    	
	    	if(width > 64)
	    		width = 64;
	    	if(length > 64)
	    		length = 64;
	    	
	    	World world = CraftBook.getWorld(chip.getCBWorld());
	        int data = CraftBook.getBlockData(world, chip.getPosition());
	        BlockArea area = UtilIC.getBlockArea(chip, data, width, height, length, offx, offy, offz);
	        RedstoneListener redListener = (RedstoneListener) chip.getExtra();
	        boolean foundone = false;
	        for(int y = area.getY(); y < area.getY2(); y++)
	        {
		        for(int x = area.getX(); x < area.getX2(); x++)
		        {
		        	for(int z = area.getZ(); z < area.getZ2(); z++)
		        	{
		        		if(world.getBlockIdAt(x, y, z) == BlockType.WALL_SIGN)
		        		{
		        			redListener.onSignAdded(world, x, y, z);
		        			foundone = true;
		        		}
		        	}
		        }
	        }
	        
	        chip.getOut(1).set(foundone);
    	}
    }
}
