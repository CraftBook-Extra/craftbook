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
	private final BaseEntity BASE_ENTITY;
	
	private final double OFFSET_Y;
	private final double MOUNTED_OFFSET_Y;
	private final int BLOCK_ID;
	private final SitType[] TYPES;
	private final double xPos;
	private final double yPos;
	private final double zPos;
	private final int blockX;
	private final int blockY;
	private final int blockZ;
	
	public EntitySitting(SitType[] types, World world, double x, double y, double z, double offsety, double mountedOffsetY)
	{
		super(world.getWorld());
		y += offsety;
		
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
		
		xPos=x;
		yPos=y;
		zPos=z;
		
		blockX = OMathHelper.c(x);
		blockY = OMathHelper.c(y);
		blockZ = OMathHelper.c(z);
		
		// wrap in BaseEntity to avoid Notchian code
		BASE_ENTITY = new BaseEntity(this);
		BASE_ENTITY.setX(x);
		BASE_ENTITY.setY(y);
		BASE_ENTITY.setZ(z);

		//World world = new World(oworld);
		BLOCK_ID = world.getBlockIdAt(blockX, blockY, blockZ);
		if(!canSitOnBlock(world, blockX, blockY, blockZ)) {
			BASE_ENTITY.destroy();
		}
	}

	public void spawn() {
		BASE_ENTITY.spawn();
	}
	
	public void setRiddenByEntity(BaseEntity rider) {
		BASE_ENTITY.setRiddenByEntity(rider);
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
			//Block.field_71973_m[id].func_71872_e()
			|| OBlock.r[id].b(world.getWorld(), x, y, z) == null) {
			return false;
		}
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
	//onUpdate func_70071_h_
	public void l_()
	{
		BaseEntity rider = this.BASE_ENTITY.getRiddenByEntity();
		if(rider != null && rider.isDead())
		{
			//sitting player is dead
			if(rider.getRidingEntity() == this.BASE_ENTITY)
			{
				// is this necessary?
				rider.setRidingEntity(null);
			}
			this.setRiddenByEntity(null);
		}

		World world = BASE_ENTITY.getWorld();
		
		if(rider == null 
				|| world.getBlockIdAt(blockX, blockY, blockZ) != BLOCK_ID 
				|| !canSitOnBlock(world, blockX, blockY, blockZ))
		{
			//dismounted or block we're sitting on changed
			BASE_ENTITY.destroy();
			return;
		}
		
		if(rider.getEntity() instanceof OEntityPlayerMP)
		{
			OEntityPlayerMP eplayer = (OEntityPlayerMP)rider.getEntity();
			for(SitType type : TYPES)
			{
				type.update(world.getWorld(), this, eplayer);
			}
		}
		// do not move, no matter what others tell us
		BASE_ENTITY.setMotion(0.0D, 0.0D, 0.0D);
		BASE_ENTITY.setX(xPos);
		BASE_ENTITY.setY(yPos);
		BASE_ENTITY.setZ(zPos);
	}
	
	@Override
	//getMountedYOffset func_70042_X
	public double W()
	{
		return MOUNTED_OFFSET_Y;
	}
	
	@Override
	//canTriggerWalking
	protected boolean f_()
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
}
