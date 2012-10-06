import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



/*
 * Minecraft Sitting Mod
 * Copyright (C) 2011  M4411K4
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class EntitySitting extends OEntityEnderEye
{
	private static final Map<World,EntityAccessObject> ENTITY_ACCESS = new HashMap<World,EntityAccessObject>();
	
	private final BaseEntity BASE_ENTITY;
	
	private final double OFFSET_Y;
	private final double MOUNTED_OFFSET_Y;
	private final int BLOCK_ID;
	private final SitType[] TYPES;
	
	public EntitySitting(SitType[] types, OWorldServer oworld, double x, double y, double z, double offsety, double mountedOffsetY)
	{
		super(oworld);
		y += offsety;
		b(x, y, z);
		
		BASE_ENTITY = new BaseEntity(this);
		
		int nullcount = 0;
		for(SitType type : types)
		{
			if(type == null)
				nullcount++;
		}
		if(nullcount > 0)
		{
			SitType[] newtypes = new SitType[types.length - nullcount];
			int j = 0;
			for(int i = 0; i < types.length; i++)
			{
				if(types[i] == null)
					continue;
				newtypes[j] = types[i];
				j++;
			}
			TYPES = newtypes;
		}
		else
		{
			TYPES = types;
		}
		
		OFFSET_Y = offsety;
		MOUNTED_OFFSET_Y = mountedOffsetY;
		
		int blockX = OMathHelper.c(BASE_ENTITY.getX());
		int blockY = OMathHelper.c(BASE_ENTITY.getY());
		int blockZ = OMathHelper.c(BASE_ENTITY.getZ());
		
		World world = new World(oworld);
		
		BLOCK_ID = world.getBlockIdAt(blockX, blockY, blockZ);
		
		if(!canSitOnBlock(world, blockX, blockY, blockZ))
			BASE_ENTITY.destroy();
	}
	
	@SuppressWarnings("unchecked")
	public void spawn(World world)
	{
		OWorld oworld = world.getWorld();
		int i1 = OMathHelper.c(this.t / 16.0D);
		int i2 = OMathHelper.c(this.v / 16.0D);
		oworld.e(i1, i2).a(this);
		oworld.f.add(this);
		
		addEntityTrackerList(world);
		
		EntityAccessObject entityAccess = ENTITY_ACCESS.get(world);
		
		if(entityAccess != null)
		{
			@SuppressWarnings("rawtypes")
			Set trackedEntitySet = entityAccess.trackedEntitySet;
			OIntHashMap trackedEntityHashTable = entityAccess.trackedEntityHashTable;
			
			EntitySittingTracker sitEntry = new EntitySittingTracker(this, 160, 999999999, true);
			
			trackedEntitySet.add(sitEntry);
			trackedEntityHashTable.a(BASE_ENTITY.getId(), sitEntry);
			sitEntry.b(world.getWorld().i);
			
			OIntHashMap entityIdMap = entityAccess.entityIdMap;
			entityIdMap.a(BASE_ENTITY.getId(), this);
		}
	}
	
	public static boolean isChairBlock(int id)
	{
		switch(id)
		{
			case 53:
			case 67:
			case 108:
			case 109:
			case 114:
			case 128:
			case 134:
			case 135:
			case 136:
				return true;
		}
		return false;
	}
	
	public static boolean canSitOnBlock(World world, int x, int y, int z)
	{
		int id = world.getBlockIdAt(x, y, z);
		
		if(id == 0)
		{
			id = world.getBlockIdAt(x, y-1, z);
			if(id == 0)
				return false;
		}
		
		if(id == 34 //piston ext
			|| id == 36 //block moved
			|| id == 52 //mob spawner
			|| id == 65 //ladder
			|| id == 85 //fence
			|| id == 101 //iron bars
			|| id == 102 //glass pane
			|| id == 107 //fence gate
			|| id == 113 //nether brick fence
			|| id == 139 //cobblestone wall
			|| OBlock.m[id].e(world.getWorld(), x, y, z) == null)
			return false;
		return true;
	}
	
	public double getOffsetY()
	{
		return OFFSET_Y;
	}
	
	@Override
	//moveTowards
	public void a(double x, int y, double z)
	{
		
	}
	
	@Override
	//onUpdate
	public void h_()
	{
		if(this.n != null && this.n.L)
		{
			//sitting player is dead
			if(this.n.o == this)
			{
				this.n.o = null;
			}
			this.n = null;
		}
		
		int x = OMathHelper.c(BASE_ENTITY.getX());
		int y = OMathHelper.c(BASE_ENTITY.getY());
		int z = OMathHelper.c(BASE_ENTITY.getZ());
		
		World world = BASE_ENTITY.getWorld();
		
		if(this.n == null || world.getBlockIdAt(x, y, z) != BLOCK_ID || !canSitOnBlock(world, x, y, z))
		{
			//dismounted
			BASE_ENTITY.destroy();
			return;
		}
		
		if(this.n instanceof OEntityPlayerMP)
		{
			OEntityPlayerMP eplayer = (OEntityPlayerMP)this.n;
			for(SitType type : TYPES)
			{
				type.update(world.getWorld(), this, eplayer);
			}
		}
		
		BASE_ENTITY.setMotion(0.0D, 0.0D, 0.0D);
		d(0.0D, 0.0D, 0.0D);
	}
	
	@Override
	//getMountedYOffset
	public double X()
	{
		return MOUNTED_OFFSET_Y;
	}
	
	@Override
	//canTriggerWalking
	protected boolean e_()
	{
		return false;
	}
	
	@Override
	//canBeCollidedWith
	public boolean L()
	{
		return false;
	}
	
	@Override
	//attackEntityFrom
	public boolean a(ODamageSource paramODamageSource, int paramInt)
	{
		return false;
	}
	
	public void b(ONBTTagCompound paramONBTTagCompound)
	{
		
	}
	
	public void a(ONBTTagCompound paramONBTTagCompound)
	{
		
	}
	
	public static void updateTrackers()
	{
		Iterator<Entry<String, OWorldServer[]>> worldIter = etc.getMCServer().worlds.entrySet().iterator();
        while(worldIter.hasNext())
        {
        	Map.Entry<String, OWorldServer[]> entry = (Map.Entry<String, OWorldServer[]>)worldIter.next();
        	OWorldServer[] oworlds = (OWorldServer[])entry.getValue();
        	
        	for(int i = 0; i < oworlds.length; i++)
        	{
        		addEntityTrackerList(oworlds[i].world);
        	}
        }
	}
	
	@SuppressWarnings("rawtypes")
	public static void addEntityTrackerList(World world)
	{
		if(world == null || ENTITY_ACCESS.containsKey(world))
		{
			return;
		}
		
		OEntityTracker tracker = world.getEntityTracker().getTracker();
		try {
			Class trackerclass = tracker.getClass();
			while(OEntityTracker.class != trackerclass)
			{
				trackerclass = trackerclass.getSuperclass();
			}
			
        	Field field = trackerclass.getDeclaredField("c");
        	field.setAccessible(true);
        	
        	Field field2 = trackerclass.getDeclaredField("b");
        	field2.setAccessible(true);
        	
        	Class worldclass = world.getWorld().getClass();
        	while(OWorldServer.class != worldclass)
        	{
        		worldclass = worldclass.getSuperclass();
        	}
        	
        	Field field3 = worldclass.getDeclaredField("U");
        	field3.setAccessible(true);
        	
        	EntityAccessObject entityAccess = new EntityAccessObject((Set)field2.get(tracker),
        															(OIntHashMap)field.get(tracker),
        															(OIntHashMap)field3.get(world.getWorld())
        															);
        	
        	ENTITY_ACCESS.put(world, entityAccess);
        	
        } catch (SecurityException e) {
        	e.printStackTrace();
        } catch (NoSuchFieldException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        } catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
