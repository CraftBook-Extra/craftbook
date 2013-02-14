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

import java.util.Set;


import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX142 extends MCX140 {
	
	//private final String TITLE = "AREA PORT";
    /**
     * Get the title of the IC.
     *
     * @return
     */
	protected String settings = "";
	
	@Override
    public String getTitle() {
    	return "^"+settings;
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
    	
		sign = UtilIC.getSignTextWithExtension(cbworld, pos, sign);
		
    	if(!sign.getLine1().isEmpty())
    	{
    		if(sign.getLine1().charAt(0) != '@' || sign.getLine1().length() < 2)
    		{
    			return "Line 1 must start with @ or left blank.";
    		}
    		
    		String[] args = sign.getLine1().substring(1).split("\\+", 2);
    		if(!UtilEntity.isValidEntityTypeID(args[0]))
    		{
    			return "Invalid name on Line 1";
    		}
    		
    		if(args.length > 1 && !UtilEntity.isValidEntityTypeID(args[1]))
    		{
    			return "Invalid rider name on Line 1";
    		}
    		
    		settings = sign.getLine1().substring(1);
    	}
    	else
    	{
    		settings = "PLAYER";
    	}
    	
    	if(sign.getLine3().isEmpty())
    	{
    		return "Specify a band name on the third line.";
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "4th", 1,16, 1,16, 1,16,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
        return null;
    }
    
	public CBXEntityFinder.BaseEntityFilter beFilterFactory(ChipState chip) {
		//MCX142 has entity+rider on the first line
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	String[] args = text.getLine1().substring(1).split("\\+", 2);
    	String entityName = args[0];
    	String riderName = args.length > 1 ? args[1] : null;
    	return new FilterEntityAndRider(entityName, riderName);
	}
	
    @Override
    public ResultHandlerWithOutput rhFactory(ChipState chip) {
	   	String destinationID = chip.getText().getLine3();
    	if(!destinationID.isEmpty()) { 
	    	SignText text = UtilIC.getSignTextWithExtension(chip);
	    	destinationID = text.getLine3();
    	} else {
    		destinationID = null;
    	}
    	return new RHTeleport(chip, destinationID);
	}
    
    
	public static class RHTeleport extends RHSetOutIfFound {
		String destinationID;

		public RHTeleport(ChipState chip, String destinationID) {
			super(chip);
			this.destinationID = destinationID;
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					try {
						boolean found = false;
				    	WorldLocation tpDestination = MCX113.airwaves.get(destinationID);
				    	if (tpDestination != null) {
				        	tpDestination = tpDestination.add(0.5D, 0.0D, 0.5D);
				        	tpDestination = tpDestination.setY(Util.getSafeYAbove(CraftBook.getWorld(tpDestination.getCBWorld()), tpDestination.getCoordinate()) + 1.0D);
							for (BaseEntity bEntity : foundEntities) {
								if (bEntity == null || bEntity.isDead()) {
									continue;
								}
								found = true;
								if(bEntity.isPlayer()) {
									Player player = new Player((OEntityPlayerMP)bEntity.getEntity());
									CraftBook.teleportPlayer(player, tpDestination);
								}
								else {
									CraftBook.teleportEntity(bEntity, tpDestination);
								}
							}
				    	}
						setOutput(found);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
	}
	
	// old stuff ----------------------------------------------------------
	
    @Override
    @Deprecated
    protected void detectEntity(World world, Vector lever, BlockArea area, ChipState chip)
    {
    	String id = chip.getText().getLine3();
    	
    	if(id.isEmpty())
    		return;
    	
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	id = text.getLine3();
    	
    	WorldLocation dest = MCX113.airwaves.get(id);
    	if(dest == null)
    	{
    		Redstone.setOutput(CraftBook.getCBWorld(world), lever, false);
    		return;
    	}
    	
    	dest = dest.add(0.5D, 0.0D, 0.5D);
    	dest = dest.setY(MCX112.getSafeY(CraftBook.getWorld(dest.getCBWorld()), dest.getCoordinate()) + 1.0D);
    	
    	String[] args = text.getLine1().substring(1).split("\\+", 2);
        
        DetectEntityInArea detectEntity = new DetectEntityInArea(area, lever, args[0], args.length > 1 ? args[1] : null, dest, null);
        etc.getServer().addToServerQueue(detectEntity);
    }
}
