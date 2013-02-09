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



import java.util.Set;

import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX117 extends MCX116 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "PLAYER MINE";
    }
    
	@Override
    public boolean requiresPermission() {
        return true;
    }

	@Override
	protected ResultHandler resultHandlerFactory(ChipState chip) {
		return new MCX117.ResultHandler(chip);
	}
	
	protected static class ResultHandler extends MCX116.ResultHandler {
		public ResultHandler(ChipState chip) {
			super(chip);
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					boolean found = false;
					BaseEntity abovePlayer = null;
					for (BaseEntity bEntity : foundEntities) {
						if (bEntity.isPlayer()) {
							abovePlayer = bEntity; 
							found = true;
							break;
						}
					}
					World world = CraftBook.getWorld(chip.getCBWorld());
					Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
					Redstone.setOutput(chip.getCBWorld(), lever, found);
					if (abovePlayer != null) {
						MC1250.explodeTNT(world, abovePlayer.getX(),
							abovePlayer.getY(), abovePlayer.getZ());
					}
				}
			});
		}
	}
}
