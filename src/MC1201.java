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
 * Dispenser.
 *
 * @author sk89q
 */
public class MC1201 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "DISPENSER";
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
        String id = sign.getLine3();

        if (id.length() == 0) {
            return "Specify a item type on the third line.";
        } else if (getItem(id) < 1) {
            return "Not a valid item type: " + sign.getLine3() + ".";
        }

        if (sign.getLine4().length() > 0) {
            try {
                Math.min(64, Math.max(-1, Integer.parseInt(sign.getLine4())));
            } catch (NumberFormatException e) {
                return "Not a valid quantity: " + sign.getLine4() + ".";
            }
        }

        return null;
    }

    /**
     * Get an item from its name or ID.
     * 
     * @param id
     * @return
     */
    private int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }

    /**
     * Think.
     *
     * @param chip
     */
    @Override
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
            return;
        }
        
        String id = chip.getText().getLine3();
        int quantity = 1;

        try {
            quantity = Math.min(64,
                    Math.max(-1, Integer.parseInt(chip.getText().getLine4())));
        } catch (NumberFormatException e) {
        }

        int item = getItem(id);

        Vector pos = chip.getBlockPosition();
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
        World world = CraftBook.getWorld(chip.getCBWorld());
        if (! world.isChunkLoaded(x, 0, z)) return;


        for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z))) {
                world.dropItem(x, y, z, item, quantity);
                return;
            }
        }
    }

}
