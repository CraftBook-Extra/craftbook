import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sk89q.craftbook.BlockArea;
import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.WorldBlockVector;


public class Bounce
{
	protected static Map<WorldBlockVector, BlockArea> icAreas = new HashMap<WorldBlockVector, BlockArea>();
	protected static Map<WorldBlockVector, BlockArea> icSoftAreas = new HashMap<WorldBlockVector, BlockArea>();
	
	protected static int[] blockBounce = new int[]{0, -1};
	protected static int[] blockSoft = new int[]{0, -1};
	protected static int force = 3;
	
	protected static int maxICForce = 8;
	public static ArrayList<Integer> allowedICBlocks;
	
	protected static void bounce(BaseEntity entity, Location from, Location to)
	{
		if(from.y <= to.y)
			return;
		
		int x = OMathHelper.b(entity.getX());
		int y = OMathHelper.b(entity.getY()) - 1;
		int z = OMathHelper.b(entity.getZ());
		
		World world = entity.getWorld();
		int worldType = world.getType().getId();
		
		Block block = world.getBlockAt(x, y, z);
		
		if(BlockType.canPassThrough(block.getType()))
			return;
		
		double applyForce = 0;
		if(icAreas != null && icAreas.size() > 0 && (allowedICBlocks == null || allowedICBlocks.contains(block.getType())))
		{
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it = icAreas.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(worldType, x, y, z))
				{
					SignText text = CraftBook.getSignText(world, item.getKey());
					if(text == null)
					{
						it.remove();
						continue;
					}
					String line2 = text.getLine2();
			    	if(!line2.startsWith("[MC") || line2.length() < 8)
			    	{
						it.remove();
						continue;
					}
			    	
			    	String id = line2.substring(1, 7).toUpperCase();
					if(id.equals("MCU300"))
					{
						applyForce = MCX300.getForce(text);
					}
					else
					{
						it.remove();
						continue;
					}
					
					break;
				}
			}
		}
		
		if(applyForce == 0
			&& (block.getType() == blockBounce[0] && (blockBounce[1] == -1 || block.getData() == blockBounce[1]) )
			&& (block.isPowered() || block.isIndirectlyPowered())
			)
		{
			//bounce block
			applyForce = force;
		}
		
		if(applyForce != 0)
		{
			OEntity oentity = entity.getEntity();
			oentity.aT = applyForce;
			etc.getMCServer().f.a(new OPacket28EntityVelocity(oentity), worldType);
		}
	}
	
	protected static boolean fallProtected(BaseEntity entity, int amount)
	{
		int x = OMathHelper.b(entity.getX());
		int y = OMathHelper.b(entity.getY()) - 1;
		int z = OMathHelper.b(entity.getZ());
		
		Block block = entity.getWorld().getBlockAt(x, y, z);
		
		if(BlockType.canPassThrough(block.getType()))
			return false;
		
		if( (
			(block.getType() == blockBounce[0] && (blockBounce[1] == -1 || block.getData() == blockBounce[1]) )
			|| (block.getType() == blockSoft[0] && (blockSoft[1] == -1 || block.getData() == blockSoft[1]) )
			)
			&& (block.isPowered() || block.isIndirectlyPowered())
			)
		{
			//a bounce or soft block
			return true;
		}
		else if(allowedICBlocks == null || allowedICBlocks.contains(block.getType()))
		{
			//check ic area list
			int worldType = entity.getWorld().getType().getId();
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it;
			
			if(icAreas != null && icAreas.size() > 0)
			{
				it = icAreas.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
					BlockArea area = item.getValue();
					if(area.containsPoint(worldType, x, y, z))
					{
						return true;
					}
				}
			}
			
			if(icSoftAreas != null && icSoftAreas.size() > 0)
			{
				it = icSoftAreas.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
					BlockArea area = item.getValue();
					if(area.containsPoint(worldType, x, y, z))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}