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



import java.util.concurrent.RejectedExecutionException;



import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Mob Nearby
 *
 * @author sk89q
 */
public class MCX119 extends CBXEntityFindingIC implements CBXEntityFindingIC.RHWithOutputFactory {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "MOB NEAR?";
    }
    
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
        String id = sign.getLine3();

        if (id.length() != 0)
        {
            if(!id.equalsIgnoreCase("mob") && !id.equalsIgnoreCase("mobs")
            	&& !id.equalsIgnoreCase("animal") && !id.equalsIgnoreCase("animals")
            	&& !Mob.isValid(id))
            {
            	return "Invalid mob name or type on 3rd line.";
            }
        }
        
        if (sign.getLine4().length() != 0) {
            try
            {
            	double dist = Double.parseDouble(sign.getLine4());
            	if(dist < 1.0D || dist > 64.0D)
            		return "Range must be a number from 1 to 64";
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
    public void think(ChipState chip) {
    	
    	if(chip.inputAmount() == 0 || (chip.getIn(1).is() && chip.getIn(1).isTriggered()) )
    	{
    		double dist = 5;
    		if(!chip.getText().getLine4().isEmpty()) {
    			dist = Double.parseDouble(chip.getText().getLine4());
    		}

       		Vector searchCenter = new Vector(chip.getBlockPosition().getX() + 0.5, chip.getBlockPosition().getY(), chip.getBlockPosition().getZ() + 0.5);
        	CBXEntityFinder entityFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, dist, rhFactory(chip));
        	addFilters(chip, entityFinder);

            if (!CraftBook.cbxScheduler.isShutdown()) {
            	try {
            		CraftBook.cbxScheduler.execute(entityFinder);
            	} catch (RejectedExecutionException e) {
            		// CraftBook is being disabled or reloaded
            	}
            }
    	}
    }
	
	protected void addFilters(ChipState chip, CBXEntityFinder entityFinder) {
		String mobType = chip.getText().getLine3();
		if(mobType.equalsIgnoreCase("mob") || mobType.equalsIgnoreCase("mobs")) {
			entityFinder.addMobFilter();
		} else if(mobType.equalsIgnoreCase("animal") || mobType.equalsIgnoreCase("animals")) {
			entityFinder.addAnimalFilter();
		} else if(Mob.isValid(mobType)) {
			entityFinder.addMobFilter(mobType);
			entityFinder.addAnimalFilter(mobType);
		} else {
			entityFinder.addMobFilter();
			entityFinder.addAnimalFilter();
		}
	}
	
	@Override
	public ResultHandlerWithOutput rhFactory(ChipState chip) {
		return new RHSetOutIfFound(chip);
	}
	
}
