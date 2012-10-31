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



import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.WorldLocation;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Pig Pad Transporter
 * 
 * For Pigchinko, but could be adapted for other uses.
 *
 * This only teleports Pigs which have a Saddle and Rider both.
 *
 * @author drathus
 */
public class MCM112 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PIGTRANSPORTER";
    }
    
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
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a band name on the third line.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        String id = chip.getText().getLine3();

        if (!id.isEmpty() && chip.getIn(1).is()) {
            WorldLocation dest = MCX113.airwaves.get(id);
            
            if (dest == null) {
                chip.getOut(1).set(false);
            } else
            {
            	dest = dest.add(0.5D, 0.0D, 0.5D);
            	
            	String[] msg;
        		if(chip.getText().getLine4().length() == 0)
        			msg = new String[]{"Woosh!"};
        		else
        			msg = new String[]{chip.getText().getLine4()};
        		
        		chip.getOut(1).set(transport(chip, dest, true, msg));
            }
        } else {
            chip.getOut(1).set(false);
        }
    }
    
    protected boolean transport(ChipState chip, WorldLocation dest, boolean useSafeY, String[] messages)
    {
    	if(dest == null)
    		return false;
    	
    	Vector pos;
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	
    	if(chip.getMode() == 'p' || chip.getMode() == 'P')
    	{
    		pos = Util.getWallSignBack(world, chip.getPosition(), -2);
    		
    		double newY = pos.getY() + 2;
    		
    		if(newY > CraftBook.MAP_BLOCK_HEIGHT)
    			newY = CraftBook.MAP_BLOCK_HEIGHT;
    		
    		pos.setY(newY);
    	}
    	else
    		pos = chip.getBlockPosition();
    	
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        
        int y = getSafeY(world, pos);

        for(Mob mob: world.getAnimalList()) {
        	if (!(mob.getEntity() instanceof OEntityPig)) {
        		continue;
        	}
    		OEntityPig op = (OEntityPig) (mob.getEntity());
			// Check for saddle and rider
			if (op.m() == false || (mob.getEntity()).n == null) {
				continue;
			}
        	
        	Location mLoc = mob.getLocation();
        	Vector mVec = new Vector(mLoc.x, mLoc.y, mLoc.z);
        	
        	if ((mVec.getBlockX() == x || mVec.getBlockX() == x + 1 || mVec.getBlockX() == x - 1)
        	   &&  mVec.getBlockY() == y
        	   && (mVec.getBlockZ() == z || mVec.getBlockZ() == z + 1 || mVec.getBlockZ() == z - 1)) {

        		if(useSafeY) {
        			dest = dest.setY(getSafeY(CraftBook.getWorld(dest.getCBWorld()), dest.getCoordinate()) + 1.0D);
        		}
        		
        		CraftBook.teleportEntity(mob, dest);
        		
        		// Reset the pig to Wander so it loses any previous target
        		OEntity oent = mob.getEntity();
        		OEntityAIWander mobAI = new OEntityAIWander((OEntityCreature)oent, 1);
        		mobAI.e();
        	} else {
        		continue;
        	}
        	
    		if(chip.getMode() == 'P')
    		{
    			//force plate off
        		int bdata = CraftBook.getBlockID(world, mVec);
        		if(bdata == BlockType.STONE_PRESSURE_PLATE || bdata == BlockType.WOODEN_PRESSURE_PLATE)
        		{
        			OWorld oworld = world.getWorld();
        			
        			int bx = mVec.getBlockX();
        			int by = mVec.getBlockY();
        			int bz = mVec.getBlockZ();
        			
        			oworld.c(bx, by, bz, 0);
        			oworld.h(bx, by, bz, bdata);
        			oworld.h(bx, by - 1, bz, bdata);
        			oworld.d(bx, by, bz, bx, by, bz);
        		}
    		}

    		return true;
        }
        
        return false;
    }
    
    protected static int getSafeY(World world, Vector pos)
    {
    	int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
    	
    	for (int y = pos.getBlockY() + 1; y <= maxY; y++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)) &&
            	y < CraftBook.MAP_BLOCK_HEIGHT && BlockType.canPassThrough(CraftBook.getBlockID(world, x, y+1, z))	
            	)
            {
            	return y;
            }
		}
    	
    	return maxY;
    }
}