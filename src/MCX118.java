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



import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX118 extends BaseIC {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "PLAYER NEAR?";
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
        String id = sign.getLine3().toLowerCase();

        if (id.length() != 0)
        {
            if(id.length() < 3 || id.charAt(1) != ':' || (id.charAt(0) != 'g' && id.charAt(0) != 'p') )
            {
            	return "Invalid player or group name on 3rd line.";
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
    		dist *= dist;
    		Vector lever = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
    		World world = CraftBook.getWorld(chip.getCBWorld());
    		
        	NearbyEntityFinder nearbyFinder = new NearbyEntityFinder(world, chip.getBlockPosition(), lever, dist, chip.getText().getLine3(), 0, false);
        	etc.getServer().addToServerQueue(nearbyFinder);
    	}
    }
    
    public class NearbyEntityFinder implements Runnable
    {
    	private final World WORLD;
    	private final Vector BLOCK;
    	private final Vector LEVER;
    	private final double DISTANCE;
    	private final String SETTINGS;
    	private final int TYPE;
    	private final boolean DESTROY;
    	
    	public NearbyEntityFinder(World world, Vector block, Vector lever, double distance, String settings, int type, boolean destroy)
    	{
    		WORLD = world;
    		BLOCK = block;
    		LEVER = lever;
    		DISTANCE = distance;
    		SETTINGS = settings;
    		TYPE = type;
    		DESTROY = destroy;
    	}
    	
		@Override
		public void run()
		{
			@SuppressWarnings("rawtypes")
			List entities = null;
			
			try
			{
				switch(TYPE)
				{
					case 0:
						entities = etc.getServer().getPlayerList();
						break;
					case 1:
						entities = this.WORLD.getMobList();
						break;
					case 2:
						entities = this.WORLD.getAnimalList();
						break;
					case 3:
						entities = this.WORLD.getLivingEntityList();
						break;
					case 4:
						entities = entitiesExceptPlayers(this.WORLD.getWorld());
						break;
					case 5:
						entities = entitiesExceptPlayersItems(this.WORLD.getWorld());
						break;
				}
			}
			catch(ConcurrentModificationException e)
			{
				e.printStackTrace();
				return;
			}
			
			if(entities == null)
				return;
			
			boolean found = false;
			
			for(Object obj: entities)
			{
				BaseEntity entity = (BaseEntity)obj;
				if(entity.getWorld().getType().getId() != WORLD.getType().getId())
					continue;
				
				double diffX = BLOCK.getBlockX() - entity.getX();
				double diffY = BLOCK.getBlockY() - entity.getY();
				double diffZ = BLOCK.getBlockZ() - entity.getZ();
				
				if(diffX * diffX + diffY * diffY + diffZ * diffZ < DISTANCE)
				{
					boolean result = entityInRange(entity);
					if(result)
					{
						found = true;
						if(DESTROY)
						{
							entity.destroy();
						}
						else
						{
							break;
						}
					}
				}
			}
			
			Redstone.setOutput(CraftBook.getCBWorld(WORLD), LEVER, found);
		}
		
		private boolean entityInRange(BaseEntity entity)
	    {
			switch(TYPE)
			{
				case 0:
					Player player = (Player) entity;
			    	
			    	return SETTINGS.isEmpty()
			    			|| (SETTINGS.charAt(0) == 'g' && player.isInGroup(SETTINGS.substring(2)))
			    			|| (SETTINGS.charAt(0) == 'p' && player.getName().equalsIgnoreCase(SETTINGS.substring(2)));
				case 1:
				case 2:
					return true;
				case 3:
					if((entity.isMob() || entity.isAnimal()) && entity instanceof Mob)
					{
						Mob mob = (Mob) entity;
						if(SETTINGS.isEmpty() || mob.getName().equalsIgnoreCase(SETTINGS))
							return true;
					}
					break;
				case 4:
				case 5:
					return true;
			}
			
	    	return false;
	    }
		
		private List<BaseEntity> entitiesExceptPlayers(OWorld oworld)
		{
			List<BaseEntity> entities = new ArrayList<BaseEntity>();
			
			for(@SuppressWarnings("rawtypes")
    		Iterator it = oworld.e.iterator(); it.hasNext();)
    		{
    			Object obj = it.next();
    			if(!(obj instanceof OEntityPlayerMP))
    			{
    				entities.add(new BaseEntity((OEntity)obj));
    			}
    		}
			
			return entities;
		}
		
		private List<BaseEntity> entitiesExceptPlayersItems(OWorld oworld)
		{
			List<BaseEntity> entities = new ArrayList<BaseEntity>();
			
			for(@SuppressWarnings("rawtypes")
    		Iterator it = oworld.e.iterator(); it.hasNext();)
    		{
    			Object obj = it.next();
    			if(!(obj instanceof OEntityPlayerMP)
    				&& !(obj instanceof OEntityItem)
    				&& !(obj instanceof OEntityMinecart)
    				&& !(obj instanceof OEntityBoat)
    				&& !(obj instanceof OEntityEnderEye)
    				&& !(obj instanceof OEntityFishHook)
    				&& (!(obj instanceof OEntityWolf) || ((OEntityTameable)obj).o().isEmpty() )
    				&& (!(obj instanceof OEntityOcelot) || ((OEntityTameable)obj).o().isEmpty() )
    				)
    			{
    				entities.add(new BaseEntity((OEntity)obj));
    			}
    		}
			
			return entities;
		}
    }
}
