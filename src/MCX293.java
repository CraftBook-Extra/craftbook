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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * chest full?
 *
 * @author sk89q
 */
public class MCX293 extends BaseIC {
	
	private final String TITLE = "CHEST FULL?";
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "^"+TITLE;
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
		
		if(!sign.getLine3().isEmpty())
		{
			if(!sign.getLine3().equalsIgnoreCase("FULL") && !sign.getLine3().equalsIgnoreCase("EMPTY"))
			{
				try
				{
					String[] args = sign.getLine3().split(",");
					for(String arg : args)
					{
						String[] data = arg.split("\\*", 2);
						int slot = Integer.parseInt(data[0]);
						
						if(slot < 1)
						{
							return "Slot #'s must be a number from 1 and up: "+arg;
						}
						
						if(data.length > 1)
						{
							int amount = Integer.parseInt(data[1]);
							if(amount < 1)
							{
								return "Item amount must be a number from 1 and up: "+arg;
							}
						}
					}
				}
				catch(NumberFormatException e)
				{
					return "Slot value must be a number.";
				}
			}
		}

        if(!sign.getLine4().isEmpty())
    	{
    		String[] args = sign.getLine4().split(":", 3);
    		if(args.length != 3)
    		{
	    		return "Not a valid value on line 4. Must contain x,y,z chest location";
    		}
    		
    		try
    		{
    			int x = Integer.parseInt(args[0]);
    			int y = Integer.parseInt(args[1]);
    			int z = Integer.parseInt(args[2]);
    			
    			if(x < -10 || x > 10)
    				return "X must be a value from -10 to 10";
    			if(y < -CraftBook.MAP_BLOCK_HEIGHT + 1 || y > CraftBook.MAP_BLOCK_HEIGHT - 1)
    				return "Y must be a value from "+(-CraftBook.MAP_BLOCK_HEIGHT + 1)+" to "+(CraftBook.MAP_BLOCK_HEIGHT - 1);
    			if(z < -10 || z > 10)
    				return "Z must be a value from -10 to 10";
    		}
    		catch(NumberFormatException e)
    		{
    			return "4th line location value must be a number.";
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
    	
    	if(chip.inputAmount() == 0
    		|| (chip.getText().getLine2().charAt(3) == 'X' && chip.getIn(1).isTriggered() && chip.getIn(1).is())
    		)
    	{
    		SignText text = UtilIC.getSignTextWithExtension(chip);
    		String line3 = text.getLine3();
    		
    		Inventory[] chests;
    		
    		if(text.getLine4().isEmpty())
        	{
    			NearbyChestBlockBag source = new NearbyChestBlockBag(chip.getCBWorld(), chip.getPosition());
    	        source.addSourcePosition(chip.getCBWorld(), chip.getPosition());
    			
    	        chests = source.getInventories();
        	}
    		else
    		{
    			String[] args = text.getLine4().split(":", 3);
    			
    			int x = Integer.parseInt(args[0]) + chip.getBlockPosition().getBlockX();
    			int y = Integer.parseInt(args[1]) + chip.getBlockPosition().getBlockY();
    			int z = Integer.parseInt(args[2]) + chip.getBlockPosition().getBlockZ();
    			
    			World world = CraftBook.getWorld(chip.getCBWorld());
    			ComplexBlock complexBlock = world.getComplexBlock(x, y, z);
    			Inventory chest;
    			
    			if(complexBlock instanceof Chest)
    			{
    				chest = (Inventory)complexBlock;
    			}
    			else if(complexBlock instanceof DoubleChest)
    			{
    				chest = (Inventory)complexBlock;
    			}
    			else
    			{
    				chip.getOut(1).set(false);
    				return;
    			}
    			
    			chests = new Inventory[]{chest};
    		}
    		
    		if(chests == null || chests.length < 1)
    		{
    			return;
    		}
    		
    		if(line3.isEmpty() || line3.equalsIgnoreCase("FULL"))
			{
				for(Inventory chest : chests)
				{
					if(chest == null)
					{
						continue;
					}
					
					if(chest.getEmptySlot() > -1)
					{
						//not full
						chip.getOut(1).set(false);
						return;
					}
				}
			}
			else if(line3.equalsIgnoreCase("EMPTY"))
			{
				for(Inventory chest : chests)
				{
					if(chest == null)
					{
						continue;
					}
					
					Item[] items = chest.getContents();
					for(Item item : items)
					{
						if(item != null)
						{
							//not empty
							chip.getOut(1).set(false);
							return;
						}
					}
				}
			}
			else
			{
				String[] slots = line3.split(",");
				
				for(Inventory chest : chests)
				{
					if(chest == null)
					{
						continue;
					}
					
					for(String arg : slots)
					{
						String[] data = arg.split("\\*", 2);
						
						int slot = Integer.parseInt(data[0]);
						if(slot < 1)
							return;
						
						slot--;
						Item item = chest.getItemFromSlot(slot);
						
						if(item == null)
						{
							//does not have an item in the slot
							chip.getOut(1).set(false);
							return;
						}
						else if(data.length > 1)
						{
							int amount = Integer.parseInt(data[1]);
							if(item.getAmount() < amount)
							{
								//does not have enough in the slot
								chip.getOut(1).set(false);
								return;
							}
						}
					}
				}
			}
    		
    		chip.getOut(1).set(true);
    	}
    	else if(chip.getIn(1).isTriggered())
    	{
    		if(chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '%')
    		{
    			chip.getText().setLine1("%"+TITLE);
    			chip.getText().supressUpdate();
    			
    			RedstoneListener redListener = (RedstoneListener) chip.getExtra();
    			redListener.onSignAdded(CraftBook.getWorld(chip.getCBWorld()), chip.getPosition().getBlockX(), chip.getPosition().getBlockY(), chip.getPosition().getBlockZ());
    		}
    		else if(!chip.getIn(1).is() && chip.getText().getLine1().charAt(0) != '^')
    		{
    			chip.getText().setLine1("^"+TITLE);
    			chip.getText().supressUpdate();
    		}
    	}
    }
}
