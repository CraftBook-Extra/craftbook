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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sk89q.craftbook.*;

/**
 * Listener for mechanisms.
 * 
 * @author sk89q
 */
public class MechanismListener extends CraftBookDelegateListener {

    protected static Map<WorldBlockVector, CBBookInventory> bookshelves = new HashMap<WorldBlockVector, CBBookInventory>();
    // properties
    private boolean checkPermissions;
    private boolean checkCreatePermissions;
    private boolean redstoneToggleAreas = true;
    private boolean useBookshelves = true;
    private String bookReadLine;
    private Cauldron cauldronModule;
    private boolean useElevators = true;
    private boolean useGates = true;
    private boolean redstoneGates = true;
    private boolean useLightSwitches = true;
    private boolean useBridges = true;
    private boolean redstoneBridges = true;
    private boolean useDoors = true;
    private boolean useConvertExp = false;
    private boolean redstoneDoors = true;
    private boolean useHiddenSwitches = true;
    private boolean useToggleAreas;
    private boolean dropBookshelves = true;
    private double dropAppleChance = 0;
    private boolean enableAmmeter = true;
    private boolean usePageReader = true;
    private boolean useBookShelf = false;
    private boolean bookshelfNeedsPower = false;
    private boolean usePageSwitches = true;
    private boolean useMapChanger = true;
    protected static int maxEnchantAmount = 3;

    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public MechanismListener(CraftBook craftBook, CraftBookListener listener) {
        super(craftBook, listener);
    }

    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
    	setPropertyFields();
        loadCauldron();
        loadMinecartCraftRecipes();
        EnchantCraft.load();
    }
    
    /**
     * Called before the console command is parsed. Return true if you don't
     * want the server command to be parsed by the server.
     * 
     * @param split
     * @return false if you want the command to be parsed.
     */
    public boolean onConsoleCommand(String[] split) {
        if (split[0].equalsIgnoreCase("reload-cauldron")) {
            loadCauldron();
            return true;
        }
        return false;
    }
    
    /**
     * Handles the wire input at a block in the case when the wire is
     * directly connected to the block in question only.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onDirectWireInput(final World world, final Vector pt,
            final boolean isOn, final Vector changed) {
    	// get the sign
        int type = CraftBook.getBlockID(world, pt);
        final CraftBookWorld cbworld = CraftBook.getCBWorld(world);
        if (type != BlockType.WALL_SIGN && type != BlockType.SIGN_POST) {
        	return;
        }
        ComplexBlock cblock = world.getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (!(cblock instanceof Sign)) {
        	return;
        }
        final Sign sign = (Sign)cblock;
        final String line2 = sign.getText(1);

        // Gates
        if (isGateString(line2)) {
        	onGateInput(line2, cbworld, pt, isOn);
        	return;
        }
        //Bridges
        if (isBridgeSign(line2, type)) {
        	onBridgeInput(line2, world, cbworld, pt, isOn);
        	return;
        }
       	// Doors
        if (isDoorSign(line2, type)) {
        	onDoorInput(line2, world, cbworld, pt, isOn);
        	return;
        }
       	// Toggle areas
        if (isToggleAreaSign(line2)) {
        	onToggleAreaInput(line2, world, cbworld, pt, isOn);
        	return;
        }
        // PageReaderSwitch
        if (isPageReaderSwitch(line2)){
        	onPageReaderSwitchInput(line2, world, cbworld, pt, isOn, changed);
        	return;
        }
    }

    private boolean isGateString(String line2) {
		return line2.equalsIgnoreCase("[Gate]")
			|| line2.equalsIgnoreCase("[DGate]")
			|| line2.equalsIgnoreCase("[GlassGate]")
			|| line2.equalsIgnoreCase("[GlassDGate]")
			|| line2.equalsIgnoreCase("[IronGate]")
			|| line2.equalsIgnoreCase("[IronDGate]")
			|| line2.equalsIgnoreCase("[NetherGate]")
			|| line2.equalsIgnoreCase("[NetherDGate]");
	}

	private void onGateInput(String line2, final CraftBookWorld cbworld, final Vector pt, boolean isOn) {
		if(useGates && redstoneGates) {
			// determine gate material
			int blockType;
			if(line2.equalsIgnoreCase("[GlassGate]") || line2.equalsIgnoreCase("[GlassDGate]")) {
				blockType = BlockType.GLASS_PANE;
			} else if(line2.equalsIgnoreCase("[IronGate]") || line2.equalsIgnoreCase("[IronDGate]")) {
				blockType = BlockType.IRON_BARS;
			} else if(line2.equalsIgnoreCase("[NetherGate]") || line2.equalsIgnoreCase("[NetherDGate]")) {
				blockType = BlockType.NETHER_BRICK_FENCE;
			} else {
				blockType = BlockType.FENCE;
			}
			// diagonal?
			boolean dgate = line2.equalsIgnoreCase("[GlassDGate]")
					|| line2.equalsIgnoreCase("[IronDGate]")
					|| line2.equalsIgnoreCase("[DGate]");
			// set the state of the gate
			BlockBag bag = getBlockBag(cbworld, pt);
			bag.addSourcePosition(cbworld, pt);
			try {            		
				GateSwitch.setGateState(blockType, cbworld, pt, bag, isOn, dgate);
			} catch(BlockSourceException e){}
		}
	}

	private boolean isBridgeSign(String line2, int type) {
		return type == BlockType.SIGN_POST && line2.equalsIgnoreCase("[Bridge]");
	}

	private void onBridgeInput(String line2, final World world, final CraftBookWorld cbworld, final Vector pt, final boolean isOn) {
	    if (useBridges != false && redstoneBridges) {
	    	craftBook.getDelay(cbworld).delayAction(
	    			new TickDelayer.Action(world, pt.toBlockVector(), 2) {
	    				@Override
	    				public void run() {
	    					BlockBag bag = listener.getBlockBag(cbworld, pt);
	    					bag.addSourcePosition(cbworld, pt);
	
	    					Bridge bridge = new Bridge(cbworld, pt);
	    					if (isOn) {
	    						bridge.setActive(bag);
	    					} else {
	    						bridge.setInactive(bag);
	    					}
	    				}
	    			});
	    }
	}

	private boolean isDoorSign(String line2, int type) {
		return type == BlockType.SIGN_POST
			&& ( line2.equalsIgnoreCase("[Door Up]") || line2.equalsIgnoreCase("[Door Down]"));
	}

	private void onDoorInput(String line2, final World world, final CraftBookWorld cbworld, final Vector pt, final boolean isOn) {
	    if (useDoors != false && redstoneDoors) {
	    	craftBook.getDelay(cbworld).delayAction(
	    			new TickDelayer.Action(world, pt.toBlockVector(), 2) {
	    				@Override
	    				public void run() {
	    					BlockBag bag = getBlockBag(cbworld, pt);
	    					bag.addSourcePosition(cbworld, pt);
	    					Door door = new Door(cbworld, pt);
	    					if (isOn) {
	    						door.setActive(bag);
	    					} else {
	    						door.setInactive(bag);
	    					}
	    				}
	    			});
	    }
	
	}

	private boolean isToggleAreaSign(String line2){
		return line2.equalsIgnoreCase("[Toggle]")
			|| line2.equalsIgnoreCase("[Area]");
	}

	private void onToggleAreaInput(String line2, final World world, final CraftBookWorld cbworld, final Vector pt, final boolean isOn) {
		if (useToggleAreas && redstoneToggleAreas) {                
	    	craftBook.getDelay(cbworld).delayAction(
	    			new TickDelayer.Action(world, pt.toBlockVector(), 2) {
	    				@Override
	    				public void run() {
	    					BlockBag bag = listener.getBlockBag(cbworld, pt);
	    					bag.addSourcePosition(cbworld, pt);
	
	    					ToggleArea area = new ToggleArea(cbworld, pt, listener.getCopyManager());
	
	    					if (isOn) { 
	    						area.setActive(bag);
	    					} else {
	    						area.setInactive(bag);
	    					}
	    				}
	    			});
	    } 
	}

	private boolean isPageReaderSwitch(String line2) {
		return line2.equalsIgnoreCase("[Book][X]");
	}

	private void onPageReaderSwitchInput(String line2, final World world, final CraftBookWorld cbworld, final Vector pt, final boolean isOn, Vector changed) {
		if (usePageReader && usePageSwitches) {
	    	Vector redstonept = Util.getWallSignBack(cbworld, pt, -1);
	    	if (redstonept == null) return;
	    	if(changed.equals(redstonept)
	    			&& CraftBook.getBlockID(world, redstonept) == BlockType.REDSTONE_WIRE)
	    	{
	    		craftBook.getDelay(cbworld).delayAction(
	    				new TickDelayer.Action(world, pt.toBlockVector(), 2) {
	    					@Override
	    					public void run() {
	    						Vector blockpt = Util.getWallSignBack(cbworld, pt, 1);
	    						if (blockpt == null) return;
	    						int x = blockpt.getBlockX();
	    						int y = blockpt.getBlockY();
	    						int z = blockpt.getBlockZ();
	    						String hiddenType = "[Book][X]";
	
	    						setHiddenSwitch(hiddenType, isOn, world, x, y - 1, z);
	    						setHiddenSwitch(hiddenType, isOn, world, x, y + 1, z);
	    						setHiddenSwitch(hiddenType, isOn, world, x - 1, y, z);
	    						setHiddenSwitch(hiddenType, isOn, world, x + 1, y, z);
	    						setHiddenSwitch(hiddenType, isOn, world, x, y, z - 1);
	    						setHiddenSwitch(hiddenType, isOn, world, x, y, z + 1);
	
	    						world.updateBlockPhysics(x, y+1, z, CraftBook.getBlockData(world, x, y+1, z));
	    						world.updateBlockPhysics(x, y-1, z, CraftBook.getBlockData(world, x, y-1, z));
	    					}
	    				});
	    	}
	    }
	
	}

	/**
	 * Called when a block is being attempted to be placed.
	 * 
	 * @param player
	 * @param blockClicked
	 * @param itemInHand
	 * @return
	 */
	@Override
	public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
		
		if(Sitting.enabled
	    	&& (item == null || item.getItemId() == 0)
			&& (!Sitting.requireRightClickPermission || player.canUseCommand("/canrightclicksit"))
			&& EntitySitting.isChairBlock(blockClicked.getType()) )
		{
			Sign[] signs = Sitting.isChair(blockClicked);
			if(signs == null)
			{
				if(Sitting.requiresChairFormats)
					return;
				signs = new Sign[0];
			}
			
			World world = player.getWorld();
			int data = world.getBlockData(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
			if(player.getRidingEntity() != null)
			{
				switch(data)
	    		{
		    		case 0x0: //south
		    			Sitting.stand(player, -0.8D, 0, 0);
		    			break;
		    		case 0x1: //north
		    			Sitting.stand(player, 0.8D, 0, 0);
		    			break;
		    		case 0x2: //west
		    			Sitting.stand(player, 0, 0, -0.8D);
		    			break;
		    		case 0x3: //east
		    			Sitting.stand(player, 0, 0, 0.8D);
		    			break;
		    		default:
		    			Sitting.stand(player, 0, 0, 0);
	    		}
			}
			else
			{
				float rotation;
	    		double x = blockClicked.getX() + 0.5D;
	    		double y = blockClicked.getY();
	    		double z = blockClicked.getZ() + 0.5D;
	    		
	    		switch(data)
	    		{
		    		case 0x0: //south
		    			rotation = 90F;
		    			x -= 0.2D;
		    			break;
		    		case 0x1: //north
		    			rotation = 270F;
		    			x += 0.2D;
		    			break;
		    		case 0x2: //west
		    			rotation = 180F;
		    			z -= 0.2D;
		    			break;
		    		case 0x3: //east
		    			rotation = 0F;
		    			z += 0.2D;
		    			break;
		    		default:
		    			rotation = 0F;
	    		}
	    		
	    		SitType[] types = new SitType[signs.length + 1];
	    		
	    		boolean hasHealing = false;
	    		for(int i = 0; i < signs.length; i++)
	    		{
	    			SittingType sittype = Sitting.getSittingTypeFromSign(signs[i]);
	    			if(sittype == null)
	    				continue;
	    			
	    			types[i] = sittype.getType(signs[i]);
	    			if(sittype == SittingType.SIT_HEAL)
	    				hasHealing = true;
	    		}
	    		
	    		if(!hasHealing)
	    		{
					switch(Sitting.healWhileSitting)
					{
						case ALL:
						case CHAIRONLY:
							types[types.length-1] = SittingType.SIT_HEAL.getType();
							break;
						default:
							types[types.length-1] = null;
					}
	    		}
	    		Sitting.sit(player, types, player.getWorld(), x, y, z, rotation, 0.2D, 0.0D);
			}
		}
		
	    try {
	        // Discriminate against attempts that would actually place blocks
	        boolean isPlacingBlock = item.getItemId() >= 1
	                && item.getItemId() <= 256;
	        // 1 to work around empty hands bug in hMod
	        
	        if (!isPlacingBlock) {
	            handleBlockUse(player, blockClicked, item.getItemId());
	        }
	    } catch (OutOfBlocksException e) {
	        player.sendMessage(Colors.Rose + "Uh oh! Ran out of: " + Util.toBlockName(e.getID()));
	        player.sendMessage(Colors.Rose + "Make sure nearby block sources have the necessary");
	        player.sendMessage(Colors.Rose + "materials.");
	    } catch (OutOfSpaceException e) {
	        player.sendMessage(Colors.Rose + "No room left to put: " + Util.toBlockName(e.getID()));
	        player.sendMessage(Colors.Rose + "Make sure nearby block sources have free slots.");
	    } catch (BlockSourceException e) {
	        player.sendMessage(Colors.Rose + "Error: " + e.getMessage());
	    }
	}

	/**
	 * Called when a block is being attempted to be placed.
	 * 
	 * @param player
	 * @param blockClicked
	 * @param itemInHand
	 * @return
	 */
	
	private boolean handleBlockUse(Player player, Block blockClicked,
	        int itemInHand)
	        throws BlockSourceException {
	
		World world = player.getWorld();
		CraftBookWorld cbworld = CraftBook.getCBWorld(world);
		
		int blockType = blockClicked.getType();
		
	    int current = -1;
	
	    // Ammeter
	    if (enableAmmeter && itemInHand == 263) { // Coal
	        int type = blockType;
	        int data = CraftBook.getBlockData(world, blockClicked.getX(),
	                blockClicked.getY(), blockClicked.getZ());
	        
	        if (type == BlockType.LEVER) {
	            if ((data & 0x8) == 0x8) {
	                current = 15;
	            }
	            current = 0;
	        } else if (type == BlockType.STONE_PRESSURE_PLATE) {
	            if ((data & 0x1) == 0x1) {
	                current = 15;
	            }
	            current = 0;
	        } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
	            if ((data & 0x1) == 0x1) {
	                current = 15;
	            }
	            current = 0;
	        } else if (type == BlockType.REDSTONE_TORCH_ON) {
	            current = 15;
	        } else if (type == BlockType.REDSTONE_TORCH_OFF) {
	            current = 0;
	        } else if (type == BlockType.STONE_BUTTON) {
	            if ((data & 0x8) == 0x8) {
	                current = 15;
	            }
	            current = 0;
	        } else if (type == BlockType.WOODEN_BUTTON) {
	            if ((data & 0x8) == 0x8) {
	                current = 15;
	            }
	            current = 0;
	        } else if (type == BlockType.REDSTONE_WIRE) {
	            current = data;
	        }
	
	        if (current > -1) {
	            player.sendMessage(Colors.Yellow + "Ammeter: "
	                    + Colors.Yellow + "["
	                    + Colors.Yellow + Util.repeatString("|", current)
	                    + Colors.Black + Util.repeatString("|", 15 - current)
	                    + Colors.Yellow + "] "
	                    + Colors.White
	                    + current + " A");
	        }
	
	        return false;
	    }
	
	    int plyX = (int)Math.floor(player.getLocation().x);
	    int plyY = (int)Math.floor(player.getLocation().y);
	    int plyZ = (int)Math.floor(player.getLocation().z);
	    
	    // Map Changing
	    if(useMapChanger
	    	&& itemInHand == 358 //map
	    	&& blockType == BlockType.BOOKCASE)
	    {
	    	Sign sign = Util.getWallSignNextTo(world, blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
	    	
	    	if(sign != null && sign.getText(1).equalsIgnoreCase("[Map]"))
	    	{
	    		Item handItem = player.getItemStackInHand();
	    		int index;
	    		try
	    		{
	    			index = Integer.parseInt(sign.getText(0));
	    		}
	    		catch(NumberFormatException e)
	    		{
	    			return false;
	    		}
	    		
	    		OMapData mapData = (OMapData)world.getWorld().a(OMapData.class, "map_" + index);
	    		if(mapData == null)
	    		{
	    			player.sendMessage(Colors.Rose+"map_"+index+" does not exist");
	    			return false;
	    		}
	    		
	    		handItem.setDamage(index);
	    		player.getInventory().update();
	    		
	    		String msg = sign.getText(2) + sign.getText(3);
	    		if(msg.isEmpty())
	    			msg = "Your map has changed to "+mapData.a;
	    		player.sendMessage(Colors.Gold+msg);
	    		
	    		return false;
	    	}
	    }
	    
	    // Page reading
	    if(usePageReader
	    		&& blockType == BlockType.BOOKCASE
	    		&& checkPermission(player, "/readpages")
	    		)
	    {
	    	Sign sign = Util.getWallSignNextTo(world, blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
	    	
	    	if(sign != null && (sign.getText(1).equalsIgnoreCase("[Book]") || sign.getText(1).equalsIgnoreCase("[Book][X]")) )
	    	{
	        	PageWriter.readPage(player, sign);
	        	
	        	if(usePageSwitches && sign.getText(1).equalsIgnoreCase("[Book][X]"))
	        	{
	        		int x = blockClicked.getX();
	                int y = blockClicked.getY();
	                int z = blockClicked.getZ();
	                String type = "[Book][X]";
	                
	                toggleHiddenSwitch(type, world, x, y - 1, z);
	                toggleHiddenSwitch(type, world, x, y + 1, z);
	                toggleHiddenSwitch(type, world, x - 1, y, z);
	                toggleHiddenSwitch(type, world, x + 1, y, z);
	                toggleHiddenSwitch(type, world, x, y, z - 1);
	                toggleHiddenSwitch(type, world, x, y, z + 1);
	                
	                world.updateBlockPhysics(x, y+1, z, CraftBook.getBlockData(world, x, y+1, z));
	                world.updateBlockPhysics(x, y-1, z, CraftBook.getBlockData(world, x, y-1, z));
	        	}
	        	
	        	return true;
	    	}
	    }
	    
	    // Book storage
	    if(useBookShelf
	    		&& blockType == BlockType.BOOKCASE
	    		&& (itemInHand == 0 || itemInHand == 340 || itemInHand == 386 || itemInHand == 387)
	    		&& (!bookshelfNeedsPower || (blockClicked.isPowered() || blockClicked.isIndirectlyPowered()) )
	    		&& (player.canUseCommand("/createcbbookshelf") || player.canUseCommand("/accesscbbookshelf") )
	    		)
	    {
	    	int x = blockClicked.getX();
	        int y = blockClicked.getY();
	        int z = blockClicked.getZ();
	        
			WorldBlockVector wloc = new WorldBlockVector(cbworld, x, y, z);
			CBBookInventory cbbookinv = bookshelves.get(wloc);
			if(cbbookinv == null && player.canUseCommand("/createcbbookshelf") )
			{
				cbbookinv = new CBBookInventory();
				cbbookinv.cbworld = cbworld;
				cbbookinv.x = x;
				cbbookinv.y = y;
				cbbookinv.z = z;
				bookshelves.put(wloc, cbbookinv);
				CraftBook.cbdata.markDirty();
			}
			
			if(cbbookinv != null)
			{
	    		OEntityPlayerMP oplayer = player.getEntity();
	    		PlayerSettings setting = CraftBookListener.getPlayerSettings(player);
	    		if(setting != null)
	    		{
	    			// field_71070_bA != field_71069_bz
	    			if (oplayer.bp != oplayer.bo)
		    		{
	    				//func_71053_j
		    			oplayer.h();
		    		}
	    			
	    			setting.getNextWindowId();
	    			// The constructor of OPacket100OpenWindow needs a boolean value now - no idea what its purpose is
	    			// Set to true for now and see if anything breaks
	    			boolean Packet100OpenWindow_field_94500_e = true;
	    			UtilEntity.sendPacket(oplayer, new OPacket100OpenWindow(setting.currentWindowId, 0, cbbookinv.b(), cbbookinv.j_(), Packet100OpenWindow_field_94500_e));
	    			//field_71070_bA ... field_71071_by
	    			oplayer.bp = new CBContainerShelf(oplayer.bn, cbbookinv);
	    			//field_71070_bA
	    			oplayer.bp.d = setting.currentWindowId;
	    			//field_71070_bA
	    			oplayer.bp.a((OICrafting)oplayer);
	    		}
	    		return true;
			}
	    }
	
	    // Book reading
	    if (useBookshelves
	            && blockType == BlockType.BOOKCASE
	            && checkPermission(player, "/readbooks")) {
	        BookReader.readBook(player, bookReadLine);
	        return true;
	
	    // Sign buttons
	    } else if (blockType == BlockType.WALL_SIGN ||
	    		blockType == BlockType.SIGN_POST ||
	            CraftBook.getBlockID(world, plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN ||
	            CraftBook.getBlockID(world, plyX, plyY, plyZ) == BlockType.WALL_SIGN) {
	        int x = blockClicked.getX();
	        int y = blockClicked.getY();
	        int z = blockClicked.getZ();
	
	        // Because sometimes the player is *inside* the block with a sign,
	        // it becomes impossible for the player to select the sign but
	        // may try anyway, so we're fudging detection for this case
	        Vector pt;
	        if (blockType == BlockType.WALL_SIGN
	                || blockType == BlockType.SIGN_POST) {
	            pt = new Vector(x, y, z);
	        } else if (CraftBook.getBlockID(world, plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN) {
	            pt = new Vector(plyX, plyY + 1, plyZ);
	            x = plyX;
	            y = plyY + 1;
	            z = plyZ;
	        } else {
	            pt = new Vector(plyX, plyY, plyZ);
	            x = plyX;
	            y = plyY;
	            z = plyZ;
	        }
	
	        ComplexBlock cBlock = world.getComplexBlock(x, y, z);
	
	        if (cBlock instanceof Sign) {
	            Sign sign = (Sign)cBlock;
	            String line2 = sign.getText(1);
	
	            // Gate
	            if (useGates
	                    && (   line2.equalsIgnoreCase("[Gate]")
	                        || line2.equalsIgnoreCase("[DGate]")
	                        || line2.equalsIgnoreCase("[GlassGate]")
	                        || line2.equalsIgnoreCase("[GlassDGate]")
	                        || line2.equalsIgnoreCase("[IronGate]")
	                        || line2.equalsIgnoreCase("[IronDGate]")
	                        || line2.equalsIgnoreCase("[NetherGate]")
	                        || line2.equalsIgnoreCase("[NetherDGate]")
	                        )
	                    && checkPermission(player, "/gate")) {
	            	
	            	
	            	if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.GATE, sign, player}))
	            		return true;
	            	
	                BlockBag bag = getBlockBag(cbworld, pt);
	                bag.addSourcePosition(cbworld, pt);
	
	                int gateType;
	        		if(line2.equalsIgnoreCase("[GlassGate]") || line2.equalsIgnoreCase("[GlassDGate]"))
	        			gateType = BlockType.GLASS_PANE;
	        		else if(line2.equalsIgnoreCase("[IronGate]") || line2.equalsIgnoreCase("[IronDGate]"))
	        			gateType = BlockType.IRON_BARS;
	        		else if(line2.equalsIgnoreCase("[NetherGate]") || line2.equalsIgnoreCase("[NetherDGate]"))
	        			gateType = BlockType.NETHER_BRICK_FENCE;
	        		else
	        			gateType = BlockType.FENCE;
	        		
	        		boolean dgate = line2.equalsIgnoreCase("[GlassDGate]")
	        						|| line2.equalsIgnoreCase("[IronDGate]")
	        						|| line2.equalsIgnoreCase("[NetherDGate]")
	        						|| line2.equalsIgnoreCase("[DGate]");
	                
	                // A gate may toggle or not
	                if (GateSwitch.toggleGates(gateType, cbworld, pt, bag, dgate)) {
	                    player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
	                } else {
	                    player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
	                }
	            
	            // Light switch
	            } else if (useLightSwitches &&
	                    (line2.equalsIgnoreCase("[|]") || line2.equalsIgnoreCase("[I]"))
	                    && checkPermission(player, "/lightswitch")) {
	                BlockBag bag = getBlockBag(cbworld, pt);
	                bag.addSourcePosition(cbworld, pt);
	                
	                return LightSwitch.toggleLights(cbworld, pt, bag);
	
	            // Elevator
	            } else if (blockType == BlockType.WALL_SIGN && useElevators
	                    && (line2.equalsIgnoreCase("[Lift Up]")
	                    || line2.equalsIgnoreCase("[Lift Down]"))
	                    && checkPermission(player, "/elevator")) {
	
	                // Go up or down?
	                boolean up = line2.equalsIgnoreCase("[Lift Up]");
	                if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.LIFT, sign, player}))
	            		return true;
	                Elevator.performLift(player, pt, up);
	                
	                return true;
	
	            // Toggle areas
	            } else if (useToggleAreas != false
	                    && (line2.equalsIgnoreCase("[Toggle]")
	                    || line2.equalsIgnoreCase("[Area]"))
	                    && checkPermission(player, "/togglearea")) {
	                
	            	if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.AREA, sign, player}))
	            		return true;
	            	
	                BlockBag bag = getBlockBag(cbworld, pt);
	                bag.addSourcePosition(cbworld, pt);
	                
	                ToggleArea area = new ToggleArea(cbworld, pt, listener.getCopyManager());
	                area.playerToggle(new CraftBookPlayerImpl(player), bag);
	
	                // Tell the player of missing blocks
	                Map<Integer,Integer> missing = bag.getMissing();
	                if (missing.size() > 0) {
	                    for (Map.Entry<Integer,Integer> entry : missing.entrySet()) {
	                        player.sendMessage(Colors.Rose + "Missing "
	                                + entry.getValue() + "x "
	                                + Util.toBlockName(entry.getKey()));
	                    }
	                }
	                
	                return true;
	
	            // Bridges
	            } else if (useBridges
	                    && blockType == BlockType.SIGN_POST
	                    && line2.equalsIgnoreCase("[Bridge]")
	                    && checkPermission(player, "/bridge")) {
	                
	            	if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.BRIDGE, sign, player}))
	            		return true;
	            	
	                BlockBag bag = getBlockBag(cbworld, pt);
	                bag.addSourcePosition(cbworld, pt);
	                
	                Bridge bridge = new Bridge(cbworld, pt);
	                bridge.playerToggleBridge(new CraftBookPlayerImpl(player), bag);
	                
	                return true;
	
	            // Doors
	            } else if (useDoors
	                    && blockType == BlockType.SIGN_POST
	                    && (line2.equalsIgnoreCase("[Door Up]")
	                            || line2.equalsIgnoreCase("[Door Down]"))
	                    && checkPermission(player, "/door")) {
	                
	            	if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.DOOR, sign, player}))
	            		return true;
	            	
	                BlockBag bag = getBlockBag(cbworld, pt);
	                bag.addSourcePosition(cbworld, pt);
	                
	                Door door = new Door(cbworld, pt);
	                door.playerToggleDoor(new CraftBookPlayerImpl(player), bag);
	                
	                return true;
	                
	            // Convert EXP
	            } else if (useConvertExp
	                    && (blockType == BlockType.SIGN_POST || blockType == BlockType.WALL_SIGN)
	                    && line2.equalsIgnoreCase("[Convert Exp]")
	                    && checkPermission(player, "/convertexpsign")) {
	                
	            	Pattern regex = Pattern.compile("^[0-9]+");
	            	Matcher match = regex.matcher(sign.getText(2));
	            	
	            	int levels = -1;
	            	if(match.find())
	            	{
	            		levels = Integer.parseInt(match.group());
	            	}
	            	else
	            	{
	            		player.sendMessage(Colors.Rose+"Failed to find EXP levels on sign.");
	            		return true;
	            	}
	            	
	            	if(levels < 1)
	            	{
	            		player.sendMessage(Colors.Rose+"Invalid EXP level # on sign.");
	            		return true;
	            	}
	            	
	            	if(player.getLevel() < levels)
	            	{
	        			player.sendMessage(Colors.Rose+"You do not have enough EXP levels");
	        			return true;
	            	}
	            	
	            	match = regex.matcher(sign.getText(3));
	            	
	            	int bottles = -1;
	            	if(match.find())
	            	{
	            		bottles = Integer.parseInt(match.group());
	            		
	            		if(bottles > 0)
	            		{
	                		int slots = (int)Math.ceil(bottles / 64.0D);
	                		int size = player.getInventory().getContentsSize();
	                		size -= 4; //armor slots
	                		int emptyslots = 0;
	                		for(int i = 0; i < size; i++)
	                		{
	                			if(player.getInventory().getItemFromSlot(i) == null)
	                			{
	                				emptyslots++;
	                				if(emptyslots >= slots)
	                					break;
	                			}
	                		}
	                		if(emptyslots < slots)
	                		{
	                			player.sendMessage(Colors.Rose+"You do not have enough room in your inventory!");
	                			player.sendMessage(Colors.Rose+"You need "+Colors.White+slots+Colors.Rose+" empty slots.");
	                			return true;
	                		}
	            		}
	            	}
	            	else
	            	{
	            		player.sendMessage(Colors.Rose+"Failed to find # of EXP bottles on sign.");
	            		return true;
	            	}
	            	
	            	if(bottles < 0)
	            	{
	            		player.sendMessage(Colors.Rose+"Invalid # of EXP bottles on sign.");
	            		return true;
	            	}
	            	
	            	if(!CBHooked.getBoolean(CBHook.SIGN_MECH, new Object[] {CBPluginInterface.CBSignMech.CONVERT_EXP, sign, player}))
	            		return true;
	            	
	            	player.getEntity().a(-levels); //remove levels
	            	
	            	while(bottles > 64)
	            	{
	            		bottles -= 64;
	            		Item item = new Item(384, 64);
	                	player.giveItem(item);
	            	}
	            	if(bottles > 0)
	            	{
	            		Item item = new Item(384, bottles);
	            		player.giveItem(item);
	            	}
	                return true;
	            }
	        }
	
	    // Cauldron
	    } else if (cauldronModule != null
	            && checkPermission(player, "/cauldron")) {
	        
	        int x = blockClicked.getX();
	        int y = blockClicked.getY();
	        int z = blockClicked.getZ();
	
	        cauldronModule.preCauldron(new Vector(x, y, z), player);
	
	    }
	
	    // Hidden switches
	    if (useHiddenSwitches
	            && itemInHand <= 0
	            && blockType != BlockType.SIGN_POST
	            && blockType != BlockType.WALL_SIGN
	            && !BlockType.isRedstoneBlock(blockType)) {
	        
	        int x = blockClicked.getX();
	        int y = blockClicked.getY();
	        int z = blockClicked.getZ();
	        String type = "[X]";
	
	        toggleHiddenSwitch(type, world, x, y - 1, z);
	        toggleHiddenSwitch(type, world, x, y + 1, z);
	        toggleHiddenSwitch(type, world, x - 1, y, z);
	        toggleHiddenSwitch(type, world, x + 1, y, z);
	        toggleHiddenSwitch(type, world, x, y, z - 1);
	        toggleHiddenSwitch(type, world, x, y, z + 1);
	        
	        return true;
	    }
	    
	    return false;
	}

	/**
	 * Toggle a hidden switch.
	 * 
	 * @param pt
	 */
	private void toggleHiddenSwitch(String type, World world, int x, int y, int z) {
		if(y < 0 || y > CraftBook.MAP_BLOCK_HEIGHT - 1)
			return;
		
	    ComplexBlock cblock = world.getComplexBlock(x, y, z);
	    
	    if (cblock instanceof Sign) {
	        Sign sign = (Sign)cblock;
	        
	        if (sign.getText(1).equalsIgnoreCase(type)) {
	        	CraftBookWorld cbworld = CraftBook.getCBWorld(world);
	            Redstone.toggleOutput(cbworld, new Vector(x, y - 1, z));
	            Redstone.toggleOutput(cbworld, new Vector(x, y + 1, z));
	            Redstone.toggleOutput(cbworld, new Vector(x - 1, y, z));
	            Redstone.toggleOutput(cbworld, new Vector(x + 1, y, z));
	            Redstone.toggleOutput(cbworld, new Vector(x, y, z - 1));
	            Redstone.toggleOutput(cbworld, new Vector(x, y, z + 1));
	        }
	    }
	}

	/**
	 * Called when a sign is updated.
	 * @param player
	 * @param cblock
	 * @return
	 */
	public boolean onSignChange(Player player, Sign sign) {
	    CraftBookPlayer ply = new CraftBookPlayerImpl(player);
	    SignTextImpl signText = new SignTextImpl(sign);
	    Vector pt = new Vector(sign.getX(), sign.getY(), sign.getZ());
	    World world = player.getWorld();
	    CraftBookWorld cbworld = CraftBook.getCBWorld(world);
	    
	    String line2 = sign.getText(1);
	    
	    // Gate
	    if (line2.equalsIgnoreCase("[Gate]")
	    	|| line2.equalsIgnoreCase("[DGate]")
	    	|| line2.equalsIgnoreCase("[GlassGate]")
	    	|| line2.equalsIgnoreCase("[GlassDGate]")
	    	|| line2.equalsIgnoreCase("[IronGate]")
	    	|| line2.equalsIgnoreCase("[IronDGate]")
	    	|| line2.equalsIgnoreCase("[NetherGate]")
	    	|| line2.equalsIgnoreCase("[NetherDGate]")
	    	) {
	        if (checkCreatePermissions && !player.canUseCommand("/makegate")) {
	            player.sendMessage(Colors.Rose
	                    + "You don't have permission to make gates.");
	            CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
	            return true;
	        }
	        
	        String setLine;
	        if(line2.equalsIgnoreCase("[GlassGate]") || line2.equalsIgnoreCase("[GlassDGate]"))
	        	setLine = line2.equalsIgnoreCase("[GlassGate]") ? "[GlassGate]" : "[GlassDGate]";
	        else if(line2.equalsIgnoreCase("[IronGate]") || line2.equalsIgnoreCase("[IronDGate]"))
	        	setLine = line2.equalsIgnoreCase("[IronGate]") ? "[IronGate]" : "[IronDGate]";
	        else if(line2.equalsIgnoreCase("[NetherGate]") || line2.equalsIgnoreCase("[NetherDGate]"))
	        	setLine = line2.equalsIgnoreCase("[NetherGate]") ? "[NetherGate]" : "[NetherDGate]";
	        else
	        	setLine = line2.equalsIgnoreCase("[Gate]") ? "[Gate]" : "[DGate]";
	        
	        sign.setText(1, setLine);
	        sign.update();
	        
	        listener.informUser(player);
	        
	        if (useGates) {
	            player.sendMessage(Colors.Gold + "Gate created!");
	        } else {
	            player.sendMessage(Colors.Rose + "Gates are disabled on this server.");
	        }
	        
	    // Light switch
	    } else if (line2.equalsIgnoreCase("[|]")
	            || line2.equalsIgnoreCase("[I]")) {
	        if (checkCreatePermissions && !player.canUseCommand("/makelightswitch")) {
	            player.sendMessage(Colors.Rose
	                    + "You don't have permission to make light switches.");
	            CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
	            return true;
	        }
	        
	        sign.setText(1, "[I]");
	        sign.update();
	        
	        listener.informUser(player);
	        
	        if (useLightSwitches) {
	            player.sendMessage(Colors.Gold + "Light switch created!");
	        } else {
	            player.sendMessage(Colors.Rose + "Light switches are disabled on this server.");
	        }
	
	    // Elevator
	    } else if (line2.equalsIgnoreCase("[Lift Up]")
	            || line2.equalsIgnoreCase("[Lift Down]")
	            || line2.equalsIgnoreCase("[Lift]")) {
	        if (checkCreatePermissions && !player.canUseCommand("/makeelevator")) {
	            player.sendMessage(Colors.Rose
	                    + "You don't have permission to make elevators.");
	            CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
	            return true;
	        }
	
	        if (line2.equalsIgnoreCase("[Lift Up]")) {
	            sign.setText(1, "[Lift Up]");
	        } else if (line2.equalsIgnoreCase("[Lift Down]")) {
	            sign.setText(1, "[Lift Down]");
	        } else if (line2.equalsIgnoreCase("[Lift]")) {
	            sign.setText(1, "[Lift]");
	        }
	        sign.update();
	        
	        listener.informUser(player);
	        
	        if(CraftBook.getBlockID(world, pt) != BlockType.WALL_SIGN)
	        {
	        	player.sendMessage(Colors.Rose + "Elevators must be made with wall signs.");
	        	CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
	        	return true;
	        }
	        
	        if (useElevators) {
	            if (line2.equalsIgnoreCase("[Lift Up]")) {
	                if (Elevator.hasLinkedLift(cbworld, pt, true)) {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created and linked!");
	                } else {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created but not yet linked to an existing lift sign.");
	                }
	            } else if (line2.equalsIgnoreCase("[Lift Down]")) {
	                if (Elevator.hasLinkedLift(cbworld, pt, false)) {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created and linked!");
	                } else {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created but not yet linked to an existing lift sign.");
	                }
	            } else if (line2.equalsIgnoreCase("[Lift]")) {
	                if (Elevator.hasLinkedLift(cbworld, pt, true)
	                        || Elevator.hasLinkedLift(cbworld, pt, false)) {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created and linked!");
	                } else {
	                    player.sendMessage(Colors.Gold
	                            + "Elevator created but not yet linked to an existing lift sign.");
	                }
	            }
	        } else {
	            player.sendMessage(Colors.Rose + "Elevators are disabled on this server.");
	        }
	    
	    // Toggle areas
	    } else if (line2.equalsIgnoreCase("[Area]")
	            || line2.equalsIgnoreCase("[Toggle]")) {
	        listener.informUser(player);
	        
	        if (useToggleAreas) {
	            if (hasCreatePermission(ply, "maketogglearea")
	                    && ToggleArea.validateEnvironment(ply, pt, signText)) {
	                signText.flushChanges();
	            } else {
	                CraftBook.dropSign(world, pt);
	            }
	        } else {
	            ply.printError("Area toggles are disabled on this server.");
	        }
	
	    // Bridges
	    } else if (line2.equalsIgnoreCase("[Bridge]")) {
	        listener.informUser(player);
	
	        if (useBridges) {
	            if (hasCreatePermission(ply, "makebridge")
	                    && Bridge.validateEnvironment(ply, pt, signText)) {
	                signText.flushChanges();
	            } else {
	                CraftBook.dropSign(world, pt);
	            }
	        } else {
	            player.sendMessage(Colors.Rose + "Bridges are disabled on this server.");
	        }
	        
	     // Page Reader
	    } else if (line2.equalsIgnoreCase("[Book]") || line2.equalsIgnoreCase("[Book][X]")) {
	        listener.informUser(player);
	        
	        if (usePageReader)
	        {
	        	//PageWriter.validateEnvironment() handles the permissions.
	        	if(PageWriter.validateEnvironment(ply, pt, signText))
	        	{
	        		signText.flushChanges();
	        	}
	        	else
	        	{
	                CraftBook.dropSign(world, pt);
	        	}
	        }
	        else
	        {
	            ply.printError("Page readers are disabled on this server.");
	        }
	        
	     // Map Changer
	    } else if (line2.equalsIgnoreCase("[Map]")) {
	    	listener.informUser(player);
	    	
	    	if(useMapChanger)
	    	{
	    		if(ply.hasPermission("makemapchanger"))
				{
	    			
					Vector bPt = Util.getWallSignBack(world, pt, 1);
					if(CraftBook.getBlockID(world, pt) == BlockType.WALL_SIGN && CraftBook.getBlockID(world, bPt) == BlockType.BOOKCASE)
					{
						if(signText.getLine1().isEmpty())
						{
							ply.printError("A map index number must be on line 1.");
							CraftBook.dropSign(world, pt);
						}
						else
						{
		    				try
		    				{
		    					int mapIndex = Integer.parseInt(signText.getLine1());
		    					if(mapIndex < 0 || mapIndex >= 65536)
		    					{
		    						ply.printError("Invalid map index on line 1.");
		    						CraftBook.dropSign(world, pt);
		    					}
		    				}
		    				catch(NumberFormatException e)
		    				{
		    					ply.printError("A map index number must be on line 1.");
		    					CraftBook.dropSign(world, pt);
		    				}
						}
					}
					else
					{
						ply.printError("[Map] sign must be on a Bookshelf.");
						CraftBook.dropSign(world, pt);
					}
				}
	    		else
				{
					ply.printError("You do not have permission to make that.");
					CraftBook.dropSign(world, pt);
				}
	    	}
	    	else
	    	{
	    		player.sendMessage(Colors.Rose + "Map changers are disabled on this server.");
	    	}
	    	
	    	
	    // Doors
	    } else if (line2.equalsIgnoreCase("[Door Up]")
	            || line2.equalsIgnoreCase("[Door Down]")) {
	        listener.informUser(player);
	
	        if (useDoors) {
	            if (hasCreatePermission(ply, "makedoor")
	                    && Door.validateEnvironment(ply, pt, signText)) {
	                signText.flushChanges();
	            } else {
	                CraftBook.dropSign(world, pt);
	            }
	        } else {
	            player.sendMessage(Colors.Rose + "Doors are disabled on this server.");
	        }
	    } else if(Sitting.enabled && sign.getBlock().getType() == 68 && Sitting.signHasSittingType(sign)) {
	    	
			sign.setText(1, sign.getText(1).toUpperCase());
			
			SittingType sittype = Sitting.getSittingTypeFromSign(sign);
			if(sittype == null || !player.canUseCommand(sittype.PERMISSION))
			{
				world.setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
				world.dropItem(sign.getX(), sign.getY(), sign.getZ(), 323);
				
				player.sendMessage(Colors.Rose+"You do not have permission to build that.");
				return true;
			}
			
			String valid = sittype.validate(sign);
			if(valid != null)
			{
				world.setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
				world.dropItem(sign.getX(), sign.getY(), sign.getZ(), 323);
				
				player.sendMessage(Colors.Rose+valid);
				return true;
			}
			
			sign.update();
			
		// Convert Exp
	    } else if (line2.equalsIgnoreCase("[Convert EXP]")) {
	        if(useConvertExp)
	        {
	            if(player.canUseCommand("/makeconvertexp"))
	            {
	            	Pattern regex = Pattern.compile("^[0-9]+");
	            	Matcher match = regex.matcher(sign.getText(2));
	            	
	            	if(match.find())
	            	{
	            		int levels = Integer.parseInt(match.group());
	            		if(levels < 1)
	            		{
	            			CraftBook.dropSign(world, pt);
	                		player.sendMessage(Colors.Rose+"Invalid # of EXP levels on 3rd line of the sign.");
	                		return true;
	            		}
	            	}
	            	else
	            	{
	            		CraftBook.dropSign(world, pt);
	            		player.sendMessage(Colors.Rose+"Failed to find EXP level # on 3rd line of the sign.");
	            		return true;
	            	}
	            	
	            	match = regex.matcher(sign.getText(3));
	            	
	            	if(match.find())
	            	{
	            		int bottles = Integer.parseInt(match.group());
	            		int size = player.getInventory().getContentsSize();
	            		size -= 4; //armor slots
	            		if(bottles < 0 || (int)Math.ceil(bottles / 64.0D) > size)
	            		{
	            			CraftBook.dropSign(world, pt);
	                		player.sendMessage(Colors.Rose+"Invalid # of EXP bottles on 4th line of the sign.");
	                		return true;
	            		}
	            	}
	            	else
	            	{
	            		CraftBook.dropSign(world, pt);
	            		player.sendMessage(Colors.Rose+"Failed to find # of EXP bottles on 4th line of the sign.");
	            		return true;
	            	}
	            	
	            	signText.setLine2("[Convert EXP]");
	                signText.flushChanges();
	            }
	            else
	            {
	                CraftBook.dropSign(world, pt);
	                player.sendMessage(Colors.Rose+"You do not have permission to build that.");
	            }
	        }
	        else
	        {
	        	CraftBook.dropSign(world, pt);
	            player.sendMessage(Colors.Rose + "Convert EXP is disabled on this server.");
	        }
	    }
	
	    return false;
	}

	/**
     * Called when a block is hit with the primary attack.
     * 
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(Player player, Block block) {
    	
    	World world = player.getWorld();
    	
    	int blockType = block.getType();
    	
        // Random apple drops
        if (dropAppleChance > 0 && blockType == BlockType.LEAVES
                && checkPermission(player, "/appledrops")) {
            if (block.getStatus() == 3 || block.getStatus() == 2) {
                if (Math.random() <= dropAppleChance) {
                	world.dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            ItemType.APPLE);
                }
            }

        // Bookshelf drops and Page reset
        } else if (blockType == BlockType.BOOKCASE) {
        	
            if (dropBookshelves && (block.getStatus() == 3 || block.getStatus() == 2) && checkPermission(player, "/bookshelfdrops")) {
            	destroyBookshelf(block);
            	world.dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            BlockType.BOOKCASE);
            }
            else if(block.getStatus() == 0 && player.isCreativeMode())
            {
            	destroyBookshelf(block);
            }
            else if(usePageReader && block.getStatus() == 0 && checkPermission(player, "/readpages"))
            {
            	//page reset
            	Sign sign = Util.getWallSignNextTo(world, block.getX(), block.getY(), block.getZ());
            	
            	if(sign != null && (sign.getText(1).equalsIgnoreCase("[Book]") || sign.getText(1).equalsIgnoreCase("[Book][X]")) )
            	{
            		PageWriter.resetPage(sign);
            	}
            }
        }

        return false;
    }
    
    private void destroyBookshelf(Block block)
	{
		WorldBlockVector wloc = new WorldBlockVector(CraftBook.getCBWorld(block.getWorld()), block.getX(), block.getY(), block.getZ());
		if(bookshelves.remove(wloc) != null)
			CraftBook.cbdata.markDirty();
	}

	public void onPlayerMove(Player player, Location from, Location to)
    {
    	if(!Bounce.bounce(player, from, to))
    		Bounce.repel(player, from, to);
    }
    
    public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount)
    {
    	if(type == PluginLoader.DamageType.FALL)
    	{
    		return Bounce.fallProtected(defender, amount);
    	}
    	return false;
    }
    

    
    /**
	 * Check if a player can use a command. May be overridden if permissions
	 * checking is disabled.
	 * 
	 * @param player
	 * @param command
	 * @return
	 */
	public boolean checkPermission(Player player, String command) {
	    return !checkPermissions || player.canUseCommand(command);
	}

	/**
	 * Check if a player can use a command. May be overrided if permissions
	 * checking is disabled.
	 * 
	 * @param player
	 * @param command
	 * @return
	 */
	public boolean hasCreatePermission(CraftBookPlayer player, String permission) {
	    if (!checkCreatePermissions || player.hasPermission(permission)) {
	        return true;
	    } else {
	        player.printError("You don't have permission to make that.");
	        return false;
	    }
	}

	public static void readFromNBT(ONBTTagCompound nbtcompound)
    {
    	if(bookshelves != null)
    	{
    		bookshelves.clear();
	    	ONBTTagList nbttag = nbtcompound.m("BookShelf");
	    	if(nbttag != null)
	    	{
	    		for(int i = 0; i < nbttag.c(); i++)
	    		{
	    			ONBTTagCompound nbtcomp = (ONBTTagCompound) nbttag.b(i);
	    			CBBookInventory cbbookinv = new CBBookInventory();
	    			cbbookinv.readFromNBT(nbtcomp);
	    			
	    			WorldBlockVector loc = new WorldBlockVector(cbbookinv.cbworld, cbbookinv.x, cbbookinv.y, cbbookinv.z);
	    			bookshelves.put(loc, cbbookinv);
	    		}
	    	}
    	}
    }
    
    public static void writeToNBT(ONBTTagCompound nbtcompound)
    {
    	ONBTTagList addtag = new ONBTTagList();
    	ONBTTagCompound addnbt;
    	if(bookshelves != null && bookshelves.size() > 0)
    	{
    		Iterator<Map.Entry<WorldBlockVector, CBBookInventory>> it = bookshelves.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, CBBookInventory> item = (Map.Entry<WorldBlockVector, CBBookInventory>) it.next();
				CBBookInventory bookinv = item.getValue();
				addnbt = new ONBTTagCompound();
				bookinv.writeToNBT(addnbt);
				addtag.a((ONBTBase)addnbt);
			}
			
			nbtcompound.a("BookShelf", (ONBTBase)addtag);
    	}
    }
    
    private void setPropertyFields() {
        if(properties.containsKey("pagereader-enable"))
        	usePageReader = properties.getBoolean("pagereader-enable", true);
        if(properties.containsKey("page-hidden-switches-enable"))
        	usePageSwitches = properties.getBoolean("page-hidden-switches-enable", true);
        if(properties.containsKey("map-changer-enable"))
        	useMapChanger = properties.getBoolean("map-changer-enable", true);
        useBookshelves = properties.getBoolean("bookshelf-enable", true);
        bookReadLine = properties.getString("bookshelf-read-text", "You pick out a book...");
        useLightSwitches = properties.getBoolean("light-switch-enable", true);
        useGates = properties.getBoolean("gate-enable", true);
        redstoneGates = properties.getBoolean("gate-redstone", true);
        useElevators = properties.getBoolean("elevators-enable", true);
        useBridges = properties.getBoolean("bridge-enable", true);
        redstoneBridges = properties.getBoolean("bridge-redstone", true);
        useHiddenSwitches = properties.getBoolean("door-enable", true);
        Bridge.allowedBlocks = Util.toBlockIDSet(properties.getString("bridge-blocks", "4,5,20,43"));
        Bridge.maxLength = properties.getInt("bridge-max-length", 30);
        if(properties.containsKey("bridge-ic-blocks"))
        	Bridge.allowedICBlocks = getICBlockList(properties.getString("bridge-ic-blocks", "all"));
        useDoors = properties.getBoolean("door-enable", true);
        redstoneDoors = properties.getBoolean("door-redstone", true);
        Door.allowedBlocks = Util.toBlockIDSet(properties.getString("door-blocks", "1,3,4,5,17,20,35,43,44,45,47,80,82"));
        Door.maxLength = properties.getInt("door-max-length", 30);
        if(properties.containsKey("door-ic-blocks"))
        	Door.allowedICBlocks = getICBlockList(properties.getString("door-ic-blocks", "all"));
        if(properties.containsKey("bounce-block"))
        	Bounce.blockBounce = StringUtil.getPropColorInt(properties.getString("bounce-block"), 0, -1);
        if(properties.containsKey("soft-land-block"))
        	Bounce.blockSoft = StringUtil.getPropColorInt(properties.getString("soft-land-block"), 0, -1);
        if(properties.containsKey("bounce-force"))
        {
        	Bounce.force = properties.getInt("bounce-force", 3);
        	if(Bounce.force < 1)
        		Bounce.force = 1;
        }
        if(properties.containsKey("bounce-ic-max-force"))
        	Bounce.maxICForce = properties.getInt("bounce-ic-max-force", 8);
        if(properties.containsKey("bounce-ic-blocks"))
        {
        	Bounce.allowedICBlocks = getICBlockList(properties.getString("bounce-ic-blocks", "all"));
        	if(Bounce.allowedICBlocks.size() == 0)
        		Bounce.allowedICBlocks = null;
        }
        if(properties.containsKey("convert-exp-enable"))
        	useConvertExp = properties.getBoolean("convert-exp-enable", false);
        if(properties.containsKey("enchantment-craft-max"))
        	maxEnchantAmount = properties.getInt("enchantment-craft-max", 3);
        if(this.properties.containsKey("sitting-enabled"))
			Sitting.enabled = this.properties.getBoolean("sitting-enabled", true);
		if(this.properties.containsKey("require-permission-to-right-click-sit"))
			Sitting.requireRightClickPermission = this.properties.getBoolean("require-permission-to-right-click-sit", true);
		if(this.properties.containsKey("right-click-sit-on-any-stair"))
			Sitting.requiresChairFormats = !this.properties.getBoolean("right-click-sit-on-any-stair", true);
		Sitting.healWhileSitting = Sitting.HealingType.NONE;
		if(this.properties.containsKey("heal-while-sitting"))
		{
			try
			{
				String type = this.properties.getString("heal-while-sitting", "NONE").toUpperCase();
				Sitting.healWhileSitting = Sitting.HealingType.valueOf(type);
			}
			catch(IllegalArgumentException e)
			{
				Sitting.healWhileSitting = Sitting.HealingType.NONE;
			}
		}
		Sitting.globalHealingRate = 20;
		if(this.properties.containsKey("heal-while-sitting-rate"))
		{
			int rate = this.properties.getInt("heal-while-sitting-rate", 20);
			if(rate < 2)
			{
				Sitting.globalHealingRate = 2;
			}
			else if(rate > 100)
			{
				Sitting.globalHealingRate = 100;
			}
			else
			{
				Sitting.globalHealingRate = rate;
			}
		}
		if(properties.containsKey("bookshelf-enabled"))
			useBookShelf = properties.getBoolean("bookshelf-enabled", false);
		if(properties.containsKey("bookshelf-require-power"))
        	bookshelfNeedsPower = properties.getBoolean("bookshelf-require-power", false);
        dropBookshelves = properties.getBoolean("drop-bookshelves", true);
        try {
            dropAppleChance = Double.parseDouble(properties.getString("apple-drop-chance", "0.5")) / 100.0;
        } catch (NumberFormatException e) {
            dropAppleChance = -1;
            logger.warning("Invalid apple drop chance setting in craftbook.properties");
        }
        useHiddenSwitches = properties.getBoolean("hidden-switches-enable", true);
        useToggleAreas = properties.getBoolean("toggle-areas-enable", true);
        redstoneToggleAreas = properties.getBoolean("toggle-areas-redstone", true);
        checkPermissions = properties.getBoolean("check-permissions", false);
        checkCreatePermissions = properties.getBoolean("check-create-permissions", false);
        cauldronModule = null;
        enableAmmeter = properties.getBoolean("ammeter", true);
    }
    
    private ArrayList<Integer> getICBlockList(String arg)
	{
		if(arg.equalsIgnoreCase("all"))
			return null;
		
		String[] args = arg.split(",");
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < args.length; i++)
		{
			try
			{
				list.add( Integer.parseInt(args[i]) );
			}
			catch(NumberFormatException e)
			{
				
			}
		}
		
		return list;
	}

	/**
     * Load the cauldron.
     */
    private void loadCauldron() {
        if (properties.getBoolean("cauldron-enable", true)) {
            try {
                CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");

                if (recipes.size() != 0) {
                    cauldronModule = new Cauldron(recipes);
                    logger.info(recipes.size()
                            + " cauldron recipe(s) loaded");
                } else {
                    logger.warning("cauldron-recipes.txt had no recipes");
                }
            } catch (FileNotFoundException e) {
                logger.info("cauldron-recipes.txt not found: " + e.getMessage());
                try {
                    logger.info("Looked in: " + (new File(".")).getCanonicalPath());
                } catch (IOException ioe) {
                    // Eat error
                }
            } catch (IOException e) {
                logger.warning("cauldron-recipes.txt not loaded: " + e.getMessage());
            }
        } else {
            cauldronModule = null;
        }
    }
    
    /**
	 * Read a file containing cauldron recipes.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static CauldronCookbook readCauldronRecipes(String path)
	        throws IOException {
	    
	    File file = new File(path);
	    FileReader input = null;
	    CauldronCookbook cookbook = new CauldronCookbook();
	
	    try {
	        input = new FileReader(file);
	        BufferedReader buff = new BufferedReader(input);
	
	        String line;
	        while ((line = buff.readLine()) != null) {
	            line = line.trim();
	
	            // Blank lines
	            if (line.length() == 0) {
	                continue;
	            }
	
	            // Comment
	            if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
	                continue;
	            }
	
	            String[] parts = line.split(":");
	            
	            if (parts.length < 3) {
	                logger.log(Level.WARNING, "Invalid cauldron recipe line in "
	                        + file.getName() + ": '" + line + "'");
	            } else {
	                String name = parts[0];
	                List<CraftBookItem> ingredients = parseCauldronItems(parts[1]);
	                List<CraftBookItem> results = parseCauldronItems(parts[2]);
	                String[] groups = null;
	                
	                if (parts.length >= 4 && parts[3].trim().length() > 0) {
	                    groups = parts[3].split(",");
	                }
	                
	                CauldronRecipe recipe =
	                        new CauldronRecipe(name, ingredients, results, groups);
	                cookbook.add(recipe);
	            }
	        }
	
	        return cookbook;
	    } finally {
	        try {
	            if (input != null) {
	                input.close();
	            }
	        } catch (IOException e) {
	        }
	    }
	}

	/**
	 * Parse a list of cauldron items.
	 * 
	 * @param list
	 * @return
	 */
	private static List<CraftBookItem> parseCauldronItems(String list) {
	    String[] parts = list.split(",");
	
	    List<CraftBookItem> out = new ArrayList<CraftBookItem>();
	
	    for (String part : parts) {
	        int multiplier = 1;
	        try {
	            // Multiplier
	            if (part.matches("^.*\\*([0-9]+)$")) {
	                int at = part.lastIndexOf("*");
	                multiplier = Integer.parseInt(
	                        part.substring(at + 1, part.length()));
	                part = part.substring(0, at);
	            }
	            
	            CraftBookItem cbitem = UtilItem.parseCBItem(part);
	            if(cbitem == null || cbitem.id() == 0)
	            {
	            	logger.warning("Cauldron: invalid item: "+part);
	            	continue;
	            }
	            
	            if(cbitem.color() == -1)
	            	cbitem = cbitem.setColor(0);
	            
	            if(BlockType.isDirectionBlock(cbitem.id()))
	            {
	        		cbitem = cbitem.setColor(0);
	            }
	            
	            for(int i = 0; i < multiplier; i++)
	            {
	            	out.add(cbitem.clone());
	            }
	            
	        } catch (NumberFormatException e) { // Bad multiplier
	            logger.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
	        }
	    }
	
	    return out;
	}

	private void loadMinecartCraftRecipes() {
        if (!properties.containsKey("minecart-craft-enable") || properties.getBoolean("minecart-craft-enable", true)) {
            try {
                CauldronCookbook recipes = readCauldronRecipes("cb-cartcraft-recipes.txt");

                if (recipes.size() != 0) {
                	VehicleListener.craftBlockRecipes = recipes;
                    logger.info(recipes.size()
                            + " minecart craft recipe(s) loaded");
                } else {
                    logger.warning("cb-cartcraft-recipes.txt had no recipes");
                }
            } catch (FileNotFoundException e) {
                logger.info("cb-cartcraft-recipes.txt not found: " + e.getMessage());
                try {
                    logger.info("Looked in: " + (new File(".")).getCanonicalPath());
                } catch (IOException ioe) {
                    // Eat error
                }
            } catch (IOException e) {
                logger.warning("cb-cartcraft-recipes.txt not loaded: " + e.getMessage());
            }
        } else {
        	VehicleListener.craftBlockRecipes = null;
        }
    }
	


	private void setHiddenSwitch(String type, boolean state, World world, int x, int y, int z) {
	    ComplexBlock cblock = world.getComplexBlock(x, y, z);
	    
	    if (cblock instanceof Sign) {
	        Sign sign = (Sign)cblock;
	        
	        if (sign.getText(1).equalsIgnoreCase(type)) {
	        	CraftBookWorld cbworld = CraftBook.getCBWorld(world);
	            Redstone.setOutput(cbworld, new Vector(x, y - 1, z), state);
	            Redstone.setOutput(cbworld, new Vector(x, y + 1, z), state);
	            Redstone.setOutput(cbworld, new Vector(x - 1, y, z), state);
	            Redstone.setOutput(cbworld, new Vector(x + 1, y, z), state);
	            Redstone.setOutput(cbworld, new Vector(x, y, z - 1), state);
	            Redstone.setOutput(cbworld, new Vector(x, y, z + 1), state);
	        }
	    }
	}
} //end of class


