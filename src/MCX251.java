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


public class MCX251 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "SFX";
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
    	String line3 = sign.getLine3();
        String line4 = sign.getLine4().toUpperCase();
        
        if(line3.isEmpty())
        {
        	return "3rd line must contain Sound Effect type";
        }
        
        if(!line4.isEmpty())
        {
	        if (line4.length() < 2)
	            return "4th line format:  (Axis-X|Y|Z)(Distance#) EX: Y3";
	
	        if (line4.charAt(0) != 'X' && line4.charAt(0) != 'Y' && line4.charAt(0) != 'Z')
	            return "4th line format:  (Axis-X|Y|Z)(Distance#)     >Missing X, Y, or Z";
	
	        try
	        {
	            int dist = Integer.parseInt(line4.substring(1));
	            
	            if(dist < -9 || dist > 9)
	            	return "Distance must be a number from -9 to 9";
	            
	        }
	        catch (Exception e)
	        {
	            return "4th line format:  (Axis-X|Y|Z)(Distance#)     >Distance was not a number";
	        }
        }

        String[] sblock = line3.split("@",2);
        if(SFXType.getEffect(sblock[0]) == null)
        {
        	return "invalid Sound Effect";
        }
        
        if(sblock.length > 1)
        {
        	try
        	{
        		Integer.parseInt(sblock[1]);
        	}
        	catch(NumberFormatException e)
        	{
        		return "Sound Effect data must be a number.";
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
    public void think(ChipState chip) {
        chip.getOut(1).set(chip.getIn(1).is());
        
        if(!chip.getIn(1).isTriggered() || !chip.getIn(1).is())
        	return;
        
        String[] sblock = chip.getText().getLine3().split("@",2);
        SFXType sfxType = SFXType.getEffect(sblock[0]);
        
        if(sfxType == null || !sfxType.allowed)
        	return;

        Vector blockPos = chip.getBlockPosition();

        int x = blockPos.getBlockX();
        int y = blockPos.getBlockY();
        int z = blockPos.getBlockZ();
        
        String line4 = chip.getText().getLine4().toUpperCase();
        
        int dist = 1;
        if(line4.isEmpty())
        {
        	y += dist;
        }
        else
        {
        	dist = Integer.parseInt(line4.substring(1));
        	
        	if(line4.charAt(0) == 'X')
        		x += dist;
        	else if(line4.charAt(0) == 'Z')
        		z += dist;
        	else
        		y += dist;
        }
        
        int data = 0;
        if(sblock.length > 1)
        {
        	data = Integer.parseInt(sblock[1]);
        }
        else if(sfxType == SFXType.MUSIC)
        {
        	data = 2256;
        }
        
        etc.getMCServer().ab().a(x, y, z, 64.0D, chip.getCBWorld().dimension(), new OPacket61DoorChange(sfxType.getId(), x, y, z, data), chip.getCBWorld().name());
    }
}
