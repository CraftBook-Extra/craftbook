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


public class MCX206 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "FLEX SET";
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
    	String line3 = sign.getLine3().toUpperCase();
        //String line4 = sign.getLine4();
        
        if (line3.length() < 5)
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)";

        // Get and validate axis
        String axis = line3.substring(0, 1);
        if (!axis.equals("X") && !axis.equals("Y") && !axis.equals("Z"))
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)     >Missing X, Y, or Z";

        // Get and validate operator
        String op = line3.substring(1, 2);
        if (!op.equals("+") && !op.equals("-"))
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)     >Missing + or -";

        // Get and validate distance
        String sdist = line3.substring(2, 3);
        int dist = -1;
        try {
            dist = Integer.parseInt(sdist);
        } catch (Exception e) {
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)     >Distance was not a number";
        }

        if (op.equals("-"))
            dist = -dist;

        // Syntax requires a : at idx 3
        if (!line3.substring(3, 4).equals(":"))
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)     "
            		+">Missing : between (Distance) and (Block ID). Distance value must be 0 to 9";

        String[] sblock = line3.substring(4).split("@",2);
        int block = -1;
        try {
            block = Integer.parseInt(sblock[0]);
            if(block < 0 || !canUseBlock(block))
            	return "invalid block ID";
            
            if(sblock.length > 1)
            {
            	int color = Integer.parseInt(sblock[1]);
            	if(color < 0 || color > 15)
            		return "invalid color #";
            }
        } catch (Exception e) {
            return "3rd line format:  (Axis-X|Y|Z)(+ or -)(Distance):(Block#)     >Block ID was not a number";
        }

        return null;
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

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        String line3 = chip.getText().getLine3().toUpperCase();
        String line4 = chip.getText().getLine4();

        chip.getOut(1).set(chip.getIn(1).is());

        if (line3.length() < 5)
            return;

        // Get and validate axis
        String axis = line3.substring(0, 1);
        if (!axis.equals("X") && !axis.equals("Y") && !axis.equals("Z"))
            return;

        // Get and validate operator
        String op = line3.substring(1, 2);
        if (!op.equals("+") && !op.equals("-"))
            return;

        // Get and validate distance
        String sdist = line3.substring(2, 3);
        int dist = -1;
        try {
            dist = Integer.parseInt(sdist);
        } catch (Exception e) {
            return;
        }

        if (op.equals("-"))
            dist = -dist;

        // Syntax requires a : at idx 3
        if (!line3.substring(3, 4).equals(":"))
            return;

        String[] sblock = line3.substring(4).split("@",2);
        int block = -1;
        int color = 0;
        try {
            block = Integer.parseInt(sblock[0]);
            if(!canUseBlock(block))
            	return;
            if(sblock.length > 1)
            	color = Integer.parseInt(sblock[1]);
        } catch (Exception e) {
            return;
        }

        boolean hold = line4.toUpperCase().contains("H");
        boolean inp = chip.getIn(1).is();

        Vector blockPos = chip.getBlockPosition();

        int x = blockPos.getBlockX();
        int y = blockPos.getBlockY();
        int z = blockPos.getBlockZ();

        if (axis.equals("X"))
            x += dist;
        else if (axis.equals("Y"))
            y += dist;
        else
            z += dist;
        if (!CraftBook.getWorld(chip.getCBWorld()).isChunkLoaded(x, y, z)) return;
        BlockBag bag = (BlockBag) chip.getExtra();
        bag.addSourcePosition(chip.getCBWorld(), chip.getPosition());

        try
		{
	        if (inp)
	        	bag.setBlockID(chip.getCBWorld(), x, y, z, block, color);
	        else if (hold)
	        	if (CraftBook.getBlockID(chip.getCBWorld(), x, y, z) == block
	        	&& CraftBook.getBlockData(chip.getCBWorld(), x, y, z) == color) {
	        		bag.setBlockID(chip.getCBWorld(), x, y, z, 0);
	        	}
		}
        catch(BlockSourceException e) {}
    }
}
