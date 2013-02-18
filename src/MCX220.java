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

import java.util.HashMap;
import java.util.Map;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX220 extends BaseIC {
	
	protected static Map<WorldBlockVector, BlockArea> icAreas = new HashMap<WorldBlockVector, BlockArea>();
    /**
     * Get the title of the IC.
     *
     * @return
     */
	private final String TITLE = "DETECT BREAK";
	@Override
	public String getTitle() {
        return "^"+TITLE;
    }
    protected String thisTitle()
    {
    	return TITLE;
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
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "3rd", 1,41, 1,41, 1,41,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		sign.setLine4("");
    	}

        return null;
    }

    protected static boolean blockBroke(WorldBlockVector chipBlock, SignText text)
    {
    	Vector lever = Util.getWallSignBack(chipBlock.getCBWorld(), chipBlock, 2);
    	if (lever != null)
    		Redstone.setOutput(chipBlock.getCBWorld(), lever, true);
    	
    	char mode = ' ';
		if(text.getLine2().length() > 8)
			mode = text.getLine2().charAt(8);
		
    	return mode == '+';
    }
    
    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip)
    {
    	chip.getOut(1).set(false);
    	
    	if(chip.inputAmount() == 0)
    	{
    		WorldBlockVector wblockVec = new WorldBlockVector(chip.getCBWorld(), chip.getPosition());
    		if(hasArea(wblockVec))
    		{
    			return;
    		}
    		
	    	int width = 3;
	    	int height = 3;
	    	int length = 3;
	    	int offx = 0;
	    	int offy = 1;
	    	int offz = 0;
	    	
	    	if(!chip.getText().getLine3().isEmpty())
	    	{
	    		String[] args = chip.getText().getLine3().split("/", 2);
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
	    	
	    	World world = CraftBook.getWorld(chip.getCBWorld());
	        int data = CraftBook.getBlockData(world, chip.getPosition());
	        BlockArea area = UtilIC.getBlockArea(chip, data, width, height, length, offx, offy, offz);
	        addArea(wblockVec, area);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			chip.getText().setLine1("%"+thisTitle());
    			chip.getText().supressUpdate();
    			
    			RedstoneListener redListener = (RedstoneListener) chip.getExtra();
    			redListener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			WorldBlockVector wblockVec = new WorldBlockVector(chip.getCBWorld(), chip.getPosition());
    			removeArea(wblockVec);
    			chip.getText().setLine1("^"+thisTitle());
    			chip.getText().supressUpdate();
    		}
    	}
    }
    
    protected boolean hasArea(WorldBlockVector key)
    {
    	return MCX220.icAreas.containsKey(key);
    }
    
    protected void addArea(WorldBlockVector key, BlockArea area)
    {
    	MCX220.icAreas.put(key, area);
    }
    
    protected void removeArea(WorldBlockVector key)
    {
    	MCX220.icAreas.remove(key);
    }
}
