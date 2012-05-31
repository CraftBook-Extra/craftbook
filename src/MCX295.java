import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/*
 * Reads the state of a lever or button at a 
 * specified relative position, and mimics the
 * output state.
 *
 * @author drathus
 */
public class MCX295 extends BaseIC {
	
	public String getTitle() {
		return "TRIGGER READER";
	}

    public boolean requiresPermission() {
        return true;
    }
	
    public String validateEnvironment(int worldType, Vector pos, SignText sign) {
    	String[] targetRelative = sign.getLine3().split(":");
    	int x;
    	int y;
    	int z;
    	
    	if (targetRelative.length != 3) {
    		return "Invalid relative location, use XX:YY:ZZ";
    	} else {
    		if (targetRelative[0].startsWith("!")) {
    			targetRelative[0] = targetRelative[0].substring(1);
    		}
    		
    		try {
	    		x = Integer.parseInt(targetRelative[0]);
	    		y = Integer.parseInt(targetRelative[1]);
	    		z = Integer.parseInt(targetRelative[2]);
    		} catch (NumberFormatException e) {
    			return "Invalid value found for X:Y:Z offset";
    		}
	    		
    		if (x < -32 || x > 32) {
    			return "X range must be +/- 32";
    		}
    		if (z < -32 || z > 32) {
    			return "Z range must be +/- 32";
    		}
    		if (y < -255 || y > 255) {
    			return "Y range must be +/- 255";
    		}
    	}
    	
    	return null;
    }
    
	@Override
	public void think(ChipState chip) {
		int x;
		int y;
		int z;
		
		if(chip.inputAmount() != 0 && !chip.getIn(1).is()) {
    		return;
    	}

		String[] targetRelative = chip.getText().getLine3().split(":");
		boolean invert = false;
		
		if (targetRelative.length < 3) return;
		
		if (targetRelative[0].startsWith("!")) {
			invert = true;
			targetRelative[0] = targetRelative[0].substring(1);
		}
		
		try {
			x = Integer.parseInt(targetRelative[0]);
			y = Integer.parseInt(targetRelative[1]);
			z = Integer.parseInt(targetRelative[2]);
		} catch (NumberFormatException e) {
			return;
		}
		
		if (x < -32 || x > 32 || z < -32 || z > 32 || y < -255 || y > 255) {
			return;
		}

		mirrorRSState(chip, x, y, z, invert);		
	}
	
	protected void mirrorRSState(ChipState chip, int x, int y, int z, boolean invert) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		int tX = chip.getPosition().getBlockX() + x;
		int tY = chip.getPosition().getBlockY() + y;
		int tZ = chip.getPosition().getBlockZ() + z;
		Block block = world.getBlockAt(tX, tY, tZ);
		boolean chipRS = false;
		
		// Make sure block is a lever, button, stone or wood pressure plate.
		if (block.getType() != 69 && block.getType() != 77 && block.getType() != 70 && block.getType() != 72) {
			return;
		}
		
		// We have a target.  Now read the target block's RS state for powered or not.
		int state = world.getBlockData(tX, tY, tZ);
		
		if (block.getType() == 70 || block.getType() == 72) {
			if (state == 1) {
				chipRS = true;
			}
		} else {
			if ((state & 8) == 8) {
				chipRS = true;
			}
		}

		if (invert) {
			chip.getOut(1).set(!chipRS);
		} else {
			chip.getOut(1).set(chipRS);
		}
	}
}