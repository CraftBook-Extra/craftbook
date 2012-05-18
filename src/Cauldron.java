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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Handler for cauldrons.
 *
 * @author sk89q
 */
public class Cauldron {
    /**
     * Stores the recipes.
     */
    private CauldronCookbook recipes;

    /**
     * Construct the handler.
     * 
     * @param recipes
     */
    public Cauldron(CauldronCookbook recipes) {
        this.recipes = recipes;
    }

    /**
     * Thrown when a suspected formation is not actually a valid cauldron.
     */
    private class NotACauldronException extends Exception {
        private static final long serialVersionUID = 3091428924893050849L;

        /**
         * Construct the exception with a message.
         * 
         * @param msg
         */
        public NotACauldronException(String msg) {
            super(msg);
        }
    }

    /**
     * Do cauldron.
     * 
     * @param pt
     * @param player
     */
    public void preCauldron(Vector pt, Player player) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        World world = player.getWorld();
        
        int rootY = y;
        int below = CraftBook.getBlockID(world, x, y - 1, z);
        int below2 = CraftBook.getBlockID(world, x, y - 2, z);
        int s1 = CraftBook.getBlockID(world, x + 1, y, z);
        int s3 = CraftBook.getBlockID(world, x - 1, y, z);
        int s2 = CraftBook.getBlockID(world, x, y, z + 1);
        int s4 = CraftBook.getBlockID(world, x, y, z - 1);

        // Preliminary check so we don't waste CPU cycles
        if ((BlockType.isLava(below) || BlockType.isLava(below2))
                && (s1 == BlockType.STONE || s2 == BlockType.STONE
                || s3 == BlockType.STONE || s4 == BlockType.STONE)) {
            // Cauldron is 2 units deep
            if (BlockType.isLava(below)) {
                rootY++;
            }

            performCauldron(new BlockVector(x, rootY, z), player);
        }
    }

    /**
     * Attempt to perform a cauldron recipe.
     * 
     * @param pt
     * @param player
     * @param recipes
     */
    private void performCauldron(BlockVector pt, Player player) {
        // Gotta start at a root Y then find our orientation
        int rootY = pt.getBlockY();

        // Used to store cauldron blocks -- walls are counted
        Map<BlockVector,CraftBookItem> visited = new HashMap<BlockVector,CraftBookItem>();

        World world = player.getWorld();
        
        try {
            // The following attempts to recursively find adjacent blocks so
            // that it can find all the blocks used within the cauldron
            findCauldronContents(world, pt, rootY - 1, rootY, visited);

            // We want cauldrons of a specific shape and size, and 24 is just
            // the right number of blocks that the cauldron we want takes up --
            // nice and cheap check
            if (visited.size() != 24) {
                throw new NotACauldronException("Cauldron is too small");
            }

            // Key is the block ID and the value is the amount
            Map<CraftBookItem,Integer> contents = new HashMap<CraftBookItem,Integer>();

            // Now we have to ignore stone blocks so that we get the real
            // contents of the cauldron
            for (Map.Entry<BlockVector,CraftBookItem> entry : visited.entrySet()) {
                if (entry.getValue().id() != BlockType.STONE) {
                    if (!contents.containsKey(entry.getValue())) {
                        contents.put(entry.getValue(), 1);
                    } else {
                        contents.put(entry.getValue(),
                                contents.get(entry.getValue()) + 1);
                    }
                }
            }

            // Find the recipe
            CauldronRecipe recipe = recipes.find(contents);

            if (recipe != null) {
                String[] groups = recipe.getGroups();
                
                if (groups != null) {
                    boolean found = false;
                    
                    for (String group : groups) {
                        if (player.isInGroup(group)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        player.sendMessage(Colors.Red + "Doesn't seem as if you have the ability...");
                        return;
                    }
                }
                
                player.sendMessage(Colors.Gold + "In a poof of smoke, you've made "
                        + recipe.getName() + ".");

                List<CraftBookItem> ingredients =
                        new ArrayList<CraftBookItem>(recipe.getIngredients());
                
                List<BlockVector> removeQueue = new ArrayList<BlockVector>();

                // Get rid of the blocks in world
                for (Map.Entry<BlockVector,CraftBookItem> entry : visited.entrySet()) {
                    // This is not a fast operation, but we should not have
                    // too many ingredients
                    if (ingredients.contains(entry.getValue())) {
                        // Some blocks need to removed first otherwise they will
                        // drop an item, so let's remove those first
                        if (!BlockType.isBottomDependentBlock(entry.getValue().id())) {
                            removeQueue.add(entry.getKey());
                        } else {
                            CraftBook.setBlockID(world, entry.getKey(), 0);
                        }
                        ingredients.remove(entry.getValue());
                    }
                }
                
                for (BlockVector v : removeQueue) {
                    CraftBook.setBlockID(world, v, 0);
                }

                // Give results
                resultItemLoop:
                for (CraftBookItem cbitem : recipe.getResults()) {
                	Item item = new Item(cbitem.id(), 1, -1, cbitem.color());
                	if(cbitem.hasEnchantments())
                	{
	                	for(int i = 0; i < cbitem.enchantments().length; i++)
	        			{
	        				CraftBookEnchantment cbenchant = cbitem.enchantment(i);
	        				
	        				//since this is from a server created recipe we can assume it is allowed
	        				//if(!cbenchant.enchantment().allowed)
	        					//continue;
	        				
	        				Enchantment enchant = new Enchantment(Enchantment.Type.fromId(cbenchant.enchantment().getId()), cbenchant.level());
	        				
	        				if(!enchant.isValid())
	        					continue;
	        				
	        				item.addEnchantment(enchant);
	        			}
	                	
	                	player.giveItem(item);
                	}
                	else
                	{
                		Inventory inv = player.getInventory();
                		int size = inv.getContentsSize();
                		size -= 4; //armor slots
                		for(int i = 0; i < size; i++)
                		{
                			Item invitem = inv.getItemFromSlot(i);
                			if(invitem != null
                				&& invitem.getItemId() == cbitem.id()
                				&& (cbitem.color() == -1 || invitem.getDamage() == cbitem.color())
                				&& invitem.getAmount() < ItemArrayUtil.getStackMax(invitem)
                				)
                			{
                				invitem.setAmount(invitem.getAmount() + 1);
                				continue resultItemLoop;
                			}
                		}
                		
                		//matching item not found, so give item as usual.
                		player.giveItem(item);
                	}
                }
                player.getInventory().update();
            // Didn't find a recipe
            } else {
                player.sendMessage(Colors.Red + "Hmm, this doesn't make anything...");
            }
        } catch (NotACauldronException e) {
        }
    }

    /**
     * Recursively expand the search area so we can define the number of
     * blocks that are in the cauldron. The search will not exceed 24 blocks
     * as no pot will ever use up that many blocks. The Y are bounded both
     * directions so we don't ever search the lava or anything above, although
     * in the case of non-wall blocks, we also make sure that there is standing
     * lava underneath.
     *
     * @param pt
     * @param minY
     * @param maxY
     * @param visited
     * @throws Cauldron.NotACauldronException
     */
    public void findCauldronContents(World world, BlockVector pt, int minY, int maxY,
            Map<BlockVector,CraftBookItem> visited) throws NotACauldronException {

        // Don't want to go too low or high
        if (pt.getBlockY() < minY) { return; }
        if (pt.getBlockY() > maxY) { return; }

        // There is likely a leak in the cauldron (or this isn't a cauldron)
        if (visited.size() > 24) {
            throw new NotACauldronException("Cauldron has a leak");
        }

        // Prevent infinite looping
        if (visited.containsKey(pt)) { return; }

        int type = CraftBook.getBlockID(world, pt);
        int data = CraftBook.getBlockData(world, pt);
        
        if(BlockType.isDirectionBlock(type))
        	data = 0;

        // Make water work reliably
        if (type == 9) {
            type = 8;
        }

        // Make lava work reliably
        if (type == 11) {
            type = 10;
        }
        
        visited.put(pt, new CraftBookItem(type, data));

        // It's a wall -- we only needed to remember that we visited it but
        // we don't need to recurse
        if (type == BlockType.STONE) { return; }

        // Must have a lava floor
        Vector lavaPos = pt.subtract(0, pt.getBlockY() - minY + 1, 0);
        if (!BlockType.isLava(CraftBook.getBlockID(world, lavaPos))) {
            throw new NotACauldronException("Cauldron lacks lava below");
        }

        // Now we recurse!
        findCauldronContents(world, pt.add(1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(-1, 0, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 0, 1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 0, -1).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, 1, 0).toBlockVector(), minY, maxY, visited);
        findCauldronContents(world, pt.add(0, -1, 0).toBlockVector(), minY, maxY, visited);
    }
}
