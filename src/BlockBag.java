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

/**
 * Represents a source to get blocks from and store removed ones.
 *
 * @author sk89q
 */
public abstract class BlockBag {
    /**
     * Stores a record of missing blocks.
     */
    private Map<Integer,Integer> missing = new HashMap<Integer,Integer>();
    
    /**
     * Sets a block.
     *
     * @param x
     * @param y
     * @param z
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(CraftBookWorld cbworld, int x, int y, int z, int id) throws BlockSourceException {
        return setBlockID(cbworld, new Vector(x, y, z), id);
    }
    
    public boolean setBlockID(CraftBookWorld cbworld, int x, int y, int z, int id, int data) throws BlockSourceException {
        return setBlockID(cbworld, new Vector(x, y, z), id, data);
    }

    /**
     * Sets a block.
     *
     * @param pos
     * @param id
     * @return
     * @throws OutOfSpaceException
     */
    public boolean setBlockID(CraftBookWorld cbworld, Vector pos, int id) throws BlockSourceException
    {
    	return setBlockID(cbworld, pos, id, 0);
    }
    
    public boolean setBlockID(CraftBookWorld cbworld, Vector pos, int id, int data) throws BlockSourceException
    {
    	return setBlockID(CraftBook.getWorld(cbworld), pos, id, data);
    }
    
    public boolean setBlockID(World world, int x, int y, int z, int id) throws BlockSourceException {
        return setBlockID(world, new Vector(x, y, z), id);
    }
    
    public boolean setBlockID(World world, int x, int y, int z, int id, int data) throws BlockSourceException {
        return setBlockID(world, new Vector(x, y, z), id, data);
    }
    
    public boolean setBlockID(World world, Vector pos, int id) throws BlockSourceException
    {
    	return setBlockID(world, pos, id, 0);
    }
    
    
    public boolean setBlockID(World world, Vector pos, int id, int data) throws BlockSourceException {
        if (id == 0) { // Clearing
            int existingID = CraftBook.getBlockID(world, pos);

            if (existingID != 0) {
                int dropped = BlockType.getDroppedBlock(existingID);
                if (dropped == -1) { // Bedrock, etc.
                    return false;
                } else if (dropped != 0) {
                    storeBlock(dropped, (byte)CraftBook.getBlockData(world, pos));
                }

                if(data >= 0)
                	return CraftBook.setBlockIdAndData(world, pos, id, data);
                return CraftBook.setBlockID(world, pos, id);
            }

            return false;
        } else { // Setting
            try {
                try {
                    int existingID = CraftBook.getBlockID(world, pos);
                    int existingData = CraftBook.getBlockData(world, pos);
                    
                    if (existingID != 0 && existingID != id && (data < 0 || existingData != data) ) {
                        int dropped = BlockType.getDroppedBlock(existingID);

                        // First store the existing block
                        if (dropped == -1) { // Bedrock, etc.
                            return false;
                        } else if (dropped != 0) {
                            storeBlock(dropped, (byte)existingData);
                        }

                        // Blocks that can't be fetched...
                        if (id == BlockType.BEDROCK
                                || id == BlockType.GOLD_ORE
                                || id == BlockType.IRON_ORE
                                || id == BlockType.COAL_ORE
                                || id == BlockType.DIAMOND_ORE
                                || id == BlockType.TALL_GRASS
                                || id == BlockType.DEAD_SHRUBS
                                || id == BlockType.TNT
                                || id == BlockType.MOB_SPAWNER
                                || id == BlockType.CROPS
                                || id == BlockType.REDSTONE_ORE
                                || id == BlockType.GLOWING_REDSTONE_ORE
                                || id == BlockType.LAPIS_LAZULI_ORE
                                || id == BlockType.SNOW
                                || id == BlockType.LIGHTSTONE
                                || id == BlockType.PORTAL
                                || id == BlockType.BED
                                || id == BlockType.CAKE_BLOCK
                                || id == BlockType.SILVERFISH_BLOCK
                                || id == BlockType.PUMPKIN_STEM
                                || id == BlockType.MELON_STEM
                                || id == BlockType.END_PORTAL
                                || id == BlockType.END_PORTAL_FRAME
                                || id == BlockType.COCOA_POD
                                || id == BlockType.EMERALD_ORE
                                || id == BlockType.CARROT
                                || id == BlockType.POTATOES
                                ) {
                            return false;
                        }

                        // Override liquids
                        if (id == BlockType.WATER
                                || id == BlockType.STATIONARY_WATER
                                || id == BlockType.LAVA
                                || id == BlockType.STATIONARY_LAVA) {
                            return CraftBook.setBlockIdAndData(world, pos, id, data);
                        }

                        fetchBlock(id, (byte)data);
                        return CraftBook.setBlockIdAndData(world, pos, id, data);
                    } else if (existingID == 0) {
                        fetchBlock(id, (byte)data);
                        return CraftBook.setBlockIdAndData(world, pos, id, data);
                    }
                } catch (OutOfBlocksException e) {
                    // Look for cobblestone
                    if (id == BlockType.STONE) {
                        fetchBlock(BlockType.COBBLESTONE);
                    // Look for dirt
                    } else if (id == BlockType.GRASS) {
                        fetchBlock(BlockType.DIRT);
                    // Look for redstone dust
                    } else if (id == BlockType.REDSTONE_WIRE) {
                        fetchBlock(331);
                     // Look for tripwire
                    } else if (id == BlockType.TRIPWIRE) {
                        fetchBlock(287);
                    // Look for furnace
                    } else if (id == BlockType.BURNING_FURNACE) {
                        fetchBlock(BlockType.FURNACE);
                    // Look for lit redstone torch
                    } else if (id == BlockType.REDSTONE_TORCH_OFF) {
                        fetchBlock(BlockType.REDSTONE_TORCH_ON);
                    // Look for redstone lamp
                    } else if (id == BlockType.REDSTONE_LAMP_ON) {
                        fetchBlock(BlockType.REDSTONE_LAMP_OFF);
                    // Look for signs
                    } else if (id == BlockType.WALL_SIGN || id == BlockType.SIGN_POST) {
                        fetchBlock(323);
                    // Look for lit redstone repeaters
                    } else if (id == BlockType.REDSTONE_REPEATER_OFF || id == BlockType.REDSTONE_REPEATER_ON) {
                        fetchBlock(356);
                    } else {
                        throw e;
                    }

                    return CraftBook.setBlockIdAndData(world, pos, id, data);
                }
            } catch (OutOfBlocksException e) {
                int missingID = e.getID();
                
                if (missing.containsKey(missingID)) {
                    missing.put(missingID, missing.get(missingID) + 1);
                } else {
                    missing.put(missingID, 1);
                }

                throw e;
            }

            return false;
        }
    }

    /**
     * Get a block.
     *
     * @param id
     */
    public abstract void fetchBlock(int id) throws BlockSourceException;
    public abstract void fetchBlock(int id, int data) throws BlockSourceException;
    
    /**
     * Store a block.
     * 
     * @param id
     */
    public abstract void storeBlock(int id) throws BlockSourceException;
    public abstract void storeBlock(int id, int data) throws BlockSourceException;
    
    /**
     * Checks to see if a block exists without removing it.
     * 
     * @param id
     * @return whether the block exists
     */
    public boolean peekBlock(int id) {
        try {
            fetchBlock(id);
            storeBlock(id);
            return true;
        } catch (BlockSourceException e) {
            return false;
        }
    }
    
    /**
     * Flush any changes. This is called at the end.
     */
    public abstract void flushChanges();

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public abstract void addSourcePosition(CraftBookWorld cbworld, Vector pos);
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public abstract void addSingleSourcePosition(CraftBookWorld cbworld, Vector pos);
    
    /**
     * Returns if the BlockBag has a real fetch or unlimited
     * 
     * @return
     */
    public abstract boolean hasRealFetch();
    
    public abstract boolean hasRealStore();

    /**
     * Return the list of missing blocks.
     * 
     * @return
     */
    public Map<Integer,Integer> getMissing() {
        return missing;
    }
}
