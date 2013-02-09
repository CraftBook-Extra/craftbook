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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.Vector;


/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MCX130 extends MCX119 {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
	@Override
    public String getTitle() {
        return "MOB ZAPPER";
    }
    
	@Override
    public boolean requiresPermission() {
        return true;
    }
    
	@Override
	protected CBXEntityFinder.ResultHandler resultHandlerFactory(CraftBookWorld cbworld, Vector lever) {
		return new MCX130.ResultHandler(cbworld, lever);
	}
	
	public static class ResultHandler extends MCX119.ResultHandler {
		public ResultHandler(CraftBookWorld cbworld, Vector lever) {
			super(cbworld, lever);
		}
		
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					boolean found = false;
					try {
					if (!foundEntities.isEmpty()) {
						for (BaseEntity bEntity:foundEntities) {
							if (bEntity != null && !bEntity.isDead() && !bEntity.isPlayer()) {
								bEntity.destroy();
								found = true;
							} else {
								if (bEntity.isPlayer()) {
									Logger.getLogger("Minecraft.CraftBook").log(Level.WARNING, "MCX130.ResultHandler tried to kill a player!");
								}
							}
						}
					}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Redstone.setOutput(cbworld, lever, found);
				}
			});
		}
	}
}
