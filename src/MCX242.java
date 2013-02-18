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

import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Shoots snowballs.
 *
 * @author sk89q
 */
public class MCX242 extends MC1240 {
    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "SNOW SHOOTER";
    }
    
    /**
     * Shoot the arrow.
     * 
     * @param chip
     * @param speed
     * @param spread
     * @param vertVel
     */
    @Override
    protected void shoot(ChipState chip, float speed, float spread, float vertVel) {
        Vector backDir = chip.getBlockPosition().subtract(
                chip.getPosition());
        Vector firePos = chip.getBlockPosition().add(backDir);
        World world = CraftBook.getWorld(chip.getCBWorld());
        Snowball arrow = new Snowball(world);
        arrow.teleportTo(firePos.getBlockX(), //+ 0.5,
                        firePos.getBlockY() + 0.5,
                        firePos.getBlockZ(), //+ 0.5,
                        0, 0);
        arrow.spawn();
        UtilEntity.setThrowableHeading(arrow.getEntity(), backDir.getBlockX(), vertVel, backDir.getBlockZ(), speed, spread);
    }
}
