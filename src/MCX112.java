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
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX112 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "TRANSPORTER";
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
            Location dest = MCX113.airwaves.get(id);
            
            if (dest == null) {
                chip.getOut(1).set(false);
            } else
            {
            	int dimension = dest.dimension;
            	dest = new Location(dest.x+0.5, dest.y, dest.z+0.5, dest.rotX, dest.rotY);
            	dest.dimension = dimension;
            	
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
    
    protected boolean transport(ChipState chip, Location dest, boolean useSafeY, String[] messages)
    {
    	if(dest == null)
    		return false;
    	
    	Vector pos;
    	World world = CraftBook.getWorld(chip.getWorldType());
    	
    	if(chip.getMode() == 'p' || chip.getMode() == 'P')
    	{
    		pos = Util.getWallSignBack(world, chip.getPosition(), -2);
    		
    		double newY = pos.getY() + 2;
    		
    		if(newY > 128)
    			newY = 128;
    		
    		pos.setY(newY);
    	}
    	else
    		pos = chip.getBlockPosition();
    	
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        
        int y = getSafeY(world, pos);
        
        for(Player player: etc.getServer().getPlayerList())
        {
        	Location pLoc = player.getLocation();
        	Vector pVec = new Vector(pLoc.x, pLoc.y, pLoc.z);
        	
        	if(player.getWorld() == world
        	   && (pVec.getBlockX() == x || pVec.getBlockX() == x + 1 || pVec.getBlockX() == x - 1)
        	   &&  pVec.getBlockY() == y
        	   && (pVec.getBlockZ() == z || pVec.getBlockZ() == z + 1 || pVec.getBlockZ() == z - 1)
        		)
        	{
        		if(useSafeY)
        			dest.y = getSafeY(CraftBook.getWorld(dest.dimension), new Vector(dest.x, dest.y, dest.z)) + 1.0;
                
                if(messages != null)
                {
                	for(String message : messages)
                	{
                		if(message == null)
                			break;
                		player.sendMessage(Colors.Gold+message);
                	}
                }
                
        		player.teleportTo(dest);
        		
        		if(chip.getMode() == 'P')
        		{
        			//force plate off
            		int bdata = CraftBook.getBlockID(world, pVec);
            		if(bdata == BlockType.STONE_PRESSURE_PLATE || bdata == BlockType.WOODEN_PRESSURE_PLATE)
            		{
            			OWorld oworld = player.getEntity().bb;
            			
            			int bx = pVec.getBlockX();
            			int by = pVec.getBlockY();
            			int bz = pVec.getBlockZ();
            			
            			oworld.c(bx, by, bz, 0);
            			oworld.h(bx, by, bz, bdata);
            			oworld.h(bx, by - 1, bz, bdata);
            			oworld.b(bx, by, bz, bx, by, bz);
            		}
        		}
        		
        		return true;
        	}
        }
    	
        return false;
    }
    
    private int getSafeY(World world, Vector pos)
    {
    	int maxY = Math.min(128, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
    	
    	for (int y = pos.getBlockY() + 1; y <= maxY; y++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)) &&
            	y < 128 && BlockType.canPassThrough(CraftBook.getBlockID(world, x, y+1, z))	
            	)
            {
            	return y;
            }
		}
    	
    	return maxY;
    }
}
