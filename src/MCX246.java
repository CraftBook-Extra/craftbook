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
 * Shoots fireballs.
 *
 * @author sk89q
 */
public class MCX246 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "FIREBALL";
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
        String dest = sign.getLine3();
        String settings = sign.getLine4();

        try
        {
        	if(!dest.isEmpty())
        	{
        		String[] args = dest.split(":", 2);
        		
        		double speed = Double.parseDouble(args[0]);
        		if(speed > 5D || speed <= 0D)
        			return "Speed must be a number between 0 and 5";
        		
        		if(args.length > 1)
        		{
        			float power = Float.parseFloat(args[1]);
        			if(power > 10 || power < 0.1)
        				return "Power must be a number from 0.1 to 10";
        		}
        	}
        }
        catch(NumberFormatException e)
        {
        	return "3rd line must be numbers "+e;
        }
        
        try
        {
        	if(!settings.isEmpty())
        	{
        		String[] args = settings.split(":", 2);
        		float rotation = Float.parseFloat(args[0]);
        		
        		if(rotation > 90 || rotation < -90)
        			return "rotation must be a number from -90 to 90";
        		
        		if(args.length > 1)
        		{
        			float pitch = Float.parseFloat(args[1]);
        			
        			if(pitch > 1 || pitch < -1)
        				return "pitch must be a number from -1 to 1";
        		}
        			
        	}
        }
        catch(NumberFormatException e)
        {
        	return "4th line must be numbers";
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
        if (chip.getIn(1).is()) {
            String dest = chip.getText().getLine3();
            String settings = chip.getText().getLine4();
            float power = 1.0F;
            double speed = 0.2D;
            float rotation = 0F;
            float pitch = 0F;

            try {
            	if (!dest.isEmpty()) {
            		String[] args = dest.split(":", 2);
            		
            		speed = Double.parseDouble(args[0]);
            		if(args.length > 1)
            			power = Float.parseFloat(args[1]);
                }

                if (!settings.isEmpty()) {
                	String[] args = settings.split(":", 2);
                	
                	rotation = Float.parseFloat(args[0]);
                	if(args.length > 1)
                		pitch = Float.parseFloat(args[1]);
                }
            } catch (NumberFormatException e) {
            }
            
            shoot(chip, power, speed, rotation, pitch);
        }
    }
    
    /**
     * Shoot the arrow.
     * 
     * @param chip
     * @param speed
     * @param spread
     * @param vertVel
     */
    protected void shoot(ChipState chip, float power, double speed, float rotation, float pitch) {
    	shootFireball(chip, power, speed, rotation, pitch, 0, 0, 0);
    }
    protected void shootFireball(ChipState chip, float power, double speed, float rotation, float pitch, int offsetx, int offsety, int offsetz)
    {
    	Vector start = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
    	if (start == null) return;
    	double x = start.getBlockX() + offsetx + 0.5D;
    	double y = start.getBlockY() + offsety;
    	double z = start.getBlockZ() + offsetz + 0.5D;
    	rotation += Util.getWallSignRotation(chip.getCBWorld(), chip.getPosition()); 
    	pitch *= 90;
    	
        OWorldServer oworld = CraftBook.getOWorldServer(chip.getCBWorld());
        OEntityFireball fireball = new CBFireball(oworld, x, y, z, rotation, pitch, power, speed);
		oworld.d(fireball);
    }
}
