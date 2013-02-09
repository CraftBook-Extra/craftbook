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

import java.util.List;
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
			int safeY = getSafeYAbove(chip.getCBWorld(), chip.getBlockPosition());
			Vector searchCenter = new Vector(chip.getBlockPosition().getX() + 0.5, safeY, chip.getBlockPosition().getZ() + 0.5);
			// prepare and run CBXEntityFinder
			CBXEntityFinder playerAboveFinder = new CBXEntityFinder(chip.getCBWorld(), searchCenter, 2, new ResultHandler(chip));
			playerAboveFinder.setDistanceCalculationMethod(new CBXinRangeCuboid(1.5, 0.2, 1.5));
			playerAboveFinder.addPlayerFilterExtended(UtilIC.getSignTextWithExtension(chip).getLine3());
			playerAboveFinder.setModeOnlyPlayers();
			if (!CraftBook.cbxScheduler.isShutdown()) {
				try {
					CraftBook.cbxScheduler.execute(playerAboveFinder);
				} catch(RejectedExecutionException e) {
					// CraftBook is being disabled or restarted
				}
			}
		}
	}
	
	
	private static class ResultHandler implements CBXEntityFinder.ResultHandler {
		private final ChipState chip;

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
	
	// move to Util
	private int getSafeYAbove(CraftBookWorld cbworld, Vector pos) {
		return getSafeYAbove(CraftBook.getWorld(cbworld), pos);
	}
	// move to Util
	private int getSafeYAbove(World world, Vector pos) {
		int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
		int x = pos.getBlockX();
		int z = pos.getBlockZ();

		for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
			if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z))
					&& y < CraftBook.MAP_BLOCK_HEIGHT
					&& BlockType.canPassThrough(CraftBook.getBlockID(world, x,
							y + 1, z))) {
				return y;
			}
		}
		return maxY;
	}
	
	
	// old stuff below -----------------------------------------------------------------------
	@Deprecated
	protected void findPlayerAbove(ChipState chip, boolean tnt) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		SignText text = UtilIC.getSignTextWithExtension(chip);
		String id = text.getLine3().toLowerCase();

		int x = chip.getBlockPosition().getBlockX();
		int z = chip.getBlockPosition().getBlockZ();

		int y = getSafeYAbove(world, chip.getBlockPosition());

		Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
		FindPlayerAbove findAbove = new FindPlayerAbove(world, x, y, z, lever,
				id, tnt);
		etc.getServer().addToServerQueue(findAbove);
	}



	@Deprecated
	public class FindPlayerAbove implements Runnable {
		private final World WORLD;
		private final int X;
		private final int Y;
		private final int Z;
		private final Vector LEVER;
		private final String ID;
		private final boolean TNT;

		public FindPlayerAbove(World world, int x, int y, int z, Vector lever,
				String id, boolean tnt) {
			WORLD = world;
			X = x;
			Y = y;
			Z = z;
			LEVER = lever;
			ID = id;
			TNT = tnt;
		}

		@Override
		public void run() {
			List<Player> entities = etc.getServer().getPlayerList();
			Player abovePlayer = null;
			for (Player player : entities) {
				Location pLoc = player.getLocation();
				Vector pVec = new Vector(pLoc.x, pLoc.y, pLoc.z);

				if (player.getWorld() == WORLD
						&& (pVec.getBlockX() == X || pVec.getBlockX() == X + 1 || pVec
								.getBlockX() == X - 1)
						&& pVec.getBlockY() == Y
						&& (pVec.getBlockZ() == Z || pVec.getBlockZ() == Z + 1 || pVec
								.getBlockZ() == Z - 1)) {
					if (!ID.isEmpty()) {
						if (UtilEntity.isValidPlayerEntity(player, ID)) {
							abovePlayer = player;
							break;
						} else {
							// continue to check if another player happens to be
							// in the same area instead of stopping
							continue;
						}
					} else {
						abovePlayer = player;
						break;
					}
				}
			}

			boolean output = abovePlayer != null && !abovePlayer.isDead();
			if (TNT && output) {
				MC1250.explodeTNT(WORLD, abovePlayer.getX(),
						abovePlayer.getY(), abovePlayer.getZ());
			}

			Redstone.setOutput(CraftBook.getCBWorld(WORLD), LEVER, output);
		}
	}
}
