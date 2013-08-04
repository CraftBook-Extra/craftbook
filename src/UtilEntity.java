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

public class UtilEntity
{
    protected static double prevX(OEntity oentity)
    {
    	//Entity.field_70169_q
        return oentity.r;
    }
    
    protected static double prevY(OEntity oentity)
    {
    	//field_70167_r
        return oentity.s;
    }
    
    protected static double prevZ(OEntity oentity)
    {
    	//field_70166_s
        return oentity.t;
    }
    
    protected static void setPosition(OEntity oentity, double x, double y, double z)
    {
    	//field_70152_a
        oentity.b(x, y, z);
    }
    
    //mainly used to set "false"
    protected static void setDead(OEntity oentity, boolean isDead)
    {
    	//func_70067_L
        oentity.K = isDead;
    }
    
    protected static void dismount(OEntity oentity)
    {
        oentity.a((OEntity)null);
    }
    
    protected static void setOWorld(OWorld oworld, OEntity oentity)
    {
        oentity.a(oworld);
    }
    
    protected static void removeEntity(OWorld oworld, OEntity oentity)
    {
    	//func_72973_f
        oworld.f(oentity);
    }
    
    protected static void onUpdate(OEntity oentity)
    {
    	//func_70071_h_
        oentity.l_();
    }
    
    protected static void updateEntityWithOptionalForce(OWorld oworld, OEntity oentity, boolean forceUpdate)
    {
        oworld.a(oentity, forceUpdate);
    }
    
    protected static double getMountedYOffset(OEntity oentity)
    {
    	//func_70042_X
        return oentity.W();
    }
    
    protected static OEntityPlayer getClosestPlayerToEntity(OWorld oworld, OEntity oentity, double distance)
    {
        return oworld.a(oentity, distance);
    }
    
    protected static void setPlayerLocation(OEntityPlayerMP oplayer, double x, double y, double z, float rotation, float pitch)
    {
        oplayer.a.a(x, y, z, rotation, pitch);
    }
    
    protected static void sendPacket(OEntityPlayerMP oplayer, OPacket opacket)
    {
        oplayer.a.b(opacket);
    }
    
    protected static void setThrowableHeading(OIProjectile oprojectile, double x, double y, double z, float speed, float spread)
    {
        oprojectile.c(x, y, z, speed, spread);
    }
    
	protected static boolean isValidEntityTypeID(String args)
    {
    	String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	String entityName = values[0];
    	
    	if(isValidPlayerTypeID(entityName)
    		|| isValidLivingEntityTypeID(entityName)
    		|| isValidVehicleEntityTypeID(entityName)
    		|| isValidItemEntityTypeID(entityName)
    		)
    	{
    		return true;
    	}
    	
    	return false;
    }
	
	protected static boolean isValidPlayerTypeID(String entityType)
    {
    	if(entityType == null)
    		return false;
    	
    	return entityType.equalsIgnoreCase("P") || entityType.equalsIgnoreCase("PLAYER") || entityType.equalsIgnoreCase("PLY")
    			|| entityType.equalsIgnoreCase("G") || entityType.equalsIgnoreCase("GROUP") || entityType.equalsIgnoreCase("GRP")
    			|| entityType.equalsIgnoreCase("M") || entityType.equalsIgnoreCase("MATCH")
    			;
    }
	
	protected static boolean isValidLivingEntityTypeID(String entityType)
    {
    	if(entityType == null)
    		return false;
    	
    	return entityType.equalsIgnoreCase("MOB")
    			|| entityType.equalsIgnoreCase("ANIMAL")
    			|| entityType.equalsIgnoreCase("CREATURE")
    			|| entityType.equalsIgnoreCase("LIVING")
    			|| Mob.isValid(entityType)
    			;
    }
	
	protected static boolean isValidVehicleEntityTypeID(String entityType)
    {
    	if(entityType == null)
    		return false;
    	
    	return entityType.equalsIgnoreCase("MINECART")
    			|| entityType.equalsIgnoreCase("BOAT")
    			;
    }
	
	protected static boolean isValidItemEntityTypeID(String entityType)
    {
    	if(entityType == null)
    		return false;
    	
    	return entityType.equalsIgnoreCase("ITEM")
    			|| entityType.equalsIgnoreCase("ARROW")
    			|| entityType.equalsIgnoreCase("EGG")
    			|| entityType.equalsIgnoreCase("SNOWBALL")
    			|| entityType.equalsIgnoreCase("FIREBALL")
    			;
    }
	
	protected static boolean isValidEntity(BaseEntity entity, String args)
    {
    	if(entity == null)
    		return false;
    	
    	String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	if(isValidPlayerEntity(entity, values[0], values[1])
    		|| isValidLivingEntity(entity, values[0], values[1])
    		|| isValidVehicleEntity(entity, values[0], values[1])
    		|| isValidItemEntity(entity, values[0], values[1])
    		)
    	{
    		return true;
    	}
    	
    	return false;
    }
	
	protected static boolean isValidPlayerEntity(BaseEntity entity, String args)
    {
		if(entity == null)
    		return false;
    	
		String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	return isValidPlayerEntity(entity, values[0], values[1]);
    }
	
	protected static boolean isValidPlayerEntity(final BaseEntity entity, String entityName, String value) {
		synchronized (entity) {
			if (entity == null
					|| !(entity.getEntity() instanceof OEntityPlayerMP))
				return false;

			Player player = new Player((OEntityPlayerMP) entity.getEntity());
			 // This is such a kludge, but we need the player's groups.
			player = etc.getServer().getPlayer(player.getName());

			// player
			if (entityName.equalsIgnoreCase("P")
					|| entityName.equalsIgnoreCase("PLAYER")
					|| entityName.equalsIgnoreCase("PLY")) {
				if (value != null) {
					return player.getName().equalsIgnoreCase(value);
				}
				return true;
			}
			// group
			else if (entityName.equalsIgnoreCase("G")
					|| entityName.equalsIgnoreCase("GROUP")
					|| entityName.equalsIgnoreCase("GRP")) {
				if (value != null) {
					return player.isInGroup(value);
				}

				return false;
			}
			// match with player name or part of player name
			else if (entityName.equalsIgnoreCase("M")
					|| entityName.equalsIgnoreCase("MATCH")) {
				if (value != null) {
					if (player.getName().equalsIgnoreCase(value)
							|| player.getName().toLowerCase()
									.indexOf(value.toLowerCase()) != -1) {
						return true;
					}
				}

				return false;
			}

			return false;
		}
	}
	
	protected static boolean isValidLivingEntity(BaseEntity entity, String args)
    {
		if(entity == null)
    		return false;
    	
		String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	return isValidLivingEntity(entity, values[0], values[1]);
    }
	
	protected static boolean isValidLivingEntity(BaseEntity entity, String entityName, String value)
    {
		if(entity == null)
    		return false;
		
		if(entityName.equalsIgnoreCase("LIVING"))
    	{
    		return entity.isLiving();
    	}
		else if(entityName.equalsIgnoreCase("CREATURE"))
    	{
    		return entity.isMob() || entity.isAnimal();
    	}
		else if(entityName.equalsIgnoreCase("MOB"))
    	{
    		return entity.isMob();
    	}
		else if(entityName.equalsIgnoreCase("ANIMAL"))
		{
			return entity.isAnimal();
		}
		else if(Mob.isValid(entityName))
		{
			if((entity.isMob() || entity.isAnimal()) && entity.getName().equals(entityName))
			{
				if(value != null && MCX200.isValidColorMob(entityName))
				{
					int color = 0;
					try
					{
						color = Integer.parseInt(value);
					}
					catch(NumberFormatException e)
					{
						return false;
					}
					if(color < 0)
						color = 0;
					else if(color > 15)
						color = 15;
					
					if(entityName.equals("Sheep") && (entity.getEntity() instanceof OEntitySheep))
			    	{
			    		OEntitySheep sheep = (OEntitySheep)entity.getEntity();
			    		//EntitySheep.getFleeceColor, func_70896_n
			    		return color == sheep.bT();
			    	}
			    	else if(entityName.equals("Creeper") && (entity.getEntity() instanceof OEntityCreeper))
			    	{
			    		if(color > 1)
			    			return false;
			    		
			    		OEntityCreeper creeper = (OEntityCreeper)entity.getEntity();
			    		// EntityCreeper.getPowered, func_70830_n
			    		return (color == 0) ^ creeper.bT();
			    	}
			    	else if(entityName.equals("Wolf") && (entity.getEntity() instanceof OEntityWolf))
			    	{
			    		if(color > 2)
			    			return false;
			    		
			    		OEntityWolf wolf = (OEntityWolf)entity.getEntity();
			    		//func_70919_bu
			    		return (color == 2 && wolf.cc())
			    				//func_70906_o
			    				|| (color == 1 && wolf.bU())
			    				|| (color == 0 && !wolf.cc() && !wolf.bU() && !wolf.bT());
			    	}
			    	else if(entityName.equals("Ozelot") && (entity.getEntity() instanceof OEntityOcelot))
			    	{
			    		if(color > 2)
			    			return false;
			    		
			    		OEntityOcelot ocelot = (OEntityOcelot)entity.getEntity();
			    		return (color == 1 && ocelot.bU())
			    				|| (color == 0 && !ocelot.bU() && !ocelot.bT());
			    	}
			    	else if(entityName.equals("Pig") && (entity.getEntity() instanceof OEntityPig))
			    	{
			    		if(color > 1)
			    			return false;
			    		
			    		OEntityPig pig = (OEntityPig)entity.getEntity();
			    		return (color == 0) ^ pig.bT();
			    	}
				}
				
				return true;
			}
			return false;
		}
		
		return false;
    }
	
	protected static boolean isValidVehicleEntity(BaseEntity entity, String args)
    {
		if(entity == null)
    		return false;
    	
		String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	return isValidVehicleEntity(entity, values[0], values[1]);
    }
	
	protected static boolean isValidVehicleEntity(BaseEntity entity, String entityName, String value)
    {
		if(entity == null)
    		return false;
		
    	if(entityName.equalsIgnoreCase("MINECART"))
		{
			if(entity.getEntity() instanceof OEntityMinecart)
			{
				if(value != null)
				{
					try
					{
						int type = Integer.parseInt(value);
						return type == (new Minecart((OEntityMinecart)entity.getEntity()).getType().getType());
					}
					catch(NumberFormatException e)
					{
						return false;
					}
				}
				
				return true;
			}
			return false;
		}
		else if(entityName.equalsIgnoreCase("BOAT"))
		{
			return entity.getEntity() instanceof OEntityBoat;
		}
    	
    	return false;
    }
	
	protected static boolean isValidItemEntity(BaseEntity entity, String args)
    {
		if(entity == null)
    		return false;
    	
		String[] values = parseEntityArgs(args);
    	if(values == null)
    		return false;
    	
    	return isValidItemEntity(entity, values[0], values[1]);
    }
	
	protected static boolean isValidItemEntity(BaseEntity entity, String entityName, String value)
    {
		if(entity == null)
    		return false;
		
    	if(entityName.equalsIgnoreCase("ITEM"))
		{
			if(entity.isItem())
			{
				if(value != null)
				{
					String[] data = value.split("@", 2);
					try
					{
						int type = Integer.parseInt(data[0]);
						int color = -1;
						if(data.length > 1)
						{
							color = Integer.parseInt(data[1]);
						}
						
						Item item = (new ItemEntity((OEntityItem)entity.getEntity())).getItem();
						
						return item.getItemId() == type && (color < 0 || color == item.getDamage());
					}
					catch(NumberFormatException e)
					{
						return false;
					}
				}
				return true;
			}
			return false;
		}
		else if(entityName.equalsIgnoreCase("ARROW"))
		{
			return entity.getEntity() instanceof OEntityArrow;
		}
		else if(entityName.equalsIgnoreCase("EGG"))
		{
			return entity.getEntity() instanceof OEntityEgg;
		}
		else if(entityName.equalsIgnoreCase("SNOWBALL"))
		{
			return entity.getEntity() instanceof OEntitySnowball;
		}
		else if(entityName.equalsIgnoreCase("FIREBALL"))
		{
			return entity.getEntity() instanceof OEntityFireball;
		}
    	
    	return false;
    }
	
	public static String[] parseEntityArgs(String args)
	{
		if(args == null)
    		return null;
    	
    	String[] values = args.split(":", 2);
    	
    	if(values.length > 1)
    		return values;
    	
    	return new String[]{values[0], null};
	}
}
