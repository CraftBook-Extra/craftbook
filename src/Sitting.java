


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
	
	protected static void sit(OEntityPlayerMP eplayer, SitType[] types, World world, double x, double y, double z, float rotation, double offsety)
	{
		eplayer.bf = x;
		eplayer.bg = y;
		eplayer.bh = z;
		eplayer.bl = rotation;
		
		OWorldServer oworld = world.getWorld();
		EntitySitting esitting = new EntitySitting(types, oworld, eplayer.bf, eplayer.bg, eplayer.bh, offsety);
		oworld.b(esitting);
		eplayer.a(esitting);
	}
	
	protected static void stand(OEntityPlayerMP eplayer, double offsetx, double offsety, double offsetz)
	{
		if(!(eplayer.ba instanceof EntitySitting))
			return;
		
		OEntity nullEnt = null;
		eplayer.a(nullEnt);
		eplayer.a.a(eplayer.bf+offsetx, eplayer.bg+offsety, eplayer.bh+offsetz, eplayer.bl, eplayer.bm);
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
