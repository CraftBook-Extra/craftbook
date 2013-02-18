// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Server log
 *
 * @author sk89q
 */
public class MCX515 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "SERVER LOG";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
	@Override
    public boolean requiresPermission() {
        return true;
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
        if(sign.getLine3().isEmpty() && sign.getLine4().isEmpty())
        {
        	return "A message on the 3rd or 4th line is required.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
	@Override
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
        	chip.getOut(1).set(false);
            return;
        }
        
        CraftBookListener.logger.info("[CB!] "+chip.getText().getLine3()+""+chip.getText().getLine4());

        chip.getOut(1).set(true);
    }
}
