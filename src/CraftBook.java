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

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.state.StateManager;

/**
 * Entry point for the plugin for hey0's mod.
 *
 * @author sk89q
 */
public class CraftBook extends Plugin {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    private static final File pathToState = new File("world"+File.separator+"craftbook");
    
    /**
     * Submit your Runnables to this instead of making your own threads.
     * 
     * Do not modify the world directly in your multithreaded code!
     * Use cbxScheduler.executeInServer() if you must modify the world from
     * a Runnable being executed here.<p>
     * Handle <i>all</i> exceptions inside your Runnables, or you might crash the server!
     */
    public static CBXScheduler cbxScheduler;
    
    /**
     * Listener for the plugin system. This listener handles configuration
     * loading and the bulk of the core functions for CraftBook. Individual
     * features are implemented in the delegate listeners.
     */
    private final CraftBookListener listener =
            new CraftBookListener(this);
    
    /**
     * Tick delayer instance used to delay some events until the next tick.
     * It is used mostly for redstone-related events.
     */
    private final TickDelayer[] delays = new TickDelayer[]{new TickDelayer()};

    /**
     * Used to fake the data value at a point. For the redstone hook, because
     * the data value has not yet been set when the hook is called, its data
     * value is faked by CraftBook. As all calls to get a block's data are
     * routed through CraftBook already, this makes this hack feasible.
     */
    private static Map<OWorldServer, FakeData> fakeData = new HashMap<OWorldServer, FakeData>();
    
    /**
     * CraftBook version, fetched from the .jar's manifest. Used to print the
     * CraftBook version in various places.
     */
    private String version;
    
    /**
     * State manager object.
     */
    private StateManager stateManager = new StateManager();
    
    /**
     * State manager thread. 
     */
    private Thread stateThread = new Thread() {
        public void run() {
            PluginLoader l = etc.getLoader();
            while(l.getPlugin("CraftBook")==CraftBook.this) {
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    try {Thread.sleep(10*60*1000);} catch (InterruptedException e) {}
                if(l.getPlugin("CraftBook")==CraftBook.this) 
                    stateManager.save(pathToState);
            }
        }
    };

    /**
     * Delegate listener for mechanisms.
     */
    private final CraftBookDelegateListener mechanisms =
            new MechanismListener(this, listener);
    /**
     * Delegate listener for redstone.
     */
    private final CraftBookDelegateListener redstone =
            new RedstoneListener(this, listener);
    /**
     * Delegate listener for vehicle.
     */
    private final CraftBookDelegateListener vehicle =
            new VehicleListener(this, listener);
    
    /**
     * Delegate listener for inventories.
     * 
     * This has no hooks to register, but we're using it as
     * the apropos place to store inventory related config
     * values.
     */
	private final CraftBookDelegateListener inventories = 
    		new InventoryListener(this, listener);
	
	protected static CBData cbdata;
    
    private PluginInterface cbRequest = new CBHookFunc();
    
    public static final int MAP_BLOCK_HEIGHT = 256;
    
    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
    	World defaultWorld = etc.getServer().getDefaultWorld();
        TickPatch.applyPatch(defaultWorld);
        
        OWorld odefaultWorld = defaultWorld.getWorld();
        OWorldSavedData wdata = odefaultWorld.a(CBData.class, "craftbook");
        
        if(wdata != null && !(wdata instanceof CBData))
        {
        	//CraftBook was reloaded. Need to remove previous CBData and replace with
        	//this version's CBData, which requires a reload of the data file.
        	if(wdata.d())
        	{
        		//force save-all
        		odefaultWorld.A.a();
        	}
        	
        	try {
            	Field field = odefaultWorld.A.getClass().getDeclaredField("b");
            	field.setAccessible(true);
            	
            	@SuppressWarnings("rawtypes")
				HashMap loadedDataMap = (HashMap)field.get(odefaultWorld.A);
            	
            	field = odefaultWorld.A.getClass().getDeclaredField("c");
            	field.setAccessible(true);
            	
            	@SuppressWarnings("rawtypes")
				ArrayList loadedDataList = (ArrayList)field.get(odefaultWorld.A);
            	
            	loadedDataList.remove(loadedDataMap.remove("craftbook"));
            } catch (SecurityException e) {
            	e.printStackTrace();
            } catch (NoSuchFieldException e) {
            	e.printStackTrace();
            } catch (IllegalArgumentException e) {
            	e.printStackTrace();
            } catch (IllegalAccessException e) {
    			e.printStackTrace();
    		}
        	
        	wdata = odefaultWorld.A.a(CBData.class, "craftbook");
        }
        
        if(wdata == null)
        {
        	CraftBook.cbdata = new CBData();
			odefaultWorld.A.a("craftbook", CraftBook.cbdata);
        }
        else
        {
        	CraftBook.cbdata = (CBData)wdata;
        }

        registerHook(listener, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(listener, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(listener, "LOGIN", PluginListener.Priority.MEDIUM);
        registerHook(listener, "REDSTONE_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(listener, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);

        registerHook(mechanisms, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_RIGHTCLICKED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "BLOCK_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "SERVERCOMMAND", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "PLAYER_MOVE", PluginListener.Priority.MEDIUM);
        registerHook(mechanisms, "DAMAGE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(mechanisms);
        
        registerHook(redstone, "BLOCK_RIGHTCLICKED", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "SIGN_SHOW", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "BLOCK_BROKEN", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "BLOCK_PLACE", PluginListener.Priority.MEDIUM);
        registerHook(redstone, "CHUNK_LOADED", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(redstone);

        registerHook(vehicle, "DISCONNECT", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "SIGN_CHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "BLOCK_PLACE", PluginListener.Priority.LOW);
        registerHook(vehicle, "BLOCK_RIGHTCLICKED", PluginListener.Priority.LOW);
        registerHook(vehicle, "COMMAND", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_POSITIONCHANGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_UPDATE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DAMAGE", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_ENTERED", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_DESTROYED", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "VEHICLE_COLLISION", PluginListener.Priority.MEDIUM);
        registerHook(vehicle, "DISPENSE", PluginListener.Priority.MEDIUM);
        listener.registerDelegate(vehicle);
        
        // Our hook-free inventories listener
        listener.registerDelegate(inventories);
        
        PluginLoader loader = etc.getLoader();
        loader.addCustomListener(CBPluginInterface.cbSignMech);
        
        loader.addCustomListener(cbRequest);
        
        Redstone.outputLever = new OutputLever();
        TickPatch.setTickRunnable(Redstone.outputLever, defaultWorld);
        
        TickPatch.addTask(TickPatch.wrapRunnable(this, delays[0], defaultWorld), defaultWorld);
        
        pathToState.mkdirs();
        stateManager.load(pathToState);
        
        stateThread.setName("StateManager");
        stateThread.start();
        
        CBWarp.reload();
    }

    /**
     * Conditionally registers a hook for a listener.
     * 
     * @param name
     * @param priority
     * @return whether the hook was registered correctly
     */
    public boolean registerHook(PluginListener listener,
            String name, PluginListener.Priority priority) {
        try {
            PluginLoader.Hook hook = PluginLoader.Hook.valueOf(name);
            etc.getLoader().addListener(hook, listener, this, priority);
            return true;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "CraftBook: Missing hook " + name + "!");
            return false;
        }
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        logger.log(Level.INFO, "CraftBook version " + getVersion() + " loaded");
        CraftBook.cbxScheduler = new CBXScheduler();
        // This will also fire the loadConfiguration() methods of delegates
        listener.loadConfiguration();
        
        SignPatch.applyPatch();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        
        /*
        // funny old code from sk89q
        // this was to stop the MinecartMania plugin from disabling CraftBook.
        // Instead of being disabled from MinecartMania, this disables MinecartMania
        // and enables CraftBook again.
        
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            if (element.getClassName().contains("MinecartMania")) {
                etc.getServer().addToServerQueue(new Runnable() {
                    public void run() {
                        try {
                            etc.getLoader().disablePlugin("MinecartMania");
                            logger.warning("Minecart Mania has been disabled.");
                        } finally {
                            etc.getLoader().enablePlugin("CraftBook");
                        }
                    }
                });
                
                return;
            }
        }
        */
    	
    	Iterator<Entry<String, OWorldServer[]>> worldIter = etc.getMCServer().worlds.entrySet().iterator();
        while(worldIter.hasNext())
        {
        	Map.Entry<String, OWorldServer[]> entry = (Map.Entry<String, OWorldServer[]>)worldIter.next();
        	OWorldServer[] oworlds = (OWorldServer[])entry.getValue();
        	
        	for(int i = 0; i < oworlds.length; i++)
        	{
        	    List<BaseEntity> entities = oworlds[i].world.getEntityList();
        	    for(BaseEntity entity : entities)
        	    {
        	        if(entity != null && entity.getEntity() instanceof EntitySitting)
        	        {
        	            entity.destroy();
        	        }
        	    }
        	}
        }
        
        EnchantCraft.removeFeature();

        SignPatch.removePatch();
        stateManager.save(pathToState);
        
        listener.disable();

        cbxScheduler.shutdown();
        try {
        	cbxScheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        cbxScheduler.shutdownNow();
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    public String getVersion() {
        if (version != null) {
            return version;
        }
        
        Package p = CraftBook.class.getPackage();
        
        if (p == null) {
            p = Package.getPackage("com.sk89q.craftbook");
        }
        
        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();
            
            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    public TickDelayer getDelay(CraftBookWorld cbworld) {
    	//[NOTE]: change to Map if multi-world delay is needed
    	//return delays.get(cbworld);
        return delays[0];
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }

    protected static int getBlockID(CraftBookWorld cbworld, int x, int y, int z) {
        return getBlockID(getWorld(cbworld), x, y, z);
    }

    protected static int getBlockID(CraftBookWorld cbworld, Vector pt) {
        return getBlockID(getWorld(cbworld), pt);
    }
    
    protected static int getBlockID(World world, int x, int y, int z) {
    	return world.getBlockIdAt(x, y, z);
    }
    
    protected static int getBlockID(World world, Vector pt) {
        return world.getBlockIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    protected static int getBlockData(CraftBookWorld cbworld, int x, int y, int z) {
    	return getBlockData(getWorld(cbworld), new BlockVector(x, y, z));
    }

    protected static int getBlockData(CraftBookWorld cbworld, Vector pt) {
    	return getBlockData(getWorld(cbworld), pt.toBlockVector());
    }
    
    protected static int getBlockData(World world, int x, int y, int z) {
    	return getBlockData(world, new BlockVector(x, y, z));
    }

    protected static int getBlockData(World world, Vector pt) {
    	return getBlockData(world, pt.toBlockVector());
    }
    
    protected static int getBlockData(World world, BlockVector bVec)
    {
    	FakeData fdata = fakeData.get(world.getWorld());
        if (fdata != null && fdata.pos.equals(bVec))
        {
            return fdata.val;
        }
        return world.getBlockData(bVec.getBlockX(), bVec.getBlockY(), bVec.getBlockZ());
    }

    protected static boolean setBlockID(CraftBookWorld cbworld, int x, int y, int z, int type) {
    	return setBlockID(getWorld(cbworld), x, y, z, type);
    }

    protected static boolean setBlockID(CraftBookWorld cbworld, Vector pt, int type) {
        return setBlockID(cbworld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }
    
    protected static boolean setBlockID(World world, Vector pt, int type) {
        return setBlockID(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }
    
    protected static boolean setBlockID(World world, int x, int y, int z, int type) {
        if (y < MAP_BLOCK_HEIGHT - 1 && BlockType.isBottomDependentBlock(getBlockID(world, x, y + 1, z))) {
            world.setBlockAt(0, x, y + 1, z);
        }
        return world.setBlockAt(type, x, y, z);
    }

    protected static boolean setBlockData(CraftBookWorld cbworld, int x, int y, int z, int data) {
        return setBlockData(getWorld(cbworld), x, y, z, data);
    }

    protected static boolean setBlockData(CraftBookWorld cbworld, Vector pt, int data) {
        return setBlockData(cbworld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }
    
    protected static boolean setBlockData(World world, Vector pt, int data) {
        return setBlockData(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }
    
    protected static boolean setBlockData(World world, int x, int y, int z, int data) {
        return world.setBlockData(x, y, z, data);
    }
    
    protected static boolean setBlockIdAndData(CraftBookWorld cbworld, int x, int y, int z, int id, int data)
    {
    	return setBlockIdAndData(getWorld(cbworld), x, y, z, id, data);
    }

    protected static boolean setBlockIdAndData(CraftBookWorld cbworld, Vector pt, int id, int data) {
        return setBlockIdAndData(cbworld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), id, data);
    }
    
    protected static boolean setBlockIdAndData(World world, Vector pt, int id, int data) {
        return setBlockIdAndData(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), id, data);
    }
    
    protected static boolean setBlockIdAndData(World world, int x, int y, int z, int id, int data)
    {
    	if(data == 0)
    		return setBlockID(world, x, y, z, id);
    	
    	boolean result = setBlockID(world, x, y, z, id);
    	setBlockData(world, x, y, z, data);
    	
        return result;
    }
    
    protected static OWorldServer getOWorldServer(CraftBookWorld cbworld)
    {
    	return getOWorldServer(cbworld.name(), cbworld.dimension());
    }
    
    protected static OWorldServer getOWorldServer(String name, int dimension)
    {
    	return etc.getMCServer().getWorld(name, dimension);
    }
    
    protected static OWorldServer getOWorldServer(String name, int dimension, boolean autoLoad)
    {
    	if(!etc.getServer().isWorldLoaded(name))
    	{
    		if(!autoLoad)
    		{
    			return null;
    		}
    		etc.getServer().loadWorld(name);
    	}
    	return etc.getMCServer().getWorld(name, dimension);
    }
    
    protected static World getWorld(CraftBookWorld cbworld)
    {
    	return getWorld(cbworld.name(), cbworld.dimension());
    }
    
    protected static World getWorld(String name, int dimension)
    {
    	OWorldServer oworld = getOWorldServer(name, dimension);
    	if(oworld == null)
    		return null;
    	return oworld.world;
    }
    
    protected static World getWorld(CraftBookWorld cbworld, boolean autoLoad)
    {
    	return getWorld(cbworld.name(), cbworld.dimension(), autoLoad);
    }
    
    protected static World getWorld(String name, int dimension, boolean autoLoad)
    {
    	//keeping as a copy because of loaded check
    	OWorldServer oworld = getOWorldServer(name, dimension, autoLoad);
    	if(oworld == null)
    		return null;
    	return oworld.world;
    }
    
    protected static String getMainWorldName()
    {
    	return etc.getServer().getMCServer().a("level-name", "world");
    }
    
    protected static OWorldServer getMainOWorldServer(int dimension) {
    	return etc.getMCServer().a(dimension);
    }
    
    protected static CraftBookWorld getCBWorld(World world)
    {
    	return new CraftBookWorld(world.getName(), world.getType().getId());
    }
    
    protected static SignText getSignText(CraftBookWorld cbworld, Vector pt) {
        return getSignText(getWorld(cbworld), pt);
    }
    
    protected static SignText getSignText(World world, Vector pt) {
        ComplexBlock cblock = world.getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (cblock instanceof Sign) {
            return new SignTextImpl((Sign)cblock);
        }
        
        return null;
    }

    public static void dropSign(CraftBookWorld cbworld, Vector pt) {
        dropSign(cbworld, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    
    public static void dropSign(CraftBookWorld cbworld, int x, int y, int z) {
        dropSign(getWorld(cbworld), x, y, z);
    }
    
    public static void dropSign(World world, Vector pt) {
        dropSign(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    
    public static void dropSign(World world, int x, int y, int z) {
    	world.setBlockAt(0, x, y, z);
    	world.dropItem(x, y, z, 323);
    }

    protected static void fakeBlockData(CraftBookWorld cbworld, int x, int y, int z, int data) {
    	fakeBlockData(cbworld, new BlockVector(x, y, z), data);
    }

    protected static void fakeBlockData(CraftBookWorld cbworld, Vector pt, int data) {
    	OWorldServer oworld = getOWorldServer(cbworld);
    	if(oworld == null)
    		return;
    	fakeData.put(oworld, new FakeData(pt.toBlockVector(), data));
    }

    protected static void clearFakeBlockData(CraftBookWorld cbworld) {
    	OWorldServer oworld = getOWorldServer(cbworld);
    	if(oworld == null)
    		return;
    	
    	fakeData.remove(oworld);
    }
    
    public static class FakeData
    {
    	public BlockVector pos;
    	
    	/**
         * Used to fake the data value at a point. See fakedataPos.
         */
    	public int val;
    	
    	public FakeData(){}
    	
    	public FakeData(BlockVector pos, int val)
    	{
    		this.pos = pos;
    		this.val = val;
    	}
    }
    
    //[NOTE]: temporary fix for warping out of The End. Remove if Canary adds a work around to Minecraft's "feature"
    public static void teleportPlayer(Player player, WorldLocation wLocation)
    {
    	CraftBookWorld cbworld = wLocation.getCBWorld();
    	
    	if(!player.getWorld().getName().equals(cbworld.name()))
    	{
    		World world = CraftBook.getWorld(cbworld, true);
    		if(world == null)
    		{
    			player.sendMessage(Colors.Rose+"Failed to find destination world.");
    			return;
    		}
    	}
    	
    	boolean isEnd = player.getWorld().getType() == World.Dimension.END;
		
		player.teleportTo(Util.worldLocationToLocation(wLocation));
		
		if(isEnd && cbworld.dimension() != World.Dimension.END.getId())
		{
			World world = CraftBook.getWorld(cbworld);
			boolean found = false;
			
			List<BaseEntity> entities = world.getEntityList();
			for(BaseEntity entity : entities)
			{
			    if(entity.isPlayer() && entity.getEntity().hashCode() == player.getEntity().hashCode())
			    {
			        found = true;
                    break;
			    }
			}
			
			if(!found)
			{
			    UtilEntity.setOWorld(world.getWorld(), player.getEntity());
			    player.teleportTo(wLocation.getX(), player.getY(), wLocation.getZ(), player.getRotation(), player.getPitch());
			    player.spawn();
			    UtilEntity.updateEntityWithOptionalForce(world.getWorld(), player.getEntity(), false);
			}
		}
    }
    
    /* Allows entities (Minecarts, items, Mobs, etc) to teleport to other worlds (Nether, The End, etc)
     * Also enables riders to teleport with the teleporting entity. Ex: Players riding minecarts.
     * 
     * NOTE: because of how Minecraft works, entities will be DELETED if teleported to an unloaded chunk!
     * 
     */
    public static void teleportEntity(BaseEntity entity, WorldLocation wLocation)
    {
    	if(entity == null || entity.isDead())
    		return;
    	
    	CraftBookWorld cbworld = wLocation.getCBWorld();
    	
    	if(!entity.getWorld().getName().equals(cbworld.name()))
    	{
    		World world = CraftBook.getWorld(cbworld, true);
    		if(world == null)
    		{
    			return;
    		}
    	}
    	
    	World oldWorld = entity.getWorld();
    	World.Dimension worldType = oldWorld.getType();
    	
    	if(worldType.getId() != cbworld.dimension() || !entity.getWorld().getName().equals(cbworld.name()))
    	{
    		World newWorld = CraftBook.getWorld(cbworld);
    		BaseEntity rider = entity.getRiddenByEntity();
    		
    		if(rider != null)
    		{
    		    entity.setRiddenByEntity(rider);
    			if(rider.isPlayer())
    			{
    				CraftBook.teleportPlayer((Player)rider, wLocation);
    			}
    			else
    			{
    				CraftBook.teleportEntity(rider, wLocation);
    			}
    		}
    		
    		UtilEntity.removeEntity(oldWorld.getWorld(), entity.getEntity());
    		UtilEntity.setDead(entity.getEntity(), false);
    		
    		wLocation = wLocation.add(0.0D, 0.6200000047683716D, 0.0D);
    		
    		UtilEntity.updateEntityWithOptionalForce(oldWorld.getWorld(), entity.getEntity(), false);
    		
    		entity.teleportTo(Util.worldLocationToLocation(wLocation));
    		entity.spawn();
    		
    		UtilEntity.updateEntityWithOptionalForce(newWorld.getWorld(), entity.getEntity(), false);
    		
    		UtilEntity.setOWorld(newWorld.getWorld(), entity.getEntity());
    		
    		if(rider != null)
    		{
    		    entity.setRiddenByEntity(rider);
    		}
    	}
    	
    	entity.teleportTo(Util.worldLocationToLocation(wLocation));
    }
}
