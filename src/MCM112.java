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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.WorldBlockVector;
import com.sk89q.craftbook.WorldLocation;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Pig Pad Transporter
 * 
 * For Pigchinko, but could be adapted for other uses.
 *
 * This only teleports Pigs which have a Saddle and Rider both.
 *
 * @author drathus
 */
public class MCM112 extends CBXEntityFindingIC implements CBXEntityFindingIC.RHWithOutputFactory {
    

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "PIGTRANSPORTER";
    }
    
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
            return "Specify a band name on the third line.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
    	if (! chip.getIn(1).is()) {
    		return;
    	}
        CBXEntityFinder.ResultHandler pigTransporter = rhFactory(chip);
        if (pigTransporter == null) {
        	chip.getOut(1).set(false);
        	return;
        }
        double maxDistance = 2.0;
        CBXEntityFinder pigFinder = new CBXEntityFinder(chip.getCBWorld(), getSearchCenter(chip), maxDistance, pigTransporter);
        pigFinder.setDistanceCalculationMethod(new CBXinRangeCuboid(1.5, 0.2, 1.5));
        pigFinder.addAnimalFilter("Pig");
        if (!CraftBook.cbxScheduler.isShutdown()) {
        	try {
        		CraftBook.cbxScheduler.execute(pigFinder);
        	} catch (RejectedExecutionException e) {
        		// CraftBook is being disabled or reloaded
        	}
        }
    }
    
    
    @Override
    public ResultHandlerWithOutput rhFactory(ChipState chip) {
    	String[] msg;
    	String id = chip.getText().getLine3();
    	WorldLocation dest = null;
    	// find teleport destination
        if (!id.isEmpty()) {
            dest = MCX113.airwaves.get(id);
            if (dest == null) {
                return null;
            } else {
            	dest = dest.add(0.5D, 0.0D, 0.5D);
            }
        }
        // prepare message to send to the player when teleporting
		if(chip.getText().getLine4().length() == 0) {
			msg = new String[]{"Woosh!"};
		} else {
			msg = new String[]{chip.getText().getLine4()};
		}
    	return new RHPigTransporter(chip, dest, msg);
    }
    
    
    protected Vector getSearchCenter(ChipState chip) {
    	Vector pos;
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	
    	if(chip.getMode() == 'p' || chip.getMode() == 'P') {
    		pos = Util.getWallSignBack(world, chip.getPosition(), -2);
    		
    		double newY = pos.getY() + 2;
    		
    		if(newY > CraftBook.MAP_BLOCK_HEIGHT)
    			newY = CraftBook.MAP_BLOCK_HEIGHT;
    		
    		pos.setY(newY);
    	} else {
    		pos = chip.getBlockPosition();
    	}
		double safeY = Util.getSafeYAbove(chip.getCBWorld(), pos);
        return new Vector(pos.getX() + 0.5, safeY, pos.getZ() + 0.5);
    }
    
    
    public static class RHPigTransporter extends RHSetOutIfFound {
    	WorldLocation tpDestination;
    	String messages[] = null;
    		
		public RHPigTransporter(ChipState chip, WorldLocation tpDestination, String[] messages) {
			super(chip);
			this.tpDestination = tpDestination;
			this.messages = messages;
		}
		
		@Override
		public void handleResult(final Set<BaseEntity> foundEntities) {
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					try {
						if (! Util.isBlockLoaded(new WorldBlockVector(chip.getCBWorld(), chip.getPosition()))) return;
				    	World world = CraftBook.getWorld(chip.getCBWorld());
						if (tpDestination == null
								|| ! world.isChunkLoaded(
										tpDestination.getBlockX(), 
										tpDestination.getBlockX(),
										tpDestination.getBlockX())) {
							setOutput(false);
							return;
						}
						boolean found = false;


						for (BaseEntity bEntity : foundEntities) {
							if (bEntity == null
									|| ! bEntity.getWorld().isChunkLoaded((int)bEntity.getX(), (int)bEntity.getY(), (int)bEntity.getZ())
									|| bEntity.isDead()) {
								 continue;
							}
				        	if (!(bEntity.getEntity() instanceof OEntityPig)) {
				        		continue;
				        	}
				    		OEntityPig oEntityPig = (OEntityPig) (bEntity.getEntity());
							// Check for saddle and rider
				    		// Notchian: EntityPig.getSaddled() Searge: func_70901_n
							if (oEntityPig.m() == false || bEntity.getRiddenByEntity() == null) {
								continue;
							}
								BaseEntity riderEntity = bEntity.getRiddenByEntity();
		    					if(messages != null && riderEntity.isPlayer())
		    					{
		    						Player player = new Player((OEntityPlayerMP)riderEntity.getEntity());
		    						for(String message : messages){
		    	                		if(message == null)
		    	                			break;
		    	                		player.sendMessage(Colors.Gold+message);
		    	                	}
		    					}
				        		tpDestination = tpDestination.setY(Util.getSafeYAbove(CraftBook.getWorld(tpDestination.getCBWorld()), tpDestination.getCoordinate()) + 1.0D);
				        		CraftBook.teleportEntity(bEntity, tpDestination);
				        		found = true;
				        		// Reset the pig to Wander so it loses any previous target
				        		OEntityAIWander mobAI = new OEntityAIWander(oEntityPig, 1);
				        		//Notchian: EntityAIBase.startExecuting(), Searge: func_75249_e
				        		mobAI.c();
				        	// Optional mode to reset pressure plates
				    		if(chip.getMode() == 'P') {
				    			//force plate off
					        	Location mLoc = bEntity.getLocation();
					        	Vector mVec = new Vector(mLoc.x, mLoc.y, mLoc.z);
				        		int bdata = CraftBook.getBlockID(world, mVec);
				        		if(bdata == BlockType.STONE_PRESSURE_PLATE || bdata == BlockType.WOODEN_PRESSURE_PLATE){
				        			OWorld oworld = world.getWorld();
				        			
				        			int bx = mVec.getBlockX();
				        			int by = mVec.getBlockY();
				        			int bz = mVec.getBlockZ();
				        			
				        			oworld.c(bx, by, bz, 0);
				        			oworld.h(bx, by, bz, bdata);
				        			oworld.h(bx, by - 1, bz, bdata);
				        			oworld.e(bx, by, bz, bx, by, bz);
				        		}
				    		}
						}
						setOutput(found);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			});
		}
    	
    }
    
    
    @Deprecated
    protected boolean transport(ChipState chip, WorldLocation dest, boolean useSafeY, String[] messages)
    {
    	if(dest == null)
    		return false;
    	
    	Vector pos;
    	World world = CraftBook.getWorld(chip.getCBWorld());
    	
    	if(chip.getMode() == 'p' || chip.getMode() == 'P')
    	{
    		pos = Util.getWallSignBack(world, chip.getPosition(), -2);
    		
    		double newY = pos.getY() + 2;
    		
    		if(newY > CraftBook.MAP_BLOCK_HEIGHT)
    			newY = CraftBook.MAP_BLOCK_HEIGHT;
    		
    		pos.setY(newY);
    	}
    	else
    		pos = chip.getBlockPosition();
    	
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
        
        int y = getSafeY(world, pos);

        for(Mob mob: world.getAnimalList()) {
        	if (!(mob.getEntity() instanceof OEntityPig)) {
        		continue;
        	}
    		OEntityPig op = (OEntityPig) (mob.getEntity());
			// Check for saddle and rider
			if (op.m() == false || mob.getRiddenByEntity() == null) {
				continue;
			}
        	
        	Location mLoc = mob.getLocation();
        	Vector mVec = new Vector(mLoc.x, mLoc.y, mLoc.z);
        	
        	if ((mVec.getBlockX() == x || mVec.getBlockX() == x + 1 || mVec.getBlockX() == x - 1)
        	   &&  mVec.getBlockY() == y
        	   && (mVec.getBlockZ() == z || mVec.getBlockZ() == z + 1 || mVec.getBlockZ() == z - 1)) {

        		if(useSafeY) {
        			dest = dest.setY(getSafeY(CraftBook.getWorld(dest.getCBWorld()), dest.getCoordinate()) + 1.0D);
        		}
        		
        		CraftBook.teleportEntity(mob, dest);
        		
        		// Reset the pig to Wander so it loses any previous target
        		OEntity oent = mob.getEntity();
        		OEntityAIWander mobAI = new OEntityAIWander((OEntityCreature)oent, 1);
        		//Notchian: EntityAIBase.startExecuting(), Searge: func_75249_e
        		mobAI.c();
        	} else {
        		continue;
        	}
        	
    		if(chip.getMode() == 'P')
    		{
    			//force plate off
        		int bdata = CraftBook.getBlockID(world, mVec);
        		if(bdata == BlockType.STONE_PRESSURE_PLATE || bdata == BlockType.WOODEN_PRESSURE_PLATE)
        		{
        			OWorld oworld = world.getWorld();
        			
        			int bx = mVec.getBlockX();
        			int by = mVec.getBlockY();
        			int bz = mVec.getBlockZ();
        			
        			oworld.c(bx, by, bz, 0);
        			oworld.h(bx, by, bz, bdata);
        			oworld.h(bx, by - 1, bz, bdata);
        			oworld.e(bx, by, bz, bx, by, bz);
        		}
    		}

    		return true;
        }
        
        return false;
    }
    
    @Deprecated
    protected static int getSafeY(World world, Vector pos)
    {
    	int maxY = Math.min(CraftBook.MAP_BLOCK_HEIGHT, pos.getBlockY() + 10);
        int x = pos.getBlockX();
        int z = pos.getBlockZ();
    	
    	for (int y = pos.getBlockY() + 1; y <= maxY; y++)
		{
            if (BlockType.canPassThrough(CraftBook.getBlockID(world, x, y, z)) &&
            	y < CraftBook.MAP_BLOCK_HEIGHT && BlockType.canPassThrough(CraftBook.getBlockID(world, x, y+1, z))	
            	)
            {
            	return y;
            }
		}
    	
    	return maxY;
    }
}