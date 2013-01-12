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

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sets the server time to day or night, repeats the signal.
 *
 * @author Shaun (sturmeh)
 */
public class MCX237 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "HIDE WEATHER";
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
    	if (sign.getLine3().length() != 0 &&
    			( sign.getLine3().charAt(1) != ':'
    				|| sign.getLine3().contains(" ")
    				)) {
            return "Incorrect format on line 3.";
        }
        
        if (sign.getLine4().length() != 0) {
            return "Fourth line needs to be blank";
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
    	
    	int type = 0;
    	String id = "";
    	if(chip.getText().getLine3().length() > 0 && chip.getText().getLine3().charAt(1) == ':')
    	{
    		switch(chip.getText().getLine3().charAt(0))
    		{
	    		case 'G':
				case 'g':
					type++;
    			case 'P':
    			case 'p':
    				type++;
    				id = chip.getText().getLine3().substring(2);
    				break;
    		}
    	}
    	
    	OPacket70GameEvent packet;
    	if(chip.getIn(1).is())
    		packet = new OPacket70GameEvent(2, 0);
    	else if(!CraftBook.getWorld(chip.getCBWorld()).isRaining())
    		return;
    	else
    		packet = new OPacket70GameEvent(1, 0);
    	
    	boolean out = chip.getIn(1).is();
    	switch(type)
    	{
    		case 0:
    		    Util.sendPacketToAllPlayers(packet);
    			break;
    		case 1:
    			Player player = etc.getServer().matchPlayer(id);
    	    	if(player != null)
    	    	    UtilEntity.sendPacket(player.getEntity(), packet);
    	    	else
    	    		out = false;
    	    	break;
    		case 2:
    			for(Player plyr: etc.getServer().getPlayerList())
    	        {
    				if(plyr.isInGroup(id))
    				    UtilEntity.sendPacket(plyr.getEntity(), packet);
    	        }
    			break;
    	}
        
        chip.getOut(1).set(out);
    }
}
