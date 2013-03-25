import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sk89q.craftbook.BlockArea;
import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.WorldBlockVector;


public class Bounce
{
	protected static Map<WorldBlockVector, BlockArea> icAreas = new HashMap<WorldBlockVector, BlockArea>();
	protected static Map<WorldBlockVector, BlockArea> icRepelAreas = new HashMap<WorldBlockVector, BlockArea>();
	protected static Map<WorldBlockVector, BlockArea> icSoftAreas = new HashMap<WorldBlockVector, BlockArea>();
	
	protected static int[] blockBounce = new int[]{0, -1};
	protected static int[] blockSoft = new int[]{0, -1};
	protected static int force = 3;
	
	protected static boolean bounceCreatures = false;
	
	protected static int maxICForce = 8;
	public static ArrayList<Integer> allowedICBlocks;
	
	/**
	 * 
	 * @param entity
	 * @param from
	 * @param to
	 * @return
	 */
	protected static boolean bounce(BaseEntity entity, Location from, Location to)
	{
		if(from.y <= to.y)
			return false;
		
		int x = OMathHelper.c(entity.getX());
		int y = OMathHelper.c(entity.getY()) - 1;
		int z = OMathHelper.c(entity.getZ());
		
		World world = entity.getWorld();
		CraftBookWorld cbworld = CraftBook.getCBWorld(world);
		
		Block block = world.getBlockAt(x, y, z);
		
		if(BlockType.canPassThrough(block.getType()))
			return false;
		
		double applyForce = 0;
		if(icAreas != null && icAreas.size() > 0 && (allowedICBlocks == null || allowedICBlocks.contains(block.getType())))
		{
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it = icAreas.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(cbworld, x, y, z))
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
						if(applyForce > maxICForce)
							applyForce = maxICForce;
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
		    entity.setMotionY(applyForce);
		    //Util.sendPacketToDimension(new OPacket28EntityVelocity(entity.getEntity()), cbworld);
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param entity
	 * @param from
	 * @param to
	 */
	protected static void repel(BaseEntity entity, Location from, Location to)
	{
		if(icRepelAreas != null && icRepelAreas.size() > 0)
		{
			int x = OMathHelper.c(entity.getX());
			int y = OMathHelper.c(entity.getY());
			int z = OMathHelper.c(entity.getZ());
			
			World world = entity.getWorld();
			CraftBookWorld cbworld = CraftBook.getCBWorld(world);
			
			double[] applyForce = null;
			
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it = icRepelAreas.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(cbworld, x, y, z))
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
					if(id.equals("MCU302") || id.equals("MCU303"))
					{
						applyForce = MCX302.getForces(text);
						
						if(applyForce != null)
						{
							if(applyForce[0] > maxICForce)
								applyForce[0] = maxICForce;
							else if(applyForce[0] < -maxICForce)
								applyForce[0] = -maxICForce;
							if(applyForce[1] > maxICForce)
								applyForce[1] = maxICForce;
							else if(applyForce[1] < -maxICForce)
								applyForce[1] = -maxICForce;
							if(applyForce[2] > maxICForce)
								applyForce[2] = maxICForce;
							else if(applyForce[2] < -maxICForce)
								applyForce[2] = -maxICForce;
						}
					}
					else
					{
						it.remove();
						continue;
					}
					
					break;
				}
			}
			
			if(applyForce != null)
			{
				entity.setMotion(applyForce[0], applyForce[1], applyForce[2]);
			}
		}
	}
	
	
	/**
	 * 
	 * @param entity
	 * @param amount
	 * @return
	 */
	protected static boolean fallProtected(BaseEntity entity, int amount)
	{
		int x = OMathHelper.c(entity.getX());
		int y = OMathHelper.c(entity.getY()) - 1;
		int z = OMathHelper.c(entity.getZ());
		
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
			CraftBookWorld cbworld = CraftBook.getCBWorld(entity.getWorld());
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it;
			
			if(icAreas != null && icAreas.size() > 0)
			{
				it = icAreas.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
					BlockArea area = item.getValue();
					if(area.containsPoint(cbworld, x, y, z))
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
					if(area.containsPoint(cbworld, x, y, z))
					{
						return true;
					}
				}
			}
		}
		
		if(icRepelAreas != null && icRepelAreas.size() > 0)
		{
			y = OMathHelper.c(entity.getY());
			
			block = entity.getWorld().getBlockAt(x, y, z);
			
			if(!BlockType.canPassThrough(block.getType()))
				return false;
			
			CraftBookWorld cbworld = CraftBook.getCBWorld(entity.getWorld());
			Iterator<Map.Entry<WorldBlockVector, BlockArea>> it;
			it = icRepelAreas.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(cbworld, x, y, z))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 *
	 */
	protected static void bounceCreatures()
	{
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
        	        if(entity != null && entity.getEntity() instanceof OEntityCreature)
                    {
                        OEntity oentity = entity.getEntity();
                        Location loc = new Location();
                        loc.x = UtilEntity.prevX(oentity);
                        loc.y = UtilEntity.prevY(oentity);
                        loc.z = UtilEntity.prevZ(oentity);
                        loc.rotX = entity.getRotation();
                        loc.rotY = entity.getPitch();
                        loc.dimension = entity.getWorld().getType().getId();
                        
                        Location loc2 = new Location();
                        loc.x = entity.getX();
                        loc.y = entity.getY();
                        loc.z = entity.getZ();
                        loc.rotX = entity.getRotation();
                        loc.rotY = entity.getPitch();
                        loc.dimension = entity.getWorld().getType().getId();
                        bounce(entity, loc, loc2);
                    }
        	    }
        	}
        }
	}
}
