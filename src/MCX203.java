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

import com.sk89q.craftbook.BlockSourceException;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Chest Collector
 *
 * @author MK4411K4
 * @author Stefan Steinheimer (nosefish)
 */
public class MCX203 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "CHEST COLLECTOR";
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
        String id = sign.getLine3();

        if (id.length() > 0) {
        	String[] args = id.split(":", 2);
            int color = getColor(args);
            
            if(color >= 0)
            	id = args[0];
            else if(color == -2)
            	return "Not a valid color/damage value: " + args[1] + ".";
            
            if (getItem(id) < 1) {
                return "Not a valid item type: " + sign.getLine3() + ".";
            }
        }
        
        if (sign.getLine4().length() > 0)
        {
        	try
        	{
        		double dist = Double.parseDouble(sign.getLine4());
        		if(dist < 1.0D || dist > 64.0D)
        			return "4th line must be a number from 1 to 64.";
        	}
        	catch(NumberFormatException e)
        	{
        		return "4th line must be a number from 1 to 64.";
        	}
        	
        }

        return null;
    }
    
    /**
     * Get an item from its name or ID.
     * 
     * @param id
     * @return
     */
    protected int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }
    
    protected int getColor(String[] args)
    {
    	int color;
    	
    	if(args.length < 2)
    		return -1;
    	
    	try
    	{
    		color = Integer.parseInt(args[1]);
    	}
    	catch(NumberFormatException e)
    	{
    		return -2;
    	}
    	
    	if(color < 0 || color > 15)
    		return -2;
    	
    	return color;
    }
    

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        if (chip.inputAmount() != 0 && !chip.getIn(1).is()) {
            return;
        }
        
        NearbyChestBlockBag source = new NearbyChestBlockBag(chip.getCBWorld(), chip.getPosition());
        source.addSourcePosition(chip.getCBWorld(), chip.getPosition());
        
        String id = chip.getText().getLine3();
        
        int item = -1;
        int color = -1;
        if(id.length() > 0)
        {
	        String[] args = id.split(":", 2);
	        color = getColor(args);
	        
	        if(color >= 0)
	        	id = args[0];
	        else if(color < -1)
	        	color = -1;
	        
	        item = getItem(id);
        }
        // set distance
        double dist = 16.0D;
        if(chip.getText().getLine4().length() > 0)
        {
        	dist = Double.parseDouble(chip.getText().getLine4());
        }
        if(dist > RedstoneListener.chestCollectorMaxRange)
        	dist = RedstoneListener.chestCollectorMaxRange;
        
        CBXEntityFinder.ResultHandler chestCollector = new ItemChestCollector(chip, source);
        CBXEntityFinder itemFinder = new CBXEntityFinder(chip.getCBWorld(), chip.getPosition(), dist, chestCollector);
       	itemFinder.addItemFilter(item, color);
        if (!CraftBook.cbxScheduler.isShutdown()) {
        	try {
        		CraftBook.cbxScheduler.execute(itemFinder);
        	} catch (RejectedExecutionException e) {
        		// CraftBook is being disabled or reloaded
        	}
        }
    }
    
	/**
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
    private class ItemChestCollector implements CBXEntityFinder.ResultHandler{
    	final ChipState chip;
    	final NearbyChestBlockBag chest;
    	
    	public ItemChestCollector(ChipState chip,  NearbyChestBlockBag chest) {
    		this.chip = chip;
    		this.chest = chest;
    	}
    	
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					boolean itemCollected = false;
					for (BaseEntity bEntity : foundEntities) {
						if (bEntity == null || bEntity.isDead() || !(bEntity.getEntity() instanceof OEntityItem)) {
							continue;
						}
						ItemEntity itemEntity = new ItemEntity((OEntityItem) bEntity.getEntity());
						Item item = itemEntity.getItem();			
						if( item.getAmount() > 0 
							&& chest.hasAvailableSlotSpace(item.getItemId(), (byte)item.getDamage(), item.getAmount())) {
							try {
		                        chest.storeBlock(item.getItemId(), (byte)item.getDamage(), item.getAmount(), item.getEnchantments());
		                    } catch (BlockSourceException e) {
		                        break;
		                    }
							itemEntity.destroy();
							itemCollected = true;
						}
					}
			   		Vector lever = Util.getWallSignBack(chip.getCBWorld(), chip.getPosition(), 2);
			   		Redstone.setOutput(chip.getCBWorld(), lever, itemCollected);
				}
			});
			
		}
    }
    
//    public class ItemChestCollector implements Runnable
//    {
//    	private final World world;
//    	private final NearbyChestBlockBag source;
//    	private final double distance;
//    	private final int item;
//    	private final int color;
//    	private final double x;
//    	private final double y;
//    	private final double z;
//    	private final Vector lever;
//    	
//    	public ItemChestCollector(World world, NearbyChestBlockBag source, double distance, int item, int color, double x, double y, double z, Vector lever)
//    	{
//    		this.world = world;
//    		this.source = source;
//    		this.distance = distance;
//    		this.item = item;
//    		this.color = color;
//    		this.x = x;
//    		this.y = y;
//    		this.z = z;
//    		this.lever = lever;
//    	}
//    	
//		@Override
//		public void run()
//		{
//			try
//			{
//				List<ItemEntity> items = this.world.getItemList();
//				
//				if(items == null)
//		        	return;
//		        
//				//boolean found = false;
//		        for(ItemEntity itemEnt : items)
//		        {
//		        	Item citem = itemEnt.getItem();
//		        	
//		        	if(!itemEnt.isDead() && citem.getAmount() > 0 && (item == -1 || (citem.getItemId() == item && (color < 0 || citem.getDamage() == color) )))
//					{
//						double diffX = x - itemEnt.getX();
//						double diffY = y - itemEnt.getY();
//						double diffZ = z - itemEnt.getZ();
//						
//						if(((diffX * diffX + diffY * diffY + diffZ * diffZ) < distance)
//							&& source.hasAvailableSlotSpace(citem.getItemId(), (byte)citem.getDamage(), citem.getAmount()))
//						{
//							//found = true;
//
//							Redstone.setOutput(CraftBook.getCBWorld(world), lever, true);
//							
//							Enchantment[] enchants = itemEnt.getItem().getEnchantments();
//							
//							//kill
//							itemEnt.destroy();
//							
//							//store
//							try {
//		                        source.storeBlock(citem.getItemId(), (byte)citem.getDamage(), citem.getAmount(), enchants);
//		                    } catch (BlockSourceException e) {
//		                        break;
//		                    }
//						}
//					}
//		        }
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//			
//			// Toggle output off
//			Redstone.setOutput(CraftBook.getCBWorld(world), lever, false);
//		}
//    }
}
