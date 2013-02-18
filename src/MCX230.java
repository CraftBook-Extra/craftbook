

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Takes in a clock input, and outputs whether it is raining or not.
 *
 * @author Shaun (sturmeh)
 */
public class MCX230 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "IS IT RAIN";
    }
    
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
        if (sign.getLine3().length() != 0) {
        	return "Third line needs to be blank";
        }

        if (sign.getLine4().length() != 0) {
            return "Fourth line needs to be blank";
        }

        return null;
    }

    /**
     * Think.
     * 
     * @param chip
     */
	@Override
    public void think(ChipState chip)
    {
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	chip.getOut(1).set(world.isRaining());
    }
}
