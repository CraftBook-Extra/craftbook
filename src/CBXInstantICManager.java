import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.WorldBlockVector;


public final class CBXInstantICManager {
	private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
	
    // thread-safe set containing positions of self-updating ICs
	private static final Set<WorldBlockVector> instantICs = Collections
			.newSetFromMap(new ConcurrentHashMap<WorldBlockVector, Boolean>(32, 0.9f, 1));
	

	private final RedstoneListener rsListener;

	
	
	public CBXInstantICManager(RedstoneListener rsListener) {
		this.rsListener = rsListener;
	}
	
	public void add(WorldBlockVector wbv) {
		instantICs.add(wbv);
	}
	
	
	
	public void onChunkLoaded(final Chunk chunk) {
		//System.out.print(".");
		if (CraftBook.cbxScheduler.isShutdown()) {
			return;
		}
		try {
			CraftBook.cbxScheduler.schedule(new InstantICFinder(chunk), 100, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			logger.log(Level.WARNING, 
					"CraftBook could not run the InstantICFinder for chunk ("
					+ chunk.getX()
					+ ", "
					+ chunk.getZ()
					+ "): RejectedExecutionException from cbxScheduler.");
		}
	}


	public void onChunkUnload(final Chunk chunk) {
//		System.out.print(",");
	}
	
    /**
     * Make all ICs in Set instantICs think(); remove them from the set if they have become invalid
     */
    public void runInstantICs(){
    	//int count=0;
    	final List<WorldBlockVector> toRemove = new LinkedList<WorldBlockVector>();
    	try {
    	// only process loaded blocks, remove unloaded ones
    	for (WorldBlockVector wbv:instantICs) {
    		World world = CraftBook.getWorld(wbv.getCBWorld());
    		// only run every second tick
	        if((world.getTime() % 2) != 0) continue;
	        
	        // don't run ICs in unloaded chunks
    		if (! Util.isBlockLoaded(wbv)) {
    			if (rsListener.selfTriggeredICsUpdating()) {
    				toRemove.add(wbv);
    			}
    			continue;
    		}
	        //get sign
	        if (world.getBlockIdAt(wbv.getBlockX(), wbv.getBlockY(), wbv.getBlockZ()) != BlockType.WALL_SIGN) {
	        	if (rsListener.selfTriggeredICsUpdating()) {
	        		toRemove.add(wbv);
	        	}
        		continue;
	        }
            Sign sign = (Sign)world.getComplexBlock(wbv.getBlockX(), wbv.getBlockY(), wbv.getBlockZ());
            if(sign == null) {
            	if (rsListener.selfTriggeredICsUpdating()) {
            		// should never happen, we've just checked the block ID
            		toRemove.add(wbv);
            	}
        		continue;
            }
            // check if it's an IC
            String line2 = sign.getText(1);
            String id = line2.substring(1, 7).toUpperCase();
            RedstoneListener.RegisteredIC ic = rsListener.getIC(id);
			if (ic == null) {
				// mark second line red if it looks like an IC, but isn't recognized
				if (line2.startsWith("[MC")) {
					sign.setText(1, Colors.Red + line2);
					sign.update();
				}
				if (rsListener.selfTriggeredICsUpdating()) {
					toRemove.add(wbv);
				}
				continue;
				
			}
            
			// make the IC do its thing
            if(ic.type.updateOnce) {
            	if(sign.getText(0).charAt(0) != '%') {
            		if (rsListener.selfTriggeredICsUpdating()) {
	            		toRemove.add(wbv);
            		}
            		if(sign.getText(0).charAt(0) == '^') {
                		continue;
            		}
            	}
            } else if(!ic.type.isSelfTriggered) {
            	if (rsListener.selfTriggeredICsUpdating()) {
            		toRemove.add(wbv);
            	}
                continue;
            }

            SignText signText = new SignText(sign.getText(0),
                    sign.getText(1), sign.getText(2), sign.getText(3));
            try {
            	//DEBUG
            	//count++;
            	ic.think(wbv.getCBWorld(), wbv, signText, sign, null);
            } catch(Throwable t) {
            	logger.log(Level.SEVERE, "ic.think failed with an exception:"
            			+ " World: " + CraftBook.getWorld(wbv.getCBWorld()).getName()
            			+ " Position: " + wbv +" " + sign);
            	t.printStackTrace();
            }
            
            if (signText.isChanged()) {
                sign.setText(0, signText.getLine1());
                sign.setText(1, signText.getLine2());
                sign.setText(2, signText.getLine3());
                sign.setText(3, signText.getLine4());
                
                if (signText.shouldUpdate()) {
                    sign.update();
                }
            }
    	}
    	}catch (Throwable t) {
    		// I've had something happen in here, once, on player logout.
    		// Can't pinpoint where exactly it went wrong:
    		// Vec3 Pool Size: ~~ERROR~~ NoSuchFieldError: c
    		t.printStackTrace();
    	}
    	//DEBUG
    	//if (etc.getServer().getDefaultWorld().getTime() % 100 == 0) System.out.println("runInstantICs executed " + count + " ICs");
		instantICs.removeAll(toRemove);
		//if (toRemove.size() > 0) System.out.println("runInstantICs removed " + toRemove.size() + " ICs");
    }

    /**
     * Searches for self-updating ICs in all currently loaded chunks and adds them to the instantICs set
     */
    public void findInstantICs() {
        if (rsListener.selfTriggeredICsEnabled()) {
	        for (String worldName : etc.getServer().getLoadedWorldNames()) {
	        	World[] worldDimensions = etc.getServer().getWorld(worldName);
	        	for (World world : worldDimensions) {
	        		for (Chunk chunk:world.getLoadedChunks()){
	        			try {
	        				CraftBook.cbxScheduler.execute(new InstantICFinder(chunk));        				
	        			} catch (RejectedExecutionException e){
	        				logger.log(Level.SEVERE, "Craftbook could not run the InstantICFinder for chunk ("
	        						+ chunk.getX()
	        						+ ", "
	        						+ chunk.getZ()
	        						+ "): RejectedExecutionException from cbxScheduler.");
	        			}
	        		}
	        	}
	        }
        }
    }
    
    
	/**
	 * Registers all instant ICs in a chunk; to be run in the CBXScheduler.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 * 
	 */
	private class InstantICFinder implements Runnable {
		private final Chunk chunk;

		public InstantICFinder(Chunk chunk) {
			this.chunk = chunk;
		}
		
		private Object[] getTileEntities() throws InterruptedException {
			final int maxtries = 10;
			int tries = 0;
			Object[] loadedTileEntities = null;
			while (tries < maxtries) {
				tries++;
				try {
					// Util.getLoadedTileEntityList() returns a copy, which might fail in rare cases
					// when the server is modifying the TileEntityList. In that case, we just retry.
					// Worst thing that can happen on failure is that self-updating ICs don't work until
					// the chunk is reloaded.
					loadedTileEntities = Util
							.getLoadedTileEntityList(chunk);
				} catch (ConcurrentModificationException e) {
					logger.log(
							Level.INFO,
							"CraftBook: InstantICFinder caught a ConcurrentModificationException. This expected to happen occasionally.");
					continue;
				} catch (NullPointerException e) {
					logger.log(
							Level.INFO,
							"CraftBook: InstantICFinder caught a NullPointerException. This expected to happen occasionally.");
					break;
				}
				break;
			}
			return loadedTileEntities;
		}
		
		private List <OTileEntitySign> findSigns(final Object[] loadedTileEntities){
			final List<OTileEntitySign> foundSigns = new ArrayList<OTileEntitySign>();
			for (Object oTE : loadedTileEntities) {
				if (oTE instanceof OTileEntitySign) {
						foundSigns.add((OTileEntitySign) oTE);
				}
			}
			return foundSigns;
		}


		private void addInstantICSigns(final List<OTileEntitySign> foundSigns){
			CraftBook.cbxScheduler.executeInServer(new Runnable() {
				public void run() {
					// chunk might have been unloaded again before this is processed
					if (chunk.isLoaded()) {
						//int added = 0;
						for (OTileEntitySign oTES : foundSigns) {
							rsListener.onSignAdded(chunk.getWorld(), oTES);
							///added++;
						}
						//DEBUG
						//if (added > 0) System.out.println("InstantICFinder added " + added + " Signs.");
					}
				}
			});
		}

		public void run() {
			try{
				Object[] loadedTileEntities = getTileEntities();

				if (loadedTileEntities != null) {
					if (loadedTileEntities.length == 0) return;
					List<OTileEntitySign> foundSigns = findSigns(loadedTileEntities);
					if (foundSigns.isEmpty()) return;
					addInstantICSigns(foundSigns);
				} else {
					logger.log(
							Level.WARNING,
							"CraftBook: RedstoneListener could not get LoadedTileEntityList for chunk "
									+ chunk.getX()
									+ ", "
									+ chunk.getZ()
									+ ". Self-updating ICs in this chunk will not work until it is reloaded.");
				}
			// no matter what happens, do not crash the server
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

}
