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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.InsufficientArgumentsException;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.WorldBlockVector;
import com.sk89q.craftbook.WorldLocation;

/**
 * Library for Minecraft-related functions.
 * 
 * @author sk89q
 */
public class Util {

    protected static void sendPacketToDimension(OPacket opacket, CraftBookWorld cbworld)
    {
        sendPacketToDimension(opacket, cbworld.name(), cbworld.dimension());
    }
    
    protected static void sendPacketToDimension(OPacket opacket, String worldName, int dimension)
    {
    	// Notchian: MinecraftServer.getConfigurationManager, func_71203_ab
        etc.getMCServer().af().sendPacketToDimension(opacket, worldName, dimension);
    }
    
    protected static void sendPacketToPlayersAroundPoint(CraftBookWorld cbworld, double x, double y, double z, double distance, OPacket opacket)
    {
        sendPacketToPlayersAroundPoint(cbworld.name(), cbworld.dimension(), x, y, z, distance, opacket);
    }
    
    protected static void sendPacketToPlayersAroundPoint(String worldName, int dimension, double x, double y, double z, double distance, OPacket opacket)
    {
        etc.getMCServer().af().a(x, y, z, distance, dimension, opacket, worldName);
    }
    
    protected static void sendPacketToAllPlayers(OPacket opacket)
    {
        etc.getMCServer().af().a(opacket);
    }
    
    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
	public static Vector getWallSignBack(CraftBookWorld cbworld, Vector pt, int multiplier) {
		return getWallSignBack(CraftBook.getWorld(cbworld), pt, multiplier);
	}
    public static Vector getWallSignBack(World world, Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        if (!world.isChunkLoaded(x, 0, z)) return null;
        int data = CraftBook.getBlockData(world, x, y, z);
        if (data == 0x2) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x3) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else {
            return new Vector(x - multiplier, y, z);
        }
    }
    public static Vector getWallSignBack(CraftBookWorld cbworld, Vector pt, double multiplier) {
		return getWallSignBack(CraftBook.getWorld(cbworld), pt, multiplier);
	}
    public static Vector getWallSignBack(World world, Vector pt, double multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        if (!world.isChunkLoaded(x, 0, z)) return null;
        int data = CraftBook.getBlockData(world, x, y, z);
        if (data == 0x2) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x3) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else {
            return new Vector(x - multiplier, y, z);
        }
    }

    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
    public static Vector getSignPostOrthogonalBack(CraftBookWorld cbworld, Vector pt, int multiplier) {
    	return getSignPostOrthogonalBack(CraftBook.getWorld(cbworld), pt, multiplier);
    }
    public static Vector getSignPostOrthogonalBack(World world, Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = CraftBook.getBlockData(world, x, y, z);
        if (data == 0x8) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x0) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else if (data == 0xC) { // South
            return new Vector(x - multiplier, y, z);
        } else {
            return null;
        }
    }

    /**
     * Gets the block next to a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
    public static Vector getWallSignSide(CraftBookWorld cbworld, Vector pt, int multiplier) {
    	return getWallSignSide(CraftBook.getWorld(cbworld), pt, multiplier);
    }
    public static Vector getWallSignSide(World world, Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        if (!world.isChunkLoaded(x, y, z)) return null;
        int data = CraftBook.getBlockData(world, x, y, z);
        if (data == 0x2) { // East
            return new Vector(x + multiplier, y, z );
        } else if (data == 0x3) { // West
            return new Vector(x - multiplier, y, z);
        } else if (data == 0x4) { // North
            return new Vector(x, y, z - multiplier);
        } else {
            return new Vector(x, y, z + multiplier);
        }
    }

    /**
     * Checks whether a sign at a location has a certain text on a
     * particular line, case in-sensitive.
     * 
     * @param pt
     * @param lineNo
     * @param text
     * @return
     */
    public static boolean doesSignSay(CraftBookWorld cbworld, Vector pt, int lineNo, String text) {
    	return doesSignSay(CraftBook.getWorld(cbworld), pt, lineNo, text);
    }
    public static boolean doesSignSay(World world, Vector pt, int lineNo, String text) {
        ComplexBlock cBlock = world.getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    
        if (cBlock instanceof Sign) {
            Sign sign = (Sign)cBlock;
            return text.equalsIgnoreCase(sign.getText(lineNo));
        }
    
        return false;
    }
    
    /**
     * Checks if a wall sign is next to the input location point
     * Does not check if wall sign is attached to the point
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Sign getWallSignNextTo(CraftBookWorld cbworld, int x, int y, int z)
    {
    	return getWallSignNextTo(CraftBook.getWorld(cbworld), x, y, z);
    }
    public static Sign getWallSignNextTo(World world, int x, int y, int z)
    {
    	ComplexBlock cBlock = null;
    	if (CraftBook.getBlockID(world, x+1, y, z) == BlockType.WALL_SIGN)
    		cBlock = world.getComplexBlock(x+1, y, z);
    	else if (CraftBook.getBlockID(world, x-1, y, z) == BlockType.WALL_SIGN)
    		cBlock = world.getComplexBlock(x-1, y, z);
    	else if (CraftBook.getBlockID(world, x, y, z+1) == BlockType.WALL_SIGN)
    		cBlock = world.getComplexBlock(x, y, z+1);
    	else if (CraftBook.getBlockID(world, x, y, z-1) == BlockType.WALL_SIGN)
    		cBlock = world.getComplexBlock(x, y, z-1);
    	
    	if(cBlock != null && cBlock instanceof Sign)
    	{
    		return (Sign)cBlock;
    	}
    	
    	return null;
    }
    
    /**
     * Gets the rotation of the wall sign
     * 
     * @param worldType
     * @param pt
     * @return
     */
    public static int getWallSignRotation(CraftBookWorld cbworld, Vector pt)
    {
    	return getWallSignRotation(CraftBook.getWorld(cbworld), pt);
    }
    
    public static int getWallSignRotation(World world, Vector pt)
    {
    	int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
    	
    	int data = CraftBook.getBlockData(world, x, y, z);
        if (data == 0x2) { // East
            return 90;
        } else if (data == 0x3) { // West
            return 270;
        } else if (data == 0x4) { // North
            return 0;
        } else {
            return 180;
        }
    }

    /**
     * Change a block ID to its name.
     * 
     * @param id
     * @return
     */
    public static String toBlockName(int id) {
        com.sk89q.worldedit.blocks.BlockType blockType =
                com.sk89q.worldedit.blocks.BlockType.fromID(id);
    
        if (blockType == null) {
            return "#" + id;
        } else {
            return blockType.getName();
        }
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static String joinString(String[] str, String delimiter,
            int initialIndex) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[initialIndex]);
        for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }

    /**
     * Repeat a string.
     * 
     * @param string
     * @param num
     * @return
     */
    public static String repeatString(String str, int num) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < num; i++) {
            buffer.append(str);
        }
        return buffer.toString();
    }

    /**
     * Convert a comma-delimited list to a set of integers.
     *
     * @param str
     * @return
     */
    public static Set<Integer> toBlockIDSet(String str) {
        if (str.trim().length() == 0) {
            return null;
        }
    
        String[] items = str.split(",");
        Set<Integer> result = new HashSet<Integer>();
    
        for (String item : items) {
            try {
                result.add(Integer.parseInt(item.trim()));
            } catch (NumberFormatException e) {
                int id = etc.getDataSource().getItem(item.trim());
                if (id != 0) {
                    result.add(id);
                } else {
                    CraftBookListener.logger.log(Level.WARNING, "CraftBook: Unknown block name: "
                            + item);
                }
            }
        }
    
        return result;
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    public static void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length - 1 > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
    }
    
    /**
     * Check if a player can use a command.
     *
     * @param player
     * @param command
     * @return
     */
    public static boolean canUse(Player player, String command) {
        return player.canUseCommand(command);
    }
    
    /**
     * Gets the point in front of the direction
     * [Note]: It appears player rotations and a few or maybe all other entities
     * have different rotation values. Ex: player rotation seems to be 90 degrees
     * off from minecarts.
     * Hopefully this will change in more updates.
     * 
     * This follows player rotation.
     * 
     * @param rotation
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Vector getFrontPoint(float rotation, int x, int y, int z)
    {
    	rotation = rotation % 360;
		if(rotation < 0)
			rotation += 360;
    	
		Vector point = null;
    	if(rotation < 45 || rotation >= 315)
		{
			//west
    		point = new Vector(x, y, z + 1);
		}
		else if(rotation >= 45 && rotation < 135)
		{
			//north
			point = new Vector(x - 1, y, z);
		}
		else if(rotation >= 135 && rotation < 225)
		{
			//east
			point = new Vector(x, y, z - 1);
		}
		else if(rotation >= 225 && rotation < 315)
		{
			//south
			point = new Vector(x + 1, y, z);
		}
    	
    	return point;
    }
    public static int getFrontBlockId(CraftBookWorld cbworld, float rotation, int x, int y, int z)
    {
    	return getFrontBlockId(CraftBook.getWorld(cbworld), rotation, x, y, z);
    }
    public static int getFrontBlockId(World world, float rotation, int x, int y, int z)
    {
    	Vector point = getFrontPoint(rotation, x, y, z);
    	return CraftBook.getBlockID(world, point);
    }
    
    public static String locationToString(Location location)
	{
		return location.dimension
				+","+location.x
				+","+location.y
				+","+location.z
				+","+location.rotX
				+","+location.rotY
				;
	}
    
    public static String worldLocationToString(WorldLocation wLocation)
	{
    	return wLocation.getCBWorld().name()
    			+","+locationToString(worldLocationToLocation(wLocation))
    			;
	}
	
	public static Location stringToLocation(String data)
	{
		String[] locData = data.split(",",6);
		return stringsToLocation(locData);
	}
	
	public static WorldLocation stringToWorldLocation(String data)
	{
		String[] locData = data.split(",",7);
		if(locData.length < 6 || locData[0].isEmpty())
			return null;
		
		String name = locData[0];
		
		//old format support
		if(locData.length < 7)
		{
			if(locData.length != 6)
				return null;
			
			name = CraftBook.getMainWorldName();
		}
		else
		{
			System.arraycopy(locData, 1, locData, 0, 6);
		}
		
		Location location = stringsToLocation(locData);
		
		return locationToWorldLocation(new CraftBookWorld(name, location.dimension), location);
	}
	
	public static Location stringsToLocation(String[] data)
	{
		if(data.length < 6)
			return null;
		
		Location location = null;
		
		try
		{
			int dimension = Integer.parseInt(data[0]);
			double x = Double.parseDouble(data[1]);
			double y = Double.parseDouble(data[2]);
			double z = Double.parseDouble(data[3]);
			float rotation = Float.parseFloat(data[4]);
			float pitch = Float.parseFloat(data[5]);
			
			location = new Location(x, y, z, rotation, pitch);
			location.dimension = dimension;
		}
		catch(NumberFormatException e)
		{
			return null;
		}
		
		return location;
	}
	
	public static WorldLocation locationToWorldLocation(CraftBookWorld cbworld, Location location)
	{
		if(cbworld == null || location == null)
			return null;
		
		return new WorldLocation(cbworld,
								location.x,
								location.y,
								location.z,
								location.rotX,
								location.rotY
								);
	}
	
	public static Location worldLocationToLocation(WorldLocation wLocation)
	{
		if(wLocation == null)
			return null;
		
		Location location =  new Location(wLocation.getX(),
											wLocation.getY(),
											wLocation.getZ(),
											wLocation.rotation(),
											wLocation.pitch()
											);
		
		location.world = wLocation.getCBWorld().name();
		location.dimension = wLocation.getCBWorld().dimension();
		
		return location;
	}
	
    /**
     * 
     * @param chunk
     * @return a copy of the values in the chunk's loadedTileEntityMap in array form
     */
	public static Object[] getLoadedTileEntityList(Chunk chunk){
            //Notchian: Chunk.chunkTileEntityMap, Searge: field_76648_i
            return chunk.chunk.i.values().toArray();
    }

	/**
	 * 
	 * @param wbv
	 * @return true, if the block specified by the WorldBlockVector is currently loaded, false otherwise.
	 */
	public static boolean isBlockLoaded(WorldBlockVector wbv) {
		return (
				etc.getServer().isWorldLoaded(wbv.getCBWorld().name())
				&& CraftBook.getWorld(wbv.getCBWorld())
							.isChunkLoaded(wbv.getBlockX(), wbv.getBlockY(), wbv.getBlockZ())
		);
	}
	

	@SuppressWarnings("unchecked")
	public static List<Object>[] getEntityLists(Chunk chunk) {
		if (!chunk.isLoaded()) return null;
		//Notchian: List[] Chunk.entityLists, Searge: field_76645_j
		return chunk.getChunk().j;
	}
	
	public static boolean chunkHasEntities(Chunk chunk) {
		if (!chunk.isLoaded()) return false;
		//Notchian: Chunk.hasEntities, Searge: field_76644_m
		return chunk.getChunk().m;
	}
	
	
	/** get a spot that player can stand in up to 10 blocks above a given point
	 * 
	 * @param cbworld
	 * @param pos
	 * @return Y-value of the safe location
	 */
	public static int getSafeYAbove(CraftBookWorld cbworld, Vector pos) {
		return getSafeYAbove(CraftBook.getWorld(cbworld), pos);
	}
	
	/** get a spot that player can stand in up to 10 blocks above a given point
	 * 
	 * @param world
	 * @param pos
	 * @return Y-value of the safe location
	 */
	public static int getSafeYAbove(World world, Vector pos) {
		int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
		int x = pos.getBlockX();
		int z = pos.getBlockZ();

		for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
			if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z))
					&& y < CraftBook.MAP_BLOCK_HEIGHT
					&& BlockType.canPassThrough(CraftBook.getBlockID(world, x,
							y + 1, z))) {
				return y;
			}
		}
		return maxY;
	}
}
