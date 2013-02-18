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

import java.util.List;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX131 extends BaseIC {
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "HIT PLAYER ABV";
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
    	String id = sign.getLine3().toLowerCase();

        if (id.length() != 0)
        {
            if(id.length() < 3 || id.charAt(1) != ':' || (id.charAt(0) != 'g' && id.charAt(0) != 'p') )
            {
            	return "Invalid player or group name on Third line.";
            }
        }
        
        if (sign.getLine4().isEmpty()) {
            sign.setLine4("1");
        }
        else
        {
        	try
        	{
        		int damage = Integer.parseInt(sign.getLine4());
        		if(damage > 20 || damage < 1)
        			return "4th line damage value must be a number from 1 to 20";
        	}
        	catch(NumberFormatException e)
        	{
        		return "4th line must be a number.";
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
    public void think(ChipState chip)
    {
    	if(!chip.getIn(1).is() || !chip.getIn(1).isTriggered())
    		return;
    	
    	boolean damaged = damagePlayers(CraftBook.getWorld(chip.getCBWorld()),
										chip.getBlockPosition().getBlockX(),
										chip.getBlockPosition().getBlockY(),
										chip.getBlockPosition().getBlockZ(),
										Integer.parseInt(chip.getText().getLine4()),
										chip.getText().getLine3().toLowerCase()
										);
    	
    	chip.getOut(1).set(damaged);
    }
    
    protected static boolean damagePlayers(World world, int x, int y, int z, int damage, String id)
    {
    	y = getSafeY(world, x, y, z);
    	
    	boolean damaged = false;
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
        		if(!id.isEmpty())
        		{
        			if( (id.charAt(0) == 'g' && player.isInGroup(id.substring(2))) ||
        					(id.charAt(0) == 'p' && player.getName().equalsIgnoreCase(id.substring(2))) )
        			{
        				player.getEntity().a(ODamageSource.j, damage);
        				damaged = true;
        			}
        		}
        		else
        		{
        			player.getEntity().a(ODamageSource.j, damage);
        			damaged = true;
        		}
        	}
        }
    	
    	return damaged;
    }
    
    @SuppressWarnings("rawtypes")
    protected static boolean damageEntities(List list, World world, int x, int y, int z, int damage, String id)
    {
    	y = getSafeY(world, x, y, z);
    	
    	boolean damaged = false;
    	boolean isNamed = !id.isEmpty()
    					&& !id.equalsIgnoreCase("animal") && !id.equalsIgnoreCase("animals")
    					&& !id.equalsIgnoreCase("mob") && !id.equalsIgnoreCase("mobs");
    	
    	for(Object obj: list)
        {
    		BaseEntity entity = (BaseEntity)obj;
    		if(entity != null 
    				&& entity.getWorld().isChunkLoaded((int)entity.getX(), (int)entity.getY(), (int)entity.getZ())
    				&& (entity.isMob() || entity.isAnimal()))
    		{
	        	Vector pVec = new Vector(entity.getX(), entity.getY(), entity.getZ());
	        	
	        	if(entity.getWorld() == world
	        	   && (pVec.getBlockX() == x || pVec.getBlockX() == x + 1 || pVec.getBlockX() == x - 1)
	        	   &&  pVec.getBlockY() == y
	        	   && (pVec.getBlockZ() == z || pVec.getBlockZ() == z + 1 || pVec.getBlockZ() == z - 1)
	        		)
	        	{
	        		if(isNamed)
	        		{
	        			//Mob mob = (Mob)entity;
	        			if(entity.getName().equalsIgnoreCase(id))
	        			{
	        				entity.getEntity().a(ODamageSource.j, damage);
	        				damaged = true;
	        			}
	        		}
	        		else
	        		{
	        			entity.getEntity().a(ODamageSource.j, damage);
	        			damaged = true;
	        		}
	        	}
    		}
        }
    	
    	return damaged;
    }
    
    private static int getSafeY(World world, int x, int y, int z)
    {
    	int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, y + 10);
    	
    	for (int safeY = y + 1; safeY <= maxY; safeY++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, safeY, z)) &&
            		safeY < CraftBook.MAP_BLOCK_HEIGHT && BlockType.canPassThrough(CraftBook.getBlockID(world, x, safeY+1, z))	
            	)
            {
            	return safeY;
            }
		}
    	
    	return maxY;
    }
}
