

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

public abstract class CBXEntityFindingIC extends BaseIC {

	public static abstract class ResultHandlerWithOutput implements CBXEntityFinder.ResultHandler {
		protected final ChipState chip;
		
		public ResultHandlerWithOutput(ChipState chip) {
			this.chip = chip;
		}
		
		public void setOutput(boolean state) {
			World world = CraftBook.getWorld(chip.getCBWorld());
			Vector chipPos = chip.getBlockPosition();
	        if (! world.isChunkLoaded(chipPos.getBlockX(), chipPos.getBlockY(), chipPos.getBlockZ())) return;
			Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
			Redstone.setOutput(chip.getCBWorld(), lever, state);
		}
	}
	
	public static class RHSetOutIfFound extends ResultHandlerWithOutput {
		public RHSetOutIfFound(ChipState chip) {
			super(chip);
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					try {
						boolean found = false;
						for (BaseEntity bEntity : foundEntities) {
							if (bEntity != null 
									&& !bEntity.isDead()) {
								found = true;
								break;
							}
						}
						setOutput(found);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public static class RHDestroyFoundEntities extends RHSetOutIfFound {
		public RHDestroyFoundEntities(ChipState chip) {
			super(chip);
		}
		
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					boolean found = false;
					try {
						for (BaseEntity bEntity : foundEntities) {
							if (bEntity != null && !bEntity.isDead() && !bEntity.isPlayer()) {
								bEntity.destroy();
								found = true;
							} else {
								if (bEntity.isPlayer()) {
									Logger.getLogger("Minecraft.CraftBook").log(Level.WARNING, "CBXEntityFindingIC.RHDestroyFoundEntities tried to kill a player!");
								}
							}
						}
						setOutput(found);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	/**
	 * This facilitates inheritance in CraftBook's strange IC class model 
	 */
	protected static interface RHWithOutputFactory {
		public ResultHandlerWithOutput rhFactory(ChipState chip); 
	}
	
}
