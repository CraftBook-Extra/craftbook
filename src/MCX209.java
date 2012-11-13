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
public class MCX209 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "BRIDGE+";
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
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign)
    {
    	int[] type = getType(sign.getLine3());
        
        //option check
        if(type[0] == -2)
        	return "Not a valid force value. "+sign.getLine3();
        
        //id check
        if(type[1] == -1)
        {
        	return "Specify a block type on the third line.";
        }
        else if(type[1] == 0 && type[0] < 1)
        {
        	sign.setLine3("f:"+sign.getLine3());
        }
        else if(type[1] < 0)
        {
        	return "Not a valid block type: " + sign.getLine3() + ".";
        }
        else if(!canUseBlock(type[1]))
        {
        	return "Block type not allowed.";
        }
        
        //color check
        if(type[2] == -2)
        	return "Not a valid color value: " + type[2] + ".";
        else if(type[2] == -3)
        	return "Not a valid color value: " + type[2] + ". Color must be a number from 0 to 15.";
        
        
        int[] dimensions = getDimensions(sign.getLine4());
        
        if(dimensions == null)
        	return "Not a valid dimension WxLxH: "+sign.getLine4();
        
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
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -2;//etc.getDataSource().getItem(id);
        }
    }
    
    protected int[] getType(String line)
    {
    	String[] type = line.split(":", 3);
    	
    	int[] out = new int[]{0, -1, -1};
    	
    	String id;
    	String color = "";

    	if(type.length == 3)
    	{
    		if(type[0].equalsIgnoreCase("f"))
    			out[0] = 1;
    		else
    			out[0] = -2; //unknown option error
    		
    		id = type[1];
    		color = type[2];
    	}
    	else if(type.length == 2)
    	{
	    	if(type[0].equalsIgnoreCase("f"))
	    	{
	    		out[0] = 1;
	    		id = type[1];
	    	}
	    	else
	    	{
	    		id = type[0];
	    		color = type[1];
	    	}
    	}
    	else
    	{
    		id = line;
    	}
    	
    	if(id.length() > 0)
    	{
    		out[1] = getItem(id);
    		
    		switch(out[1])
    		{
	    		case BlockType.BED:
	    		case BlockType.PISTON_EXTENSION:
	    		case BlockType.PISTON_MOVED_BLOCK:
	    		case BlockType.END_PORTAL:
	    			out[1] = -2;
	    			break;
    		}
    	}
    	
    	if(color.length() > 0)
    	{
	    	try
	    	{
	    		int colorid = Integer.parseInt(color);
	    		if(colorid < 0 || colorid > 15)
	    			out[2] = -3; //not a valid color value. Must be between 0 and 15
	    		else
	    			out[2] = colorid;
	    	}
	    	catch(NumberFormatException e)
	    	{
	    		out[2] = -2; //not a valid color value.
	    	}
    	}
    	
    	return out;
    }
    
    protected int[] getDimensions(String line)
    {
    	String[] values = line.split(":", 3);
    	
    	int[] out = new int[3];
    	
    	if(values.length == 1)
    		return null;
    	
    	try
    	{
    		for(int i = 0; i < values.length; i++)
    			out[i] = Integer.parseInt(values[i]);
    	}
    	catch(NumberFormatException e)
    	{
    		return null;
    	}
    	
    	if(out[0] <= 0 || out[1] <= 0 || out[0] > 11 || out[2] > 10 || out[2] < -10)
    		return null;
    	
    	if(out[1] > getMaxLength())
    		out[1] = getMaxLength();
    	
    	return out;
    }
    
    protected boolean canUseBlock(int id)
    {
    	if(Bridge.allowedICBlocks == null)
    		return true;
    	
    	if(Bridge.allowedICBlocks.size() == 0)
    		return false;
    	
    	for(Integer bid : Bridge.allowedICBlocks)
    	{
    		if(bid == id)
    			return true;
    	}
    	
    	return false;
    }
    
    protected int getMaxLength()
    {
    	return Bridge.maxLength;
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
        if (!chip.getIn(1).isTriggered())
            return;
        
        int[] type = getType(chip.getText().getLine3());
        int[] values = getDimensions(chip.getText().getLine4());
        
        if(type[0] < 0 || type[1] < 0 || !canUseBlock(type[1]) || type[2] < -1 || values == null || !(chip.getExtra() instanceof BlockBag))
        	return;
        
        if(type[2] < 0)
			type[2] = 0;
        
        BlockBag bag = (BlockBag) chip.getExtra();
        bag.addSourcePosition(chip.getCBWorld(), chip.getPosition());
        
        World world = CraftBook.getWorld(chip.getCBWorld());
        
        int data = CraftBook.getBlockData(world, chip.getPosition());
        
        int wStart = values[0] / 2;
        
        int startX = 0;
        int endX = 0;
        int startZ = 0;
        int endZ = 0;
        
        if (data == 0x2) //east
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + values[0];
        	
        	startZ = (int)chip.getBlockPosition().getZ()+1;
        	endZ = startZ + values[1];
        }
        else if (data == 0x3) //west
        {
        	startX = (int)chip.getPosition().getX() - wStart;
        	endX = startX + values[0];
        	
        	endZ = (int)chip.getBlockPosition().getZ();
        	startZ = endZ - values[1];
        }
        else if (data == 0x4) //north
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + values[0];
        	
        	startX = (int)chip.getBlockPosition().getX()+1;
        	endX = startX + values[1];
        }
        else if (data == 0x5) //south
        {
        	startZ = (int)chip.getPosition().getZ() - wStart;
        	endZ = startZ + values[0];
        	
        	endX = (int)chip.getBlockPosition().getX();
        	startX = endX - values[1];
        }
        
        int y = (int)chip.getPosition().getY() + values[2];
        
        setBlocks(world, startX, endX, y, y+1, startZ, endZ, chip.getIn(1).is(), type, bag);
    }
    
    
    protected static void setBlocks(World world, int startX, int endX, int startY, int endY, int startZ, int endZ,
    		boolean set, int[] type, BlockBag bag)
    {
    	if(startY < 0)
    		startY = 0;
    	else if(startY > CraftBook.MAP_BLOCK_HEIGHT - 1)
    		startY = CraftBook.MAP_BLOCK_HEIGHT - 1;
    	if(endY < 0)
    		endY = 0;
    	else if(endY > CraftBook.MAP_BLOCK_HEIGHT - 1)
    		endY = CraftBook.MAP_BLOCK_HEIGHT - 1;
    	
    	if (!set && type[1]==BlockType.LOG) {
    		type[2] = type[2] & 3;
    	}
    	
    	for(int x = startX; x < endX; x++)
        {
    		for(int y = startY; y < endY; y++)
    		{
	        	for(int z = startZ; z < endZ; z++)
	        	{
	        		int bType = CraftBook.getBlockID(world, x, y, z);
	        		
	        		
	    			try
	    			{
	    				if(set)
	    				{
	    					//set
	    					if(type[0] == 1 || canPassThrough(bType))
	    	        		{
	    						bag.setBlockID(world, x, y, z, type[1], type[2]);
	    	        		}
	    					else if(type[0] == 0 && bType != type[1])
	    	        		{
	    	        			continue;
	    	        		}
	    				}
	    				else
	    				{
	    					int bData = CraftBook.getBlockData(world, x, y , z);
	    			    	// logs are different from all other blocks - 
	    			    	// they use their data value for both color and direction
	    			    	// clear direction before storing - 2 bits for color remain
	    			    	if (bType==BlockType.LOG) {
	    			    		bData = bData & 3;
	    			    	}
	    					
	    					//clear
	    					if(bType == type[1] && (!BlockType.isColorTypeBlock(bType) || bData == type[2]))
	    					{
	    						bag.setBlockID(world, x, y, z, 0);
	    					}
	    					else if(type[0] == 0 && bType != 0)
	    					{
	    						continue;
	    					}
	    				}
	    			}
	    			catch(BlockSourceException e) {}
	        	}
    		}
        }
    }
    
    protected static boolean canPassThrough(int t)
    {
        return t == 0 || t == BlockType.WATER || t == BlockType.STATIONARY_WATER
                || t == BlockType.LAVA || t == BlockType.STATIONARY_LAVA
                || t == BlockType.SNOW;
    }
}
