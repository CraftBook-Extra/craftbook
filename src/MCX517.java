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

import com.sk89q.craftbook.ic.*;

/**
 * server log nearby
 *
 * @author sk89q
 */
public class MCX517 extends MCX516 {
	
	private final String TITLE = "S-LOG NEARBY+";
	
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return TITLE+distance;
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
        
        processMessage(chip.getText().getLine3()+""+chip.getText().getLine4(),
        				chip.getBlockPosition(),
        				chip.getCBWorld(),
        				Integer.parseInt(chip.getText().getLine1().substring(TITLE.length())),
        				chip.getMode() == '+',
        				true);

        chip.getOut(1).set(true);
    }
}
