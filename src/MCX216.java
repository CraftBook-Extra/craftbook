import java.util.Set;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sapling planter
 * 
 * Hybrid variant of MCX206 and MCX203 chest collector
 * 
 * When there is a sapling or seed item drop in range it will auto plant it above the IC.
 * 
 * @author Drathus
 *
 */
public class MCX216 extends CBXEntityFindingIC {
	private static final double SEARCH_DIST = 3;
	
	@Override
	public String getTitle() {

		return "PLANTER";
	}

	@Override
    public boolean requiresPermission() {
        return true;
    }
	
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
		
        if (sign.getLine3().length() == 0) {
        	return "Line 3 should contain itemid[@damage] to target";
        } else {
        	String[] lineParts = sign.getLine3().split(":");
        	int[] info = UtilItem.getItemInfoFromParts(lineParts);
        	
        	if(info == null || !plantableItem(info[0])) {
        		return "No plantable item specified";
        	}
        }
		
        if (sign.getLine4().length() != 0) {
        	int yOffset = 0;
        	try {
        		yOffset = Integer.parseInt(sign.getLine4());
        	} catch (NumberFormatException e) {
        		return "Line four must contain a numeric value for a Y offset.";
        	}
        	if (yOffset < 1) {
        		return "Y Offset must be positive";
        	}
        }
		
		return null;
	}
	
	@Override
	public void think(ChipState chip) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		Vector onBlock = Util.getWallSignBack(world, chip.getPosition(), 1);
		if (onBlock == null) return;
		Vector target = null;
		int[] info = null;
		int yOffset = 0;
		
        if (chip.getText().getLine3().length() != 0) {
        	String[] lineParts = chip.getText().getLine3().split(":");
        	info = UtilItem.getItemInfoFromParts(lineParts);
        }
        
    	if(info == null || !plantableItem(info[0])) {
    		return;
    	}

    	if (chip.getText().getLine4().length() != 0) {
	    	try {
	    		yOffset = Integer.parseInt(chip.getText().getLine4());
	    	} catch (NumberFormatException e) {
	    		return;
	    	}
	    	if (yOffset < 1) {
	    		return;
	    	}
    	} else {
    		yOffset = 1;
    	}
    	
    	target = onBlock.add(0, yOffset, 0);
    	
		if (world.getBlockIdAt(target.getBlockX(), target.getBlockY(), target.getBlockZ()) == 0)
		{
				
			if (info[0]==127)
			{ 	//this block if the planted seed is cocoa
				//the cocoa beans is ID 351:3 which is the brown dye
				//the cocoa sapling when planted is 127, so still confused which to use
				FakeData fdata = fakeData.get(world.getWorld()); // for use in the meta data finding sub routine
				
				for (int diffX = -1 ; diffX < 2 ; diffX++)
				{
					for (int diffZ = -1 ; diffZ <2 ; diffZ++)
					{	
						if((diffX == 0) && (diffZ == 0))
						{
							;	
						}
						else if(itemPlantableOnBlock(info[0], world.getBlockIdAt(target.getBlockX() + diffX, target.getBlockY() - 1, target.getBlockZ() - diffZ)))
						{
							// Assuming getBlockData returns the meta/damage value of the block, so 3 incase of jungle wood
							int metavalue = 0;
        						if (fdata != null && fdata.pos.equals(new BlockVector(world.getBlockIdAt(target.getBlockX() + diffX, target.getBlockY() - 1, target.getBlockZ() - diffZ))))
        						{
            							metavalue = fdata.val;
        						}else
        						{
        							metavalue = world.getBlockData(target.getBlockX() + diffX, target.getBlockY() - 1, target.getBlockZ() - diffZ);
        						}
							if(3 == metavalue)
							{
								Vector searchCenter = target.add(0.5, 0, 0.5);
								CBXEntityFinder.ResultHandler rhPlanter = new RHPlanter(world, target, info[0], info[1], chip);
								CBXEntityFinder toPlantFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, SEARCH_DIST, rhPlanter);
								toPlantFinder.addItemFilter(info[0], info[1]);
								CraftBook.cbxScheduler.execute(toPlantFinder);
								break;
							}
						}
					}
				}
			}
			else if (itemPlantableOnBlock(info[0], world.getBlockIdAt(target.getBlockX(), target.getBlockY() - 1, target.getBlockZ()))) 
			{
				Vector searchCenter = target.add(0.5, 0, 0.5);
				CBXEntityFinder.ResultHandler rhPlanter = new RHPlanter(world, target, info[0], info[1], chip);
				CBXEntityFinder toPlantFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, SEARCH_DIST, rhPlanter);
				toPlantFinder.addItemFilter(info[0], info[1]);
				CraftBook.cbxScheduler.execute(toPlantFinder);
			}
		}
	}

	private boolean plantableItem(int itemId) { // 127 for cocoa bean and not the brown dye
		return (itemId == 6 || itemId == 127 || itemId == 295 || itemId == 372 || itemId == 391 || itemId == 392); 
	}
	
	private boolean itemPlantableOnBlock(int itemId, int blockId) {
		if (itemId == 6 && (blockId == 2 || blockId == 3)) {
			// Saplings can go on Dirt or Grass
			return true;
		} else if ( (itemId == 295 || itemId == 391 || itemId == 392) && blockId == 60) {
			// Seeds, carrots, and potatoes can only go on farmland
			return true;
		} else if (itemId == 372 && blockId == 88) {
			// Netherwart on soulsand
			return true;
		} else if (itemId == 127 && blockId == 17){// 127 for cocoa bean and not the brown dye
							   // and block == 17 for log, type 3 for jungle is checked in the calling method
			return true;
		}
		// can't plant this item on this block
		return false;
	}
	
	/**
	 * Plants an item from the world on the target block.
	 * Sets output to high when successful, low when no suitable item was found.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 */
	public static class RHPlanter extends CBXEntityFindingIC.ResultHandlerWithOutput {
		private final World world;
		private final Vector target;
		private final int itemId;
		private final int damVal;
		
		public RHPlanter(World world, Vector target, int itemId, int damVal, ChipState chip) {
			super(chip);
			this.world = world;
			this.target = target;
			this.itemId = itemId;
			this.damVal = damVal;
		}

		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					try {
						boolean found = false;
						for (BaseEntity bEntity : foundEntities) {
							if (bEntity != null 
									&& bEntity.getWorld().isChunkLoaded((int)bEntity.getX(), (int)bEntity.getY(), (int)bEntity.getZ())
									&& !bEntity.isDead()) {
								// looks valid, let's try to plant it
								found = plant(bEntity);
								if (found) break;
							}
						}
						setOutput(found);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		
		private boolean plant(BaseEntity bEntity) {
			if (! (bEntity.getEntity() instanceof OEntityItem)) {
				return false;
			}
			// get the item stack
			ItemEntity itemEnt = new ItemEntity((OEntityItem) bEntity.getEntity());
            Item itemStack = itemEnt.getItem();
        	
            // This should always be true if the toPlantFinder has done his work properly,
            // but unsynchronized multithreading can have strange effects, so we'd better check again.
        	if(itemStack.getAmount() > 0 
        			&& itemStack.getItemId() == this.itemId 
        			&& (this.damVal < 0 || itemStack.getDamage() == this.damVal)) {
        		
        		// get one item from the stack
				itemStack.setAmount(itemStack.getAmount() - 1);
				if (itemStack.getAmount() < 1) {
					itemEnt.destroy();
				}
				// and plant it
				world.setBlockAt(getBlockByItem(this.itemId), target.getBlockX(), target.getBlockY(), target.getBlockZ());
				world.setBlockData(target.getBlockX(), target.getBlockY(), target.getBlockZ(), itemStack.getDamage());
				return true;
        	}
			return false;
			
		}
		
		/**
		 * @param itemId id of the item to plant
		 * @return block ID to place to simulate planting
		 */
		private int getBlockByItem(int itemId) {
            switch (itemId) {
            case 295: return 59;
            case   6: return 6;
            case 372: return 115;
            case 391: return 141;
            case 392: return 142;
            default : return 0;
            }
		}
	}
	
}
