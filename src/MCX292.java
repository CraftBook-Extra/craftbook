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
 * Dispenser.
 *
 * @author sk89q
 */
public class MCX292 extends BaseIC {
	
	private final String TITLE = "CHEST HAS ITEM";
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
		
		if(sign.getLine3().isEmpty())
		{
			return "Specify a item type on the third line.";
		}
		
        String[] ids = sign.getLine3().split(",");
        
        for(String id : ids)
        {
        	String[] args = id.split("=", 2);
        	String[] data;
        	if(args.length > 1)
        	{
        		data = args[1].split("\\*", 2);
        		try
        		{
        			Integer.parseInt(args[0]);
        		}
        		catch(NumberFormatException e)
        		{
        			return "Not a valid slot #: " + id;
        		}
        	}
        	else
        	{
        		data = args[0].split("\\*", 2);
        	}
        	
	        CraftBookItem cbitem = UtilItem.parseCBItem(data[0], false);
	        
	        if (cbitem == null || cbitem.id() < 1)
	        {
	            return "Not a valid item: " + id;
	        }
	        
	        if(data.length > 1)
	        {
	        	try
	        	{
	        		int amount = Integer.parseInt(data[1]);
	        		if(amount < 1)
	        		{
	        			return "Item amount must be 1 or larger: " + id;
	        		}
	        	}
	        	catch(NumberFormatException e)
	        	{
	        		return "Not a valid item amount: " + id;
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
    		
    		String[] ids = text.getLine3().split(",");
    		
    		boolean hasItems;
    		
    		if(text.getLine4().isEmpty())
        	{
    			hasItems = findInNearbyChestSource(chip, ids);
        	}
    		else
    		{
    			String[] args = text.getLine4().split(":", 3);
    			
    			int x = Integer.parseInt(args[0]) + chip.getBlockPosition().getBlockX();
    			int y = Integer.parseInt(args[1]) + chip.getBlockPosition().getBlockY();
    			int z = Integer.parseInt(args[2]) + chip.getBlockPosition().getBlockZ();
    			
    			hasItems = findAtChestLocation(chip, ids, x, y, z);
    		}
    		
    		chip.getOut(1).set(hasItems);
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
    
    private boolean findInNearbyChestSource(ChipState chip, String[] ids)
    {
    	NearbyChestBlockBag source = new NearbyChestBlockBag(chip.getCBWorld(), chip.getPosition());
        source.addSourcePosition(chip.getCBWorld(), chip.getPosition());
        
        for(String id : ids)
        {
        	String[] args = id.split("=", 2);
        	String[] data;
        	int slot = -1;
        	if(args.length > 1)
        	{
        		data = args[1].split("\\*", 2);
        		slot = Integer.parseInt(args[0]);
        		if(slot < 1)
        			slot = -1;
        		else
        			slot--;
        	}
        	else
        	{
        		data = args[0].split("\\*", 2);
        	}
        	
	        CraftBookItem cbitem = UtilItem.parseCBItem(data[0], false);
	        
	        if(cbitem == null || cbitem.id() < 1)
	        {
	        	return false;
	        }
	        else
	        {
	        	int amount = 1;
	            if(data.length > 1)
	            {
	            	amount = Integer.parseInt(data[1]);
	            }
	            
	            if(slot < 0)
	            {
	            	if(!source.hasItems(cbitem, amount))
		            {
		            	return false;
		            }
	            }
	            else
	            {
		            if(!source.hasItemAtSlot(cbitem, amount, slot))
		            {
		            	return false;
		            }
	            }
	        }
        }
        
        return true;
    }
    
    private boolean findAtChestLocation(ChipState chip, String[] ids, int x, int y, int z)
    {
    	if(CraftBook.getBlockID(chip.getCBWorld(), x, y, z) == BlockType.CHEST)
		{
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
				return false;
			}
			
			for(String id : ids)
            {
				String[] args = id.split("=", 2);
	        	String[] data;
	        	int slot = -1;
	        	if(args.length > 1)
	        	{
	        		data = args[1].split("\\*", 2);
	        		slot = Integer.parseInt(args[0]);
	        		if(slot < 1)
	        			slot = -1;
	        		else
	        			slot--;
	        	}
	        	else
	        	{
	        		data = args[0].split("\\*", 2);
	        	}
	        	
    	        CraftBookItem cbitem = UtilItem.parseCBItem(data[0], false);
    	        
    	        if(cbitem == null || cbitem.id() < 1)
    	        {
    	        	return false;
    	        }
    	        else
    	        {
    	        	int amount = 1;
    	            if(data.length > 1)
    	            {
    	            	amount = Integer.parseInt(data[1]);
    	            	if(amount < 1)
    	            		amount = 1;
    	            }
    	            
    	            if(slot < 0)
    	            {
    	            	Item[] items = chest.getContents();
    	            	int count = 0;
    	            	
	    	            for(Item item : items)
	    	            {
	    	            	if(item != null)
	    	            	{
	    	            		if(item.getItemId() == cbitem.id()
	    	            			&& (cbitem.color() == -1 || item.getDamage() == cbitem.color())
	    	            			&& item.getAmount() >= 1
	    	            			&& (!cbitem.hasEnchantments() || UtilItem.enchantsAreEqual(item.getEnchantments(), cbitem.enchantments()))
	    	            			)
	    	            		{
	    	            			count += item.getAmount();
	    	            			if(count >= amount)
	    	            			{
	    	            				break;
	    	            			}
	    	            		}
	    	            	}
	    	            }
	    	            
	    	            if(count < amount)
	    	            {
	    	            	return false;
	    	            }
    	            }
    	            else
    	            {
    	            	Item item = chest.getItemFromSlot(slot);
    	            	if(item != null)
    	            	{
    	            		if(item.getItemId() == cbitem.id()
    	            			&& (cbitem.color() == -1 || item.getDamage() == cbitem.color())
    	            			&& item.getAmount() >= amount
    	            			&& (!cbitem.hasEnchantments() || UtilItem.enchantsAreEqual(item.getEnchantments(), cbitem.enchantments()))
    	            			)
    	            		{
    	            			return true;
    	            		}
    	            	}
    	            	
    	            	return false;
    	            }
    	            
    	        }
            }
		}
		else
		{
			return false;
		}
    	
    	return true;
    }
}
