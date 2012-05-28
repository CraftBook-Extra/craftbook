import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Pulse IC
 * 
 * When input 1 goes high set the output high then low.
 * 
 * @author Drathus
 */
public class MCX010 extends BaseIC {

	@Override
	public String getTitle() {

		return "PULSE";
	}

    @Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
    	
        if (sign.getLine3().length() != 0 || sign.getLine4().length() != 0) {
        	return "Lines three and four should be left empty.";
        }
    	
    	return null;
    }
	
	@Override
	public void think(ChipState chip) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);

		if (chip.getIn(1).is() == true) {
			
			chip.getOut(1).set(true);

			etc.getServer().addToServerQueue(new toggleOff(world, lever), 250);
		}
	}
	
	private class toggleOff implements Runnable {
		private final World world;
		private final Vector lever;
		
		public toggleOff(World world, Vector lever) {
			
			this.world = world;
			this.lever = lever;
		}
		
		@Override
		public void run() {
			
			if (world != null && lever != null) {
				Redstone.setOutput(CraftBook.getCBWorld(world), lever, false);
			}
		}
	}
}
