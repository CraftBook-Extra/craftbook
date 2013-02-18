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
 * Mob spawner.
 *
 * @author sk89q
 */
public class MC1200 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "MOB SPAWNER";
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
        String rider = sign.getLine4();

        if (id.length() == 0) {
            return "Specify a mob type on the third line.";
        } else if (!Mob.isValid(id)) {
            return "Not a valid mob type: " + id + ".";
        } else if (rider.length() != 0 && !Mob.isValid(rider)) {
            return "Not a valid rider type: " + rider + ".";
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
        if (chip.getIn(1).is()) {
            String id = chip.getText().getLine3();
            String rider = chip.getText().getLine4();
            
            if (Mob.isValid(id)) {
                Vector pos = chip.getBlockPosition();
                int x = pos.getBlockX();
                int z = pos.getBlockZ();
                if (!CraftBook.getWorld(chip.getCBWorld()).isChunkLoaded(x, 0, z)) return;
                
                int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
                for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
                    if (BlockType.canPassThrough(CraftBook.getBlockID(chip.getCBWorld(), x, y, z))) {
                        Mob mob = new Mob(id, CraftBook.getWorld(chip.getCBWorld()));
                        
                        Location loc = new Location(x, y, z);
                        loc.dimension = chip.getCBWorld().dimension();
                        mob.teleportTo(loc);
                        
                        if (rider.length() != 0 && Mob.isValid(rider)) {
                            mob.spawn(new Mob(rider, CraftBook.getWorld(chip.getCBWorld()) ));
                        } else {
                            mob.spawn();
                        }
                        return;
                    }
                }
            }
        }
    }
}
