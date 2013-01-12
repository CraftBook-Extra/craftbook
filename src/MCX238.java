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

import java.util.Iterator;
import java.util.Map;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.WorldBlockVector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sets the server time to day or night, repeats the signal.
 *
 * @author Shaun (sturmeh)
 */
public class MCX238 extends BaseIC {
	
	/**
     * Data store.
     */
    protected static Map<Player,WorldBlockVector> players =
            new HistoryHashMap<Player,WorldBlockVector>(100);
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
    @Override
    public String getTitle() {
        return "DIST HIDE RAIN";
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

    @Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	if (sign.getLine3().length() == 0) {
    		return "Please put a distance on the third line.";
        }
    	
    	try
    	{
    		int dist = Integer.parseInt(sign.getLine3());
    		if(dist < 1 || dist > 127)
    			return "Distance must be a number from 1 to 127";
    	}
    	catch(NumberFormatException e)
    	{
    		return "Third line must be a number";
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
	    	Vector pos = chip.getBlockPosition();
	    	Vector cPos = chip.getPosition();
	    	int dist = Integer.parseInt(chip.getText().getLine3());
	    	dist = dist * dist;
	    	
	    	for(Player player: etc.getServer().getPlayerList())
	        {
	    		Location pLoc = player.getLocation();
	    		Vector diff = pos.subtract(pLoc.x, pLoc.y, pLoc.z);
	    		CraftBookWorld pcbworld = CraftBook.getCBWorld(player.getWorld());
	    		
	    		WorldBlockVector exists = players.get(player);
	    		
	    		if(!MCX236.isSameCoord(exists, chip.getCBWorld(), cPos) || MCX236.players.get(player) != null)
	    		{
	    			//not this IC or player already part of another IC
	    			continue;
	    		}
	    		
	    		if(pcbworld.equals(chip.getCBWorld())
	    		   && diff.getX() * diff.getX() + diff.getY() * diff.getY() + diff.getZ() * diff.getZ() < dist)
	    		{
	    			if(exists == null)
	    			{
	    				players.put(player, new WorldBlockVector(chip.getCBWorld(), cPos));
	    				UtilEntity.sendPacket(player.getEntity(), new OPacket70GameEvent(2, 0));
	    				if(chip.getText().getLine4().length() > 0)
	    					player.sendMessage(chip.getText().getLine4());
	    			}
	    		}
	    		else if(exists != null)
	    		{
	    			players.remove(player);
	    			if(CraftBook.getWorld(chip.getCBWorld()).isRaining())
	    			    UtilEntity.sendPacket(player.getEntity(), new OPacket70GameEvent(1, 0));
	    		}
	        }
    	}
    	else if(!chip.getIn(1).is())
    	{
    		Iterator<Map.Entry<Player, WorldBlockVector>> it = players.entrySet().iterator();
    		Vector cPos = chip.getPosition();
    	    while (it.hasNext())
    	    {
				Map.Entry<Player, WorldBlockVector> item = (Map.Entry<Player, WorldBlockVector>)it.next();
				if(!MCX236.isSameCoord(item.getValue(), chip.getCBWorld(), cPos))
					continue;
				if(CraftBook.getWorld(chip.getCBWorld()).isRaining())
				    UtilEntity.sendPacket(item.getKey().getEntity(), new OPacket70GameEvent(1, 0));
				it.remove();
    	    }
    	}
    }
}
