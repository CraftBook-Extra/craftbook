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

/**
 * 
 * AREA CBWARP
 *
 */
public class MCX144 extends MCX142 {
	
	//private final String TITLE = "AREA CBWARP";
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
    		return "Specify a CBWarp name on the third line.";
    	}
    	
    	CBWarpObject warp = CBWarp.getWarp(sign.getLine3(), false);
        if(warp == null)
        	return "CBWarp not found: "+sign.getLine3();
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "4th", 1,16, 1,16, 1,16,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
        return null;
    }
    
	
    @Override
    public ResultHandlerWithOutput rhFactory(ChipState chip) {
    	CBWarpObject warp = null;
    	if(! chip.getText().getLine3().isEmpty()) {
	    	SignText text = UtilIC.getSignTextWithExtension(chip);
	    	String destinationID = text.getLine3();
	    	warp = CBWarp.getWarp(destinationID, false);
    	}
    	return new RHTeleportCBWarp(chip, warp);
    	
	}
    
    
    
	public static class RHTeleportCBWarp extends RHSetOutIfFound {
		protected String[] messages = null;
		protected WorldLocation tpDestination = null;
		
		public RHTeleportCBWarp(ChipState chip, CBWarpObject cbWarp) {
			super(chip);
			if (cbWarp != null) {
				this.tpDestination = cbWarp.LOCATION;
				this.messages = cbWarp.getMessage();
			}
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					try {
						boolean found = false;
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
									if (messages != null) {
										for(String message : messages) {
											if(message == null)
												break;
											player.sendMessage(Colors.Gold+message);
										}
									}
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
	
	// old stuff ------------------------------------------------------------------------------
    @Override
    @Deprecated
    protected void detectEntity(World world, Vector lever, BlockArea area, ChipState chip)
    {
    	String id = chip.getText().getLine3();
    	
    	if(id.isEmpty())
    		return;
    	
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	id = text.getLine3();
    	CBWarpObject warp = CBWarp.getWarp(id, false);
    	
    	if(warp == null)
    		return;
    	
    	String[] args = text.getLine1().substring(1).split("\\+", 2);
        
        DetectEntityInArea detectEntity = new DetectEntityInArea(area, lever, args[0], args.length > 1 ? args[1] : null, warp.LOCATION, warp.getMessage());
        etc.getServer().addToServerQueue(detectEntity);
    }
}
