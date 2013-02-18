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
    	int pulse = 0;
    	
        if (sign.getLine3().length() == 0) {
        	pulse = 250;
        } else {
        	try {
        		pulse = Integer.parseInt(sign.getLine3());
        	} catch (NumberFormatException e) {
        		return "Invalid pulse length, must be a number between 100 and 1000";
        	}
        }

    	if (pulse < 100 || pulse > 1000) {
    		return "Invalid pulse length, valid range: 100-1000";
    	}
        
        if (sign.getLine4().length() != 0) {
        	return "Line four must be left blank!";
        }
        if (sign.getLine4().length() != 0) {
        	return "Line four should be left empty.";
        }
    	
    	return null;
    }
	
	@Override
	public void think(ChipState chip) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
		
		if (chip != null &&
			((chip.getIn(1) != null && chip.getIn(1).is()) || 
			(chip.getIn(2) != null && chip.getIn(2).is()) || 
			(chip.getIn(3) != null && chip.getIn(3).is()))) {
			
			int pulse;
			
        	try {
        		pulse = Integer.parseInt(chip.getText().getLine3());
        	} catch (NumberFormatException e) {
        		pulse = 250;
        	}
			
			chip.getOut(1).set(true);
			if (lever != null)
				etc.getServer().addToServerQueue(new toggleOff(world, lever), pulse);
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
