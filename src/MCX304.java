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

import java.util.Iterator;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;


public class MCX304 extends MCX140 {
	
	//private final String TITLE = "REPEL FLOOR+";
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
    	
    	if(!sign.getLine3().isEmpty())
    	{
    		try
    		{
    			String[] params = sign.getLine3().split(":", 3);
    			double forcex = Double.parseDouble(params[0]);
    			if(forcex < -10 || forcex > 10)
    				return "3rd line force values must be a number from -10 to 10";
    			
    			if(params.length > 1)
    			{
    				double forcey = Double.parseDouble(params[1]);
    				if(forcey < -10 || forcey > 10)
    					return "3rd line force values must be a number from -10 to 10";
    				
    				if(params.length > 2)
    				{
    					double forcez = Double.parseDouble(params[2]);
        				if(forcez < -10 || forcez > 10)
        					return "3rd line force values must be a number from -10 to 10";
    				}
    				else
    				{
    					sign.setLine3(forcex+":"+forcey+":0");
    				}
    			}
    			else
				{
					sign.setLine3(forcex+":0:0");
				}
    		}
    		catch(NumberFormatException e)
    		{
    			return "3rd line must be numbers";
    		}
    	}
    	
    	if(!sign.getLine4().isEmpty())
    	{
    		String out = UtilIC.isValidDimensions(sign.getLine4(), "4th", 1,16, 1,16, 1,16,   -10,10, -10,10, -10,10);
    		if(out != null)
    			return out;
    	}
    	
        return null;
    }
	
	@Override
	protected int defaultHeight()
    {
    	return 4;
    }
    
    @Override
    protected void detectEntity(World world, Vector lever, BlockArea area, ChipState chip)
    {
    	SignText text = UtilIC.getSignTextWithExtension(chip);
    	String[] args = text.getLine1().substring(1).split("\\+", 2);
        
    	double[] applyForce = getForces(chip.getText());
    	
    	DetectEntityRepel detectEntity = new DetectEntityRepel(area, lever, args[0], args.length > 1 ? args[1] : null, applyForce);
        etc.getServer().addToServerQueue(detectEntity);
    }
    
    protected static double[] getForces(SignText text)
    {
    	if(!text.getLine3().isEmpty())
    	{
    		double[] forces = new double[3];
    		String[] params = text.getLine3().split(":",3);
    		forces[0] = Double.parseDouble(params[0]);
    		forces[1] = Double.parseDouble(params[1]);
    		forces[2] = Double.parseDouble(params[2]);
    		return forces;
    	}
    	return new double[]{0.0D, 2.0D, 0.0D};
    }
    
    public class DetectEntityRepel implements Runnable
    {
    	private final BlockArea AREA;
    	private final Vector LEVER;
    	private final String ENTITY_NAME;
    	private final String RIDER_NAME;
    	private final double[] FORCE;
    	
    	public DetectEntityRepel(BlockArea area, Vector lever, String entityName, String riderName, double[] force)
    	{
    		AREA = area;
    		LEVER = lever;
    		ENTITY_NAME = entityName;
    		RIDER_NAME = riderName;
    		FORCE = force;
    	}
    	
		@Override
		public void run()
		{
			boolean output = false;
			
			OWorldServer oworld = CraftBook.getOWorldServer(AREA.getCBWorld());
			for(@SuppressWarnings("rawtypes")
    		Iterator it = oworld.e.iterator(); it.hasNext();)
    		{
    			Object obj = it.next();
    			
    			if(!(obj instanceof OEntity))
    			{
    				//outdated?
    				return;
    			}
    			
    			BaseEntity entity = new BaseEntity((OEntity)obj);
    			
    			if(AREA.containsPoint(AREA.getCBWorld(),
										OMathHelper.c(entity.getX()),
										OMathHelper.c(entity.getY()),
										OMathHelper.c(entity.getZ()) )
    				&& UtilEntity.isValidEntity(entity, ENTITY_NAME)
    				&& (RIDER_NAME == null || RIDER_NAME.isEmpty()
    					|| (UtilEntity.riddenByEntity(entity.getEntity()) != null && UtilEntity.isValidEntity(new BaseEntity(UtilEntity.riddenByEntity(entity.getEntity())), RIDER_NAME)) )
    				)
    			{
    				output = true;
    				
    				if(FORCE != null)
					{
    					int maxICForce = Bounce.maxICForce;
						if(FORCE[0] > maxICForce)
							FORCE[0] = maxICForce;
						else if(FORCE[0] < -maxICForce)
							FORCE[0] = -maxICForce;
						if(FORCE[1] > maxICForce)
							FORCE[1] = maxICForce;
						else if(FORCE[1] < -maxICForce)
							FORCE[1] = -maxICForce;
						if(FORCE[2] > maxICForce)
							FORCE[2] = maxICForce;
						else if(FORCE[2] < -maxICForce)
							FORCE[2] = -maxICForce;
						
						CraftBookWorld cbworld = AREA.getCBWorld();
						OEntity oentity = entity.getEntity();
						oentity.w = FORCE[0];
						oentity.x = FORCE[1];
						oentity.y = FORCE[2];
						etc.getMCServer().ad().sendPacketToDimension(new OPacket28EntityVelocity(oentity), cbworld.name(), cbworld.dimension());
					}
    				
    				break;
    			}
    		}
			
			Redstone.setOutput(AREA.getCBWorld(), LEVER, output);
		}
    }
}
