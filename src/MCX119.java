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



import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Mob Nearby
 *
 * @author sk89q
 */
public class MCX119 extends BaseIC {
    

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
    		if(!chip.getText().getLine4().isEmpty())
    			dist = Double.parseDouble(chip.getText().getLine4());

    		Vector lever = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
    		
       		Vector searchCenter = new Vector(chip.getBlockPosition().getX() + 0.5, chip.getBlockPosition().getY(), chip.getBlockPosition().getZ() + 0.5);
        	CBXEntityFinder.ResultHandler resultHandler = resultHandlerFactory(chip.getCBWorld(), lever);
        	CBXEntityFinder entityFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, dist, resultHandler);
        	
    		String id = chip.getText().getLine3();
    		if(id.equalsIgnoreCase("mob") || id.equalsIgnoreCase("mobs")) {
    			entityFinder.addMobFilter();
    		} else if(id.equalsIgnoreCase("animal") || id.equalsIgnoreCase("animals")) {
    			entityFinder.addAnimalFilter();
    		} else if(Mob.isValid(id)) {
				entityFinder.addMobFilter(id);
				entityFinder.addAnimalFilter(id);
    		} else {
    			entityFinder.addMobFilter();
    			entityFinder.addAnimalFilter();
    		}
            if (!CraftBook.cbxScheduler.isShutdown()) {
            	try {
            		CraftBook.cbxScheduler.execute(entityFinder);
            	} catch (RejectedExecutionException e) {
            		// CraftBook is being disabled or reloaded
            	}
            }
    	}
    }
	
	
	protected CBXEntityFinder.ResultHandler resultHandlerFactory(CraftBookWorld cbworld, Vector lever) {
		return new MCX119.ResultHandler(cbworld, lever);
	}
	
	public static class ResultHandler implements CBXEntityFinder.ResultHandler {
		protected final CraftBookWorld cbworld;
		protected final Vector lever;
		
		public ResultHandler(CraftBookWorld cbworld, Vector lever) {
			this.cbworld = cbworld;
			this.lever = lever;
		}
		@Override
		public void handleResult(Set<BaseEntity> foundEntities) {
			final boolean found;
			if (foundEntities.isEmpty()) {
				found = false;
			} else {
				found = true;
			}
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					Redstone.setOutput(cbworld, lever, found);
				}
			});
		}
	}
}
