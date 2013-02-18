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

import java.text.DecimalFormat;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Cannon - shoots fire balls.
 *
 * @author sk89q
 */
public class MCT246 extends MCX246 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "CANNON";
    }

	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	String out = super.validateEnvironment(cbworld, pos, sign);
    	
    	if(sign.getLine4().isEmpty())
    	{
    		sign.setLine4("0:0");
    	}
    	else
    	{
    		String[] args = sign.getLine4().split(":", 2);
    		if(args.length < 2)
    			sign.setLine4(args[0]+":0");
    	}
    	
    	return out;
    }
    
    /**
     * Think.
     * 
     * @param chip
     */
	@Override
    public void think(ChipState chip) {
        if (chip.getIn(1).is() && chip.getIn(1).isTriggered()) {
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
        else if(chip.getIn(2).is() && chip.getIn(2).isTriggered())
        {
        	String[] args = chip.getText().getLine4().split(":", 2);
        	DecimalFormat df = new DecimalFormat("#.#");
        	
        	if(getTopLeverState(chip))
        	{
        		double pitch = Float.parseFloat(args[1]);
        		pitch -= 0.1F;
        		if(pitch < -1.0F)
        			pitch = -1.0F;
        		args[1] = df.format(pitch);
        	}
        	else
        	{
        		double rotation = Float.parseFloat(args[0]);
        		rotation -= 1.0F;
        		if(rotation < -90.0F)
        			rotation = -90.0F;
        		args[0] = df.format(rotation);
        	}
        	chip.getText().setLine4(args[0]+":"+args[1]);
        	chip.getText().allowUpdate();
        	
        	//[HACK]:forces update since Sign.update() doesn't always work anymore due to Notch optimizing block updates
        	OWorldServer oworld = CraftBook.getOWorldServer(chip.getCBWorld());
        	oworld.h(chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
        }
        else if(chip.getIn(3).is() && chip.getIn(3).isTriggered())
        {
        	String[] args = chip.getText().getLine4().split(":", 2);
        	DecimalFormat df = new DecimalFormat("#.#");
        	
        	if(getTopLeverState(chip))
        	{
        		double pitch = Float.parseFloat(args[1]);
        		pitch += 0.1F;
        		if(pitch > 1.0F)
        			pitch = 1.0F;
        		args[1] = df.format(pitch);
        	}
        	else
        	{
        		double rotation = Float.parseFloat(args[0]);
        		rotation += 1.0F;
        		if(rotation > 90.0F)
        			rotation = 90.0F;
        		args[0] = df.format(rotation);
        	}
        	chip.getText().setLine4(args[0]+":"+args[1]);
        	chip.getText().allowUpdate();
        	
        	//[HACK]:forces update since Sign.update() doesn't always work anymore due to Notch optimizing block updates
        	OWorldServer oworld = CraftBook.getOWorldServer(chip.getCBWorld());
        	oworld.h(chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
        }
    }
    
    private boolean getTopLeverState(ChipState chip)
    {
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	int x = chip.getBlockPosition().getBlockX();
    	int y = chip.getBlockPosition().getBlockY() + 1;
    	int z = chip.getBlockPosition().getBlockZ();
    	
    	if(CraftBook.getBlockID(world, x, y, z) != BlockType.LEVER)
    		return false;
    	
    	int data = CraftBook.getBlockData(world, x, y, z);
    	return (data >> 3) == 0x1;
    }
}
