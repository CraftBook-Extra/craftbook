


public class Sitting
{
	protected static boolean enabled = true;
	protected static boolean requireRightClickPermission = true;
	protected static boolean requiresChairFormats = false;
	protected static  HealingType healWhileSitting = HealingType.NONE;
	protected static int globalHealingRate = 20;
	
	protected enum HealingType
	{
		NONE,
		CHAIRONLY,
		SITCOMMANDONLY,
		ALL
	};
	
	protected static void sit(Player player, SitType[] types, World world, double x, double y, double z, float rotation, double offsety)
	{
		player.setX(x);
		player.setY(y);
		player.setZ(z);
		
		OEntityPlayerMP eplayer = (OEntityPlayerMP) player.getEntity();
		eplayer.bs = rotation;
		
		OWorldServer oworld = world.getWorld();
		EntitySitting esitting = new EntitySitting(types, oworld, player.getX(), player.getY(), player.getZ(), offsety);
		
		UtilEntity.spawnEntityInWorld(oworld, esitting);
		UtilEntity.mountEntity(eplayer, esitting);
	}
	
	protected static void stand(Player player, double offsetx, double offsety, double offsetz)
	{
		OEntityPlayerMP eplayer = (OEntityPlayerMP) player.getEntity();
		if(!(UtilEntity.ridingEntity(eplayer) instanceof EntitySitting))
			return;
		
		UtilEntity.mountEntity(eplayer, (OEntity)null);
		eplayer.a.a(player.getX()+offsetx, player.getY()+offsety, player.getZ()+offsetz, player.getRotation(), player.getPitch(), player.getWorld().getType().getId(), player.getWorld().getName());
	}
	
	/*
	 * @return Returns null if not a chair. If it is a chair, it will return a list of Sign which can have
	 *     a size of 0, meaning no signs found, but it is a chair.
	 */
	protected static Sign[] isChair(Block block)
	{
		int data = block.getWorld().getBlockData(block.getX(), block.getY(), block.getZ());
		Block[] sides = ChairFormatUtil.getStairSideBlocks(block, data);
		if(sides == null)
			return null;
		
		return ChairFormatUtil.isChair(block, data, sides[0], sides[1]);
	}
	
	protected static boolean signHasSittingType(Sign sign)
	{
		String line2 = sign.getText(1).toUpperCase();
		if(line2.isEmpty() || line2.charAt(0) != '[' || line2.charAt(line2.length()-1) != ']')
			return false;
		
		try
		{
			SittingType.valueOf(line2.substring(1, line2.length()-1).replace(' ', '_'));
		}
		catch(IllegalArgumentException e)
		{
			return false;
		}
		return true;
	}
	
	protected static SittingType getSittingTypeFromSign(Sign sign)
	{
		String line2 = sign.getText(1);
		if(line2.isEmpty() || line2.charAt(0) != '[' || line2.charAt(line2.length()-1) != ']')
			return null;
		
		SittingType sittype = null;
		try
		{
			sittype = SittingType.valueOf(line2.substring(1, line2.length()-1).replace(' ', '_'));
		}
		catch(IllegalArgumentException e)
		{
			return null;
		}
		return sittype;
	}
}
