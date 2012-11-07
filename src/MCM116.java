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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Animal Above - Variant of MCX116 Player Above?
 * 
 * Lots of specific name checking due to inclusion of custom group "golems" as well as
 * two alternate pig detectors:
 * 
 * saddledpig - Matches a pig with a saddle
 * riddenpig  - Matches a pig with a saddle and a rider - Used on MinecraftOnline.com for Pigchinko!
 * 
 * @author drathus
 */
public class MCM116 extends BaseIC {
	/**
	 * Get the title of the IC.
	 * 
	 * @return
	 */
	public String getTitle() {
		return "MOB ABOVE?";
	}

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
	public String validateEnvironment(int worldType, Vector pos, SignText sign) {
		String id = sign.getLine3().toLowerCase();

		if (!id.equalsIgnoreCase("animal") &&
				!id.equalsIgnoreCase("mob") &&
				!id.equalsIgnoreCase("golem") &&
				!id.equalsIgnoreCase("chicken") &&
				!id.equalsIgnoreCase("cow") &&
				!id.equalsIgnoreCase("mooshroom") &&
				!id.equalsIgnoreCase("ocelot") &&
				!id.equalsIgnoreCase("pig") &&
				!id.equalsIgnoreCase("squid") &&
				!id.equalsIgnoreCase("wolf") &&
				!id.equalsIgnoreCase("blaze") &&
				!id.equalsIgnoreCase("creeper") &&
				!id.equalsIgnoreCase("enderman") &&
				!id.equalsIgnoreCase("ghast") &&
				!id.equalsIgnoreCase("giant") &&
				!id.equalsIgnoreCase("magmacube") &&
				!id.equalsIgnoreCase("pigzombie") &&
				!id.equalsIgnoreCase("silverfish") &&
				!id.equalsIgnoreCase("skeleton") &&
				!id.equalsIgnoreCase("slime") &&
				!id.equalsIgnoreCase("slime") &&
				!id.equalsIgnoreCase("spider") &&
				!id.equalsIgnoreCase("zombie") &&
				!id.equalsIgnoreCase("irongolem") &&
				!id.equalsIgnoreCase("snowgolem") &&
				!id.equalsIgnoreCase("villager") &&
				!id.equalsIgnoreCase("riddenpig") &&
				!id.equalsIgnoreCase("saddledpig")) {
			return "Invalid mob type.";
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
	public void think(ChipState chip) {

		if (chip.inputAmount() == 0 || (chip.getIn(1).is() && chip.getIn(1).isTriggered())) {
			findMobAbove(chip);
		}
	}

	protected void findMobAbove(ChipState chip) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		String id = chip.getText().getLine3().toLowerCase();

		int x = chip.getBlockPosition().getBlockX();
		int z = chip.getBlockPosition().getBlockZ();

		int y = getSafeY(world, chip.getBlockPosition());

		Vector lever = Util.getWallSignBack(world, chip.getPosition(), 2);
		FindMobAbove findAbove = new FindMobAbove(world, x, y, z, lever, id);
		etc.getServer().addToServerQueue(findAbove);
	}

	private int getSafeY(World world, Vector pos) {
		int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
		int x = pos.getBlockX();
		int z = pos.getBlockZ();

		for (int y = pos.getBlockY() + 1; y <= maxY; y++) {
			if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)) && y < CraftBook.MAP_BLOCK_HEIGHT && BlockType.canPassThrough(CraftBook.getBlockID(world, x, y + 1, z))) {
				return y;
			}
		}

		return maxY;
	}

	public class FindMobAbove implements Runnable {
		private final World WORLD;
		private final int X;
		private final int Y;
		private final int Z;
		private final Vector LEVER;
		private final String ID;

		public FindMobAbove(World world, int x, int y, int z, Vector lever, String id) {
			WORLD = world;
			X = x;
			Y = y;
			Z = z;
			LEVER = lever;
			ID = id;
		}

		/*
		 * Given a string, see if the entity matches
		 * 
		 * Take into account custom types like "RiddenPig"
		 */
		private boolean entityIsNamed(String line, OEntity oent) {
			boolean match = false;

			if (!(oent instanceof OEntity)) {
				match = false;
			} else {
				// Default to an empty line matching all
				if (line == null || line.length() == 0) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("pig")) && (oent instanceof OEntityPig)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("chicken")) && (oent instanceof OEntityChicken)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("cow")) && (oent instanceof OEntityCow)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("mooshroom")) && (oent instanceof OEntityMooshroom)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("ocelot")) && (oent instanceof OEntityOcelot)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("sheep")) && (oent instanceof OEntitySheep)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("squid")) && (oent instanceof OEntitySquid)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("wolf")) && (oent instanceof OEntityWolf)) {
					match = true;
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("riddenpig")) && (oent instanceof OEntityPig)) {
					OEntityPig op = (OEntityPig) oent;
					// Check for saddle
					if (op.m() == true && oent.n != null) {
						match = true;
					}
				} else if ((line.equalsIgnoreCase("animal") || line.equalsIgnoreCase("saddledpig")) && (oent instanceof OEntityPig)) {
					OEntityPig op = (OEntityPig) oent;
					if (op.m() == true) {
						match = true;
					}
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("blaze")) && (oent instanceof OEntityBlaze)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("cavespider")) && (oent instanceof OEntityCaveSpider)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("creeper")) && (oent instanceof OEntityCreeper)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("enderman")) && (oent instanceof OEntityEnderman)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("ghast")) && (oent instanceof OEntityGhast)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("giant")) && (oent instanceof OEntityGiantZombie)) {
					match = true;
				} else if ((line.equalsIgnoreCase("golem") || line.equalsIgnoreCase("irongolem")) && (oent instanceof OEntityIronGolem)) {
					match = true;
				} else if ((line.equalsIgnoreCase("golem") || line.equalsIgnoreCase("snowgolem")) && (oent instanceof OEntitySnowman)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("magmacube")) && (oent instanceof OEntityMagmaCube)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("pigzombie")) && (oent instanceof OEntityPigZombie)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("silverfish")) && (oent instanceof OEntitySilverfish)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("skeleton")) && (oent instanceof OEntitySkeleton)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("slime")) && (oent instanceof OEntitySlime)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("spider")) && (oent instanceof OEntitySpider)) {
					match = true;
				} else if ((line.equalsIgnoreCase("mob") || line.equalsIgnoreCase("zombie")) && (oent instanceof OEntityZombie)) {
					match = true;
				} else if (line.equalsIgnoreCase("villager") && (oent instanceof OEntityVillager)) {
					match = true;
				}
			}
			return match;
		}
		
		@Override
		public void run() {
			List<LivingEntity> entities = WORLD.getLivingEntityList();
			LivingEntity aboveEnt = null;
			
			for (LivingEntity ent : entities) {
				Vector mVec = new Vector(ent.getX(), ent.getY(), ent.getZ());

				if ((mVec.getBlockX() == X || mVec.getBlockX() == X + 1 || mVec.getBlockX() == X - 1) && mVec.getBlockY() == Y && (mVec.getBlockZ() == Z || mVec.getBlockZ() == Z + 1 || mVec.getBlockZ() == Z - 1)) {
					OEntity oent = ent.getEntity();
					// First make sure we're not going to trip for players.
					if (oent instanceof OEntityPlayer) {
						continue;
					} else if (entityIsNamed(ID, oent)) {
						aboveEnt = ent;
						break;
					} else {
						continue;
					}
				}
			}

			boolean output = aboveEnt != null && !UtilEntity.isDead(aboveEnt.getEntity());

			Redstone.setOutput(CraftBook.getCBWorld(WORLD), LEVER, output);
		}
	}
}