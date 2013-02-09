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
import java.util.concurrent.RejectedExecutionException;

import cbx.CBXinRangeCuboid;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Wireless transmitter.
 * 
 * @author sk89q
 */
public class MCX116 extends BaseIC {

	/**
	 * Get the title of the IC.
	 * 
	 * @return
	 */
	@Override
	public String getTitle() {
		return "PLAYER ABOVE?";
	}

	@Override
	public boolean requiresPermission() {
		return true;
	}

	/**
	 * Validates the IC's environment. The position of the sign is given. Return
	 * a string in order to state an error message and deny creation, otherwise
	 * return null to allow.
	 * 
	 * @param sign
	 * @return
	 */
	@Override
	public String validateEnvironment(CraftBookWorld cbworld, Vector pos,
			SignText sign) {

		sign = UtilIC.getSignTextWithExtension(cbworld, pos, sign);

		if (!sign.getLine3().isEmpty()) {
			String[] values = sign.getLine3().split(":", 2);
			if (!UtilEntity.isValidPlayerTypeID(values[0])) {
				return "Invalid player or group name on Third line.";
			}
		}

		if (sign.getLine4().length() != 0) {
			return "Fourth line must be blank.";
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

		if (chip.inputAmount() == 0
				|| (chip.getIn(1).is() && chip.getIn(1).isTriggered())) {
			// find where above the sign to look for the player
			double safeY = Util.getSafeYAbove(chip.getCBWorld(), chip.getBlockPosition());
			Vector searchCenter = new Vector(chip.getBlockPosition().getX() + 0.5, safeY, chip.getBlockPosition().getZ() + 0.5);
			// prepare and run CBXEntityFinder
			CBXEntityFinder playerAboveFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, 2, resultHandlerFactory(chip));
			playerAboveFinder.setDistanceCalculationMethod(new CBXinRangeCuboid(1.5, 0.2, 1.5));
			playerAboveFinder.addPlayerFilterExtended(UtilIC.getSignTextWithExtension(chip).getLine3());
			if (!CraftBook.cbxScheduler.isShutdown()) {
				try {
					CraftBook.cbxScheduler.execute(playerAboveFinder);
				} catch(RejectedExecutionException e) {
					// CraftBook is being disabled or restarted
				}
			}
		}
	}
	
	protected ResultHandler resultHandlerFactory(ChipState chip) {
		return new ResultHandler(chip);
	}
	
	public static class ResultHandler implements CBXEntityFinder.ResultHandler {
		protected final ChipState chip;

		public ResultHandler(ChipState chip) {
			this.chip = chip;
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					boolean found = false;
					for (BaseEntity bEntity : foundEntities) {
						if (bEntity.isPlayer()) {
							found = true;
							break;
						}
					}
					World world = CraftBook.getWorld(chip.getCBWorld());
					Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
					Redstone.setOutput(chip.getCBWorld(), lever, found);
				}
			});
		}
	}
	
}
