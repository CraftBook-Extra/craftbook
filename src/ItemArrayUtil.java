import com.sk89q.craftbook.BlockType;

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

/**
 * Inventory related functions.
 * 
 * @author sk89q
 */
public class ItemArrayUtil {
    /**
     * Move the contents of an inventory to a chest block bag.
     * 
     * @param minecart
     * @param bag
     */
    public static void moveItemArrayToChestBag(ItemArray<?> from,
            NearbyChestBlockBag bag) {
    	moveItemArrayToChestBag(from, bag, 0, -1, 0);
    }
	public static void moveItemArrayToChestBag(ItemArray<?> from,
            NearbyChestBlockBag bag, int itemType, int itemColor, int itemAmount) {
    	
        Item[] fromItems = from.getContents();
        Inventory[] inventories = bag.getInventories();
        int invenIndex = 0;
        boolean changed = false;
        
        int currentAmount = 0;
        
        try {
            for (int cartSlot = 0; cartSlot < fromItems.length; cartSlot++) {
                Item cartItem = fromItems[cartSlot];
                
                if (cartItem == null || cartItem.getAmount() == 0
                	|| (itemType > 0 && (itemType != cartItem.getItemId() || (itemColor != -1 && itemColor != cartItem.getDamage()) ) )
                	) {
                    continue;
                }
                
                currentAmount += cartItem.getAmount();
                Item itemCopy = null;
                
                if(itemAmount > 0 && currentAmount > itemAmount)
                {
                	itemCopy = new Item(cartItem.getItemId(),
                						currentAmount - itemAmount,
                						cartItem.getSlot(),
                						cartItem.getDamage());
                	
                	cartItem.setAmount(cartItem.getAmount() - itemCopy.getAmount());
                }
                
                try {
                    for (; invenIndex < inventories.length; invenIndex++) {
                        Item[] chestItems = inventories[invenIndex].getContents();
                        
                        for (int chestSlot = 0; chestSlot < chestItems.length; chestSlot++) {
                            Item chestItem = chestItems[chestSlot];
                            
                            if (chestItem == null) {
                                chestItems[chestSlot] = cartItem;
                                fromItems[cartSlot] = null;
                                setContents(inventories[invenIndex], chestItems);
                                changed = true;
                                throw new TransferredItemException();
                            } else {
                            	
                            	int maxStack = getStackMax(chestItem);
                            	
                            	if (chestItem.getItemId() == cartItem.getItemId()
                            			&& isSameColor(chestItem, cartItem)
                                        && chestItem.getAmount() < maxStack
                                        && chestItem.getAmount() >= 0)
                            	{
	                                int spaceAvailable = maxStack - chestItem.getAmount();
	                                
	                                if (spaceAvailable >= cartItem.getAmount()) {
	                                    chestItem.setAmount(chestItem.getAmount()
	                                            + cartItem.getAmount());
	                                    fromItems[cartSlot] = null;
	                                    setContents(inventories[invenIndex], chestItems);
	                                    changed = true;
	                                    throw new TransferredItemException();
	                                } else {
	                                    cartItem.setAmount(cartItem.getAmount()
	                                            - spaceAvailable);
	                                    chestItem.setAmount(maxStack);
	                                    changed = true;
	                                }
                            	}
                            }
                        }
                        
                        if(changed)
                        {
                        	setContents(inventories[invenIndex], chestItems);
                        }
                    }
                    
                    throw new TargetFullException();
                } catch (TransferredItemException e) {
                }
                
                if(itemAmount > 0 && currentAmount >= itemAmount)
                {
                	if(itemCopy != null)
                	{
                		if(fromItems[cartSlot] != null)
                		{
                			itemCopy.setAmount(itemCopy.getAmount() + fromItems[cartSlot].getAmount());
                		}
                		
                		fromItems[cartSlot] = itemCopy;
                	}
                	break;
                }
            }
        } catch (TargetFullException e) {
        }
        
        if (changed) {
            setContents(from, fromItems);
        }
    }

    /**
     * Move the contents of a chest block bag to an inventory.
     * 
     * @param to
     * @param bag
     */
    public static void moveChestBagToItemArray(ItemArray<?> to,
            NearbyChestBlockBag bag) {
    	
    	moveChestBagToItemArray(to, bag, 0, -1, 0);
    }
    
    /*
     * M4411K4: redoing code here because the last code from
     * I guess sk89q, didn't work properly and used more resources
     * than should have (using exceptions to break? o_O)
     */
    public static void moveChestBagToItemArray(ItemArray<?> to,
            NearbyChestBlockBag bag, int itemType, int itemColor, int itemAmount) {
    	
        Item[] toItems = to.getContents();
        Inventory[] bags = bag.getInventories();
        boolean changed = false;
        int currentAmount = 0;
        
        for(int toSlot = 0; toSlot < toItems.length; toSlot++)
        {
        	Item toItem = toItems[toSlot];
        	int maxStack = 0;
        	if(toItem != null)
        	{
        		maxStack = getStackMax(toItem);
        		if(toItem.getAmount() >= maxStack
        			|| (itemType > 0 && (itemType != toItem.getItemId() || (itemColor != -1 && itemColor != toItem.getDamage()) ))
        			)
        		{
        			continue;
        		}
        	}
        	
        	boolean moved = false;
        	
        	for(Inventory inventory : bags)
        	{
        		Item[] chestItems = inventory.getContents();
        		for(int chestSlot = 0; chestSlot < chestItems.length; chestSlot++)
        		{
        			Item chestItem = chestItems[chestSlot];
        			if(chestItem == null
        				|| chestItem.getAmount() == 0
        				|| (toItem != null && (chestItem.getItemId() != toItem.getItemId() || chestItem.getDamage() != toItem.getDamage()))
        				|| (itemType > 0 && (itemType != chestItem.getItemId() || (itemColor != -1 && itemColor != chestItem.getDamage())) )
        				)
        			{
        				//empty or not the same item so move on to next slot
        				continue;
        			}
        			
        			currentAmount += chestItem.getAmount();
        			Item itemCopy = null;
        			if(itemAmount > 0 && currentAmount > itemAmount)
        			{
        				itemCopy = new Item(chestItem.getItemId(),
				    						currentAmount - itemAmount,
				    						chestItem.getSlot(),
				    						chestItem.getDamage());
        				
        				chestItem.setAmount(chestItem.getAmount() - itemCopy.getAmount());
        			}
        			
        			//can move to slot
        			if(toItem == null)
        			{
        				toItems[toSlot] = chestItem;
        				chestItems[chestSlot] = null;
        			}
        			else
        			{
        				//maxStack should have correct value since toItem is not null
        				int spaceAvailable = maxStack - toItem.getAmount();
        				if(spaceAvailable >= chestItem.getAmount())
        				{
        					//everything fits
        					toItem.setAmount(toItem.getAmount() + chestItem.getAmount());
        					chestItems[chestSlot] = null;
        				}
        				else
        				{
        					//doesn't fit into slot
        					toItem.setAmount(maxStack);
        					chestItem.setAmount(chestItem.getAmount() - spaceAvailable);
        				}
        			}
        			
        			//if not max, re-check slot
        			maxStack = getStackMax(toItems[toSlot]);
        			if(toItems[toSlot].getAmount() < maxStack)
        			{
        				toSlot--;
        			}
        			
        			if(itemCopy != null)
                	{
                		if(chestItems[chestSlot] != null)
                		{
                			itemCopy.setAmount(itemCopy.getAmount() + chestItems[chestSlot].getAmount());
                		}
                		
                		chestItems[chestSlot] = itemCopy;
                	}
        			
        			moved = true;
        			changed = true;
        			break;
        		}
        		
        		if(moved || (itemAmount > 0 && currentAmount >= itemAmount))
        		{
        			//set chest items with new values
        			setContents(inventory, chestItems);
        			
        			//go to next item slot
        			break;
        		}
        	}
        	
        	if(itemAmount > 0 && currentAmount >= itemAmount)
        	{
        		break;
        	}
        }
        
        if(changed)
        {
        	setContents(to, toItems);
        }
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    public static void setContents(Inventory itemArray, Item[] contents) {
        int size = itemArray.getContentsSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i], i);
            }
        }
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    public static void setContents(ItemArray<?> itemArray, Item[] contents) {
        int size = itemArray.getContentsSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i], i);
            }
        }
    }
    
    /*
     * assumes item is valid
     */
    protected static int getStackMax(Item item)
    {
    	return OItem.e[item.getItemId()].k();
    }
    
    /*
     * assumes item1 and item2 id types have already been compared
     */
    private static boolean isSameColor(Item item1, Item item2)
    {
    	if(item1.getItemId() != BlockType.CLOTH &&
    		item1.getItemId() != BlockType.LOG &&
    		item1.getItemId() != 351 //dye
    		)
    	{
    		return true;
    	}
    	
    	return item1.getDamage() == item2.getDamage();
    }

    /**
     * Thrown when an item has been fully transferred.
     */
    private static class TransferredItemException extends Exception {
        private static final long serialVersionUID = -4125958007487924445L;
    }

    /**
     * Thrown when the target is full.
     */
    private static class TargetFullException extends Exception {
        private static final long serialVersionUID = 5408687817221722647L;
    }
}
