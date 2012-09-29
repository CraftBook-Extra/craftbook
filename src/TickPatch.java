/*    
Drop-in onTick hpok for hMod
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software. It comes withput any warranty, to
the extent permitted by applicable law. You can redistribute it
and/or modify it under the terms of the Do What The Fuck You Want
To Public License, Version 2, as published by Sam hpcevar. See
http://sam.zoy.org/wtfpl/COPYING for more details.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * <p>Allows plugins to define code to run every tick.</p>
 * 
 * <p>To use, define a Runnable object that will be run each tick, and call the
 *    following in the initialize methpd:</p>
 * 
 * <p>TickPatch.applyPatch();
 *    TickPatch.addTask(TickTask.wrapRunnable(this,onTick));</p>
 * 
 * @authpr Lymia
 */
public class TickPatch extends OEntityTracker {
    @SuppressWarnings("unused")
    private static final Object HP_PATCH_APPLIED = null;
    /**
     * Do not use directly.
     */
    @Deprecated
    public static final CopyOnWriteArrayList<Runnable> TASK_LIST = new CopyOnWriteArrayList<Runnable>();
    
    private static Class<OEntityTracker> CLASS = OEntityTracker.class;
    private static Field[] FIELDS = CLASS.getDeclaredFields();
    
    private final int WORLD_INDEX;
    private static Runnable tickRunnable;
    
    private TickPatch(OWorldServer oworld, OEntityTracker g, int index) {
        super(oworld);
        WORLD_INDEX = index;
        if(g.getClass()!=CLASS) throw new RuntimeException("unexpected type for im instance");
        for(Field f:FIELDS) try {
            if(Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            Object o = f.get(g);
            f.setAccessible(true);
            f.set(this, o);
        } catch (Exception e) {
            System.out.println("Failed to copy field: "+f.getName());
            e.printStackTrace();
        }
    }
    
    /**
     * The actual patch method.
     * Should not be called.
     */
    @Deprecated
    public void a() {
        super.a();
        if(WORLD_INDEX == 0)
        {
	        Runnable[] tasks = TASK_LIST.toArray(new Runnable[0]);
	        for(int i=0;i<tasks.length;i++)
        	{
	        	try
	        	{
	        		tasks[i].run();
	        	}
	        	catch(ConcurrentModificationException e)
	        	{
	        		CraftBookDelegateListener.logger.log(Level.WARNING, "CraftBook TickPatch ConcurrentModificationException Error: ", e);
	        	}
        	}
        }
        
        if(tickRunnable != null)
        	tickRunnable.run();
    }
    
    protected static void setTickRunnable(Runnable runnable, World world)
    {
    	EntityTracker entityTracker = world.getEntityTracker();
    	if(entityTracker == null)
    	{
    		throw new RuntimeException("unexpected error: EntityTracker is null");
    	}
    	
        try {
        	Field field = entityTracker.getTracker().getClass().getDeclaredField("tickRunnable");
        	field.setAccessible(true);
        	field.set(entityTracker.getTracker(), runnable);
        } catch (SecurityException e) {
            throw new RuntimeException("unexpected error: cannot use reflection");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("patch not applied");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        }
    }
    
    /**
     * Applies the patch, if not already applied.
     * Call before using addTask or getTaskList().
     */
    public static void applyPatch(World world) {
    	EntityTracker entityTracker = world.getEntityTracker();
    	if(entityTracker == null)
    	{
    		throw new RuntimeException("unexpected error: EntityTracker is null");
    	}
    	
    	int i = 0;
    	
    	try {
    		entityTracker.getTracker().getClass().getDeclaredField("HP_PATCH_APPLIED");
        } catch (SecurityException e) {
            throw new RuntimeException("unexpected error: cannot use reflection");
        } catch (NoSuchFieldException e) {
        	OWorld oworld = world.getWorld();
        	
        	try {
        		TickPatch patch = new TickPatch(world.getWorld(),entityTracker.getTracker(),i);
            	EntityTracker tickTrack = new EntityTracker(patch);
        		
        		Field field = patch.getClass().getSuperclass().getDeclaredField("entityTracker");
            	field.setAccessible(true);
            	field.set(patch, tickTrack);
            	
            	field = oworld.getClass().getDeclaredField("L");
            	field.setAccessible(true);
            	field.set(oworld, patch );
            } catch (SecurityException e2) {
            	throw new RuntimeException("error: EntityTracker reflection failed.");
            } catch (NoSuchFieldException e2) {
            	throw new RuntimeException("error: entityTracker field missing. Outdated?");
            } catch (IllegalArgumentException e2) {
            	e2.printStackTrace();
            } catch (IllegalAccessException e2) {
            	throw new RuntimeException("error: EntityTracker access failed.");
    		}
        }
    }
    /**
     * Adds a new task.
     */
    public static void addTask(Runnable r, World world) {
    	if(r == null || world == null)
    		return;
    	
    	getTaskList(world).add(r);
    }
    /**
     * Retrieves the task list.
     */
    @SuppressWarnings("unchecked")
    public static CopyOnWriteArrayList<Runnable> getTaskList(World world) {
    	
    	EntityTracker entityTracker = world.getEntityTracker();
    	if(entityTracker == null)
    	{
    		throw new RuntimeException("unexpected error: EntityTracker is null");
    	}
    	
        try {
            return (CopyOnWriteArrayList<Runnable>) entityTracker.getTracker().getClass().getField("TASK_LIST").get(null);
        } catch (SecurityException e) {
            throw new RuntimeException("unexpected error: cannot use reflection");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("patch not applied");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("patch not applied, or incompatable patch applied");
        }
    }

    /**
     * Wraps a runnable to allow easier use by plugins.
     */
    public static Runnable wrapRunnable(final Plugin p, final Runnable r, final World world) {
        return new Runnable() {
            private PluginLoader l = etc.getLoader();
            
            public void run() {
            	
                CopyOnWriteArrayList<Runnable> taskList = getTaskList(world);
                if(l.getPlugin(p.getName())!=p)
                	while(taskList.contains(this))
                		getTaskList(world).remove(this);
                if(p.isEnabled()) r.run();
            }
        };
    }
}
