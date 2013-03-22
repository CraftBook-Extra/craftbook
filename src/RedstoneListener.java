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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import olegbl.perlstone32.Perlstone32_1;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import lymia.customic.*;
import lymia.perlstone.Perlstone_1_0;
import lymia.plc.PlcLang;


/**
 * Event listener for redstone enhancements such as redstone pumpkins and
 * integrated circuits. Redstone hooks for mechanisms are in
 * MechanismListener.
 * 
 * @author sk89q
 * @author Lymia
 */
public class RedstoneListener extends CraftBookDelegateListener
        implements CustomICAccepter, SignPatch.ExtensionListener, 
                   TickExtensionListener {
    
    /**
     * Currently registered ICs
     */
    private Map<String,RegisteredIC> icList = 
            new HashMap<String,RegisteredIC>(32);

    private HashMap<String,PlcLang> plcLanguageList = 
            new HashMap<String,PlcLang>();

    private CBXInstantICManager instantICs;
    
    private boolean checkCreatePermissions = false;
    private boolean redstonePumpkins = true;
    private boolean redstoneNetherstone = false;
    private boolean redstoneICs = true;
    private boolean redstonePLCs = true;
    private boolean redstonePLCsRequirePermission = true;
    private boolean listUnusuableICs = true;
    
    private boolean enableSelfTriggeredICs = true;
    private boolean restrictSelfTriggeredICs = false;
    
    private boolean updateSelfTriggeredICList = false;
    
    protected static int chestCollectorMaxRange = 64;
    protected static int icMessageMaxRange = 64;
    protected static int icInAreaMaxLength = 64;
    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public RedstoneListener(CraftBook craftBook, CraftBookListener listener) {
        super(craftBook, listener);
        registerLang("perlstone_v1.0",new Perlstone_1_0());
        registerLang("perlstone32_v1",new Perlstone32_1());
    }
    
    public void run() {
    	onTick();
    }

    /**
     * Loads relevant configuration.
     */
    @Override
    public void loadConfiguration() {
    	setPropertyFields();
    	icList.clear();
        loadCustomICs();
        addDefaultICs();
        instantICs = new CBXInstantICManager(this);
        instantICs.findInstantICs();
    }
    

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(Player player, Sign sign) {
    	World world = player.getWorld();
    	
        int type = CraftBook.getBlockID(world,
                sign.getX(), sign.getY(), sign.getZ());
        
        String line2 = sign.getText(1);
        int len = line2.length();
        
        // ICs
        if (line2.length() > 7
                && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                line2.charAt(7) == ']') {

            // Check to see if the player can even create ICs
            if (checkCreatePermissions
                    && !player.canUseCommand("/makeic")) {
                player.sendMessage(Colors.Rose
                        + "You don't have permission to make ICs.");
                CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                return true;
            }

            String id = line2.substring(1, 7).toUpperCase();
            RegisteredIC ic = icList.get(id);

            if (ic != null) {
            	
                if (ic.isPlc) {
                    if (!redstonePLCs) {
                        player.sendMessage(Colors.Rose + "PLCs are not enabled.");
                        CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                        return false;
                    }
                }
                
                if (!canCreateIC(player, id, ic)) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make " + id + ".");
                    CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                    return true;
                } else {
                	// TODO: rewrite option system
                	String options = line2.substring(8);
            		switch(options.length())
            		{
            			case 1:
            			case 2:
            				break;
            			case 3:
            			case 4:
            			case 7:
            				options = options.substring(1);
            				break;
            		}
            		
                	if(len > 9)
                	{
                		byte direction = 0;
                		if(options.length() == 2 || options.length() == 3)
                		{
                			direction = UtilIC.getExtendDirection(line2);
                		}
                		
                		if(direction == 0)
                		{
	                		int[][] orders = getIOOrder(options);
	                		if(orders[0] == null && orders[1] == null)
	                		{
	                			//options = "";
	                			//player.sendMessage(Colors.Rose + "Unrecognized options: " + line2.substring(8));
	                		}
	                		else
	                			options = line2.substring(8);
                		}
                		else
                		{
                			options = line2.substring(8);
                		}
                	}
                	
                    // To check the environment
                    Vector pos = new Vector(sign.getX(), sign.getY(), sign.getZ());
                    SignText signText = new SignText(
                        sign.getText(0), sign.getText(1), sign.getText(2),
                        sign.getText(3));

                    CraftBookWorld cbworld = CraftBook.getCBWorld(world);
                    
                    // Maybe the IC is setup incorrectly
                    String envError = ic.ic.validateEnvironment(cbworld, pos, signText);

                    if (signText.isChanged()) {
                        sign.setText(0, signText.getLine1());
                        sign.setText(1, signText.getLine2());
                        sign.setText(2, signText.getLine3());
                        sign.setText(3, signText.getLine4());
                    }

                    if (envError != null) {
                        player.sendMessage(Colors.Rose
                                + "Error: " + envError);
                        CraftBook.dropSign(world, sign.getX(), sign.getY(), sign.getZ());
                        return true;
                    } else {
                        sign.setText(0, ic.ic.getTitle());
                        sign.setText(1, "[" + id + "]"+options);
                    }
                    
                    if(enableSelfTriggeredICs && (ic.type.isSelfTriggered || ic.type.updateOnce) ) {
                        instantICs.add(new WorldBlockVector(cbworld, pos));
                    }
                    sign.update();
                }
                
                if (ic.isPlc && !redstonePLCs && redstoneICs) {
                    player.sendMessage(Colors.Rose + "Warning: PLCs are disabled.");
                }
            } else {
                sign.setText(1, Colors.Red + line2);
                player.sendMessage(Colors.Rose + "Unrecognized IC: " + id);
            }

            if (!redstoneICs) {
                player.sendMessage(Colors.Rose + "Warning: ICs are disabled.");
            } else if (type == BlockType.SIGN_POST) {
                player.sendMessage(Colors.Rose + "Warning: IC signs must be on a wall.");
            }

            return false;
        }
        
        return false;
    }
    
	public void onChunkLoaded(final Chunk chunk) {
		if (!enableSelfTriggeredICs || !updateSelfTriggeredICList) {
			return;
		}
		instantICs.onChunkLoaded(chunk);
	}


	public void onChunkUnload(final Chunk chunk) {
		if (!updateSelfTriggeredICList) {
			return;
		}
		instantICs.onChunkUnload(chunk);
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
    public void onDirectWireInput(World world, final Vector pt, boolean isOn, final Vector changed) {
        int type = CraftBook.getBlockID(world, pt);
        // Redstone pumpkins
        if (redstonePumpkins
                && (type == BlockType.PUMPKIN || type == BlockType.JACKOLANTERN)) {
            Boolean useOn = Redstone.testAnyInput(world, pt);

            int data = CraftBook.getBlockData(world, pt);
            if (useOn != null && useOn) {
                CraftBook.setBlockID(world, pt, BlockType.JACKOLANTERN);
            } else if (useOn != null) {
                CraftBook.setBlockID(world, pt, BlockType.PUMPKIN);
            }
            CraftBook.setBlockData(world, pt, data);
        // Redstone netherstone
        } else if (redstoneNetherstone
                && (type == BlockType.NETHERSTONE)) {
            Boolean useOn = Redstone.testAnyInput(world, pt);
            Vector above = pt.add(0, 1, 0);

            if (useOn != null && useOn && CraftBook.getBlockID(world, above) == 0) {
                CraftBook.setBlockID(world, above, BlockType.FIRE);
            } else if (useOn != null && !useOn && CraftBook.getBlockID(world, above) == BlockType.FIRE) {
                CraftBook.setBlockID(world, above, 0);
            }
        } else if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = world.getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (!(cblock instanceof Sign)) {
                return;
            }

            final Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);
            //int len = line2.length();
            
            // ICs
            if (redstoneICs && type == BlockType.WALL_SIGN
                    && line2.length() > 7
                    && line2.substring(0, 3).equalsIgnoreCase("[MC")
                    && line2.charAt(7) == ']') {
            	
                final String id = line2.substring(1, 7).toUpperCase();
                final SignText signText = new SignText(sign.getText(0),
                        sign.getText(1), sign.getText(2), sign.getText(3));

                final RegisteredIC ic = icList.get(id);
                
                if (ic == null) {
                    sign.setText(1, Colors.Red + line2);
                    sign.update();
                    return;
                }
                
                if (ic.type.isSelfTriggered) {
                    return;
                }
                
                
                final char mode;
            	
                String options = line2.substring(8);
        		switch(options.length())
        		{
        			case 1:
        			case 4:
        			case 7:
        				mode = options.charAt(0);
        				options = options.substring(1);
        				break;
        			default:
        				mode = ' ';
        				break;
        		}
                
            	final int[] abc;
        		final int[] def;
        		int[][] orders = new int[][]{null, null};
        		
                if(line2.length() > 9)
            	{
            		orders = getIOOrder(options);
            	}
                
                if(orders[0] == null)
                	abc = new int[]{0, 1, 2};
                else
                	abc = orders[0];
                
                if(orders[1] == null)
                	def = new int[]{0, 1, 2};
                else
                	def = orders[1];
                
                
                final RedstoneListener thisListener = this;
                
                final CraftBookWorld cbworld = CraftBook.getCBWorld(world);

                craftBook.getDelay(cbworld).delayAction(
                        new TickDelayer.Action(world, pt.toBlockVector(), 2) {
                    @Override
                    public void run() {
                    	// Our chunk might have been unloaded. 
                    	// Don't process unloaded ICs, or the chunk might be reloaded when it shouldn't be
                    	// and cause a memory leak.
                    	if(! CraftBook.getWorld(cbworld).isChunkLoaded(pt.getBlockX(),pt.getBlockY(),pt.getBlockZ())) return;
                    	//not too happy about putting in the "extra" param to get the blockbag access cuz it's a hack.
                    	//hopefully I can change it later on.
                    	if(id.equals("MCX206") || id.equals("MCX207") || id.equals("MCX208") || id.equals("MCX209") || id.equals("MCX210"))
                    		ic.think(cbworld, pt, changed, signText, sign, craftBook.getDelay(cbworld), mode, abc, def, listener.getBlockBag(cbworld, pt));
                    	else if(id.equals("MCU440")
                    			|| id.equals("MCU131") || id.equals("MCU132")
                    			|| id.equals("MCU140") || id.equals("MCU142") || id.equals("MCU144") || id.equals("MCU146")
                    			|| id.equals("MCU700") || id.equals("MCU701") || id.equals("MCU702") || id.equals("MCU705")
                    			|| id.equals("MCU211") || id.equals("MCU212") || id.equals("MCU213") || id.equals("MCU214")
                    			|| id.equals("MCU217")
                    			|| id.equals("MCU200")
                    			|| id.equals("MCU220") || id.equals("MCU221") || id.equals("MCU222")
                    			|| id.equals("MCX228")
                    			|| id.equals("MCU292") || id.equals("MCU293")
                    			|| id.equals("MCU300") || id.equals("MCU301") || id.equals("MCU302") || id.equals("MCU303") || id.equals("MCU304") )
                    		ic.think(cbworld, pt, changed, signText, sign, craftBook.getDelay(cbworld), mode, abc, def, thisListener);
                        else
                        	ic.think(cbworld, pt, changed, signText, sign, craftBook.getDelay(cbworld), mode, abc, def, null);

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
                });
            }
        }
    }

    public void onTick() {
    	//[NOTE]: Placing Bounce here instead of Mech to re-use onTick. If more onTick is needed in Mech, then move.
    	if(Bounce.bounceCreatures){
    		Bounce.bounceCreatures();
    	}	
    	instantICs.runInstantICs();	
    }

	public void onSignAdded(World world, int x, int y, int z) {
		ComplexBlock block = world.getComplexBlock(x, y, z);
		if (block instanceof Sign) {
			Sign sign = (Sign) block;
			onSignAdded(CraftBook.getCBWorld(world), sign);
		}
	}

	public void onSignAdded(World world, OTileEntitySign oTES) {
		Sign sign = new Sign((OTileEntitySign) oTES);
		if (sign.getBlock().getType() == BlockType.WALL_SIGN) {
			onSignAdded(CraftBook.getCBWorld(sign.getWorld()), sign);
		}
	}

	public void onSignAdded(CraftBookWorld cbworld, Sign sign) {
		String line2 = sign.getText(1);
		if (line2.length() < 8)
			return;

		String id = line2.substring(1, 7).toUpperCase();
		RegisteredIC ic = icList.get(id);
		if (ic == null) {
			if (line2.startsWith("[MC")) {
				sign.setText(1, Colors.Red + line2);
				sign.update();
			}
			return;
		}
		if (ic.type.isSelfTriggered && !enableSelfTriggeredICs)
			return;
		if (!ic.type.isSelfTriggered && !ic.type.updateOnce)
			return;

		instantICs.add(new WorldBlockVector(cbworld, sign.getX(), sign.getY(),
				sign.getZ()));
	}
    
    public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand)
    {
    	if(blockPlaced != null && MCX221.icAreas != null && MCX221.icAreas.size() > 0)
    	{
    		Iterator<Map.Entry<WorldBlockVector, BlockArea>> it = MCX221.icAreas.entrySet().iterator();
    		while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(CraftBook.getCBWorld(blockPlaced.getWorld()), blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ()))
				{
					SignText text = CraftBook.getSignText(blockPlaced.getWorld(), item.getKey());
					if(text == null)
					{
						it.remove();
						continue;
					}
					String line2 = text.getLine2();
			    	if(!line2.startsWith("[MC") || line2.length() < 8)
			    	{
						it.remove();
						continue;
					}
			    	
			    	String id = line2.substring(1, 7).toUpperCase();
					if(id.equals("MCU221") || id.equals("MCU222"))
					{
						boolean stopBreak = MCX221.blockPlaced(item.getKey(), text);
						return stopBreak;
					}
					else
					{
						it.remove();
						continue;
					}
				}
			}
    	}
    	
    	return false;
    }
    
    public boolean onBlockBreak(Player player, Block block)
    {
    	if(player == null || block == null)
    		return false;
    	
    	if(MCX220.icAreas != null && MCX220.icAreas.size() > 0)
    	{
    		Iterator<Map.Entry<WorldBlockVector, BlockArea>> it = MCX220.icAreas.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry<WorldBlockVector, BlockArea> item = (Map.Entry<WorldBlockVector, BlockArea>) it.next();
				BlockArea area = item.getValue();
				if(area.containsPoint(CraftBook.getCBWorld(block.getWorld()), block.getX(), block.getY(), block.getZ()))
				{
					SignText text = CraftBook.getSignText(block.getWorld(), item.getKey());
					if(text == null)
					{
						it.remove();
						continue;
					}
					String line2 = text.getLine2();
			    	if(!line2.startsWith("[MC") || line2.length() < 8)
			    	{
						it.remove();
						continue;
					}
			    	
			    	String id = line2.substring(1, 7).toUpperCase();
					if(id.equals("MCU220") || id.equals("MCU222"))
					{
						boolean stopBreak = MCX220.blockBroke(item.getKey(), text);
						return stopBreak;
					}
					else
					{
						it.remove();
						continue;
					}
				}
			}
    	}
    	
    	if(block.getType() == Block.Type.WallSign.getType())
    	{
	    	World world = player.getWorld();
	    	
	    	Sign sign = (Sign)world.getComplexBlock(block);
	    	
	    	String line2 = sign.getText(1);
	    	if(!line2.startsWith("[MC") || line2.length() < 8)
	        	return false;
	    	
	    	String id = line2.substring(1, 7).toUpperCase();
	        RegisteredIC ic = icList.get(id);
	        if (ic == null)
	        	return false;
	        
	        if(id.equals("MCZ236"))
	        {
	        	if(MCX236.isSameCoord(MCX236.players.get(player),
	        			CraftBook.getCBWorld(world),
	        			new Vector(block.getX(), block.getY(), block.getZ())))
	        	{
		        	MCX236.players.remove(player);
	        	}
	        }
	        else if(id.equals("MCZ238"))
	        {
	        	if(MCX236.isSameCoord(MCX238.players.get(player),
	        			CraftBook.getCBWorld(world),
	        			new Vector(block.getX(), block.getY(), block.getZ())))
	        	{
	        		MCX238.players.remove(player);
	        	}
	        }
	        
	        if(!ic.type.updateOnce)
	        	return false;
	        
	        SignText signText = new SignText(sign.getText(0), line2, sign.getText(2), sign.getText(3));
	        Vector pos = new Vector(block.getX(), block.getY(), block.getZ());
	        
	        String message = ic.ic.clear(CraftBook.getCBWorld(world), pos, signText);
	        if(message != null)
	        {
	        	//player.sendMessage(Colors.Rose + message);
	        }
    	}
    	
    	return false;
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
    public void onBlockRightClicked(Player player, Block blockClicked, Item item)
    {
    	if( (item == null || item.getItemId() == 0)
    		&& player.canUseCommand("/cbrightclicksignupdate")
    		&& blockClicked != null
    		&& blockClicked.getType() == BlockType.WALL_SIGN){
    		// get clicked sign
    		World world = player.getWorld();
        	Sign sign = (Sign)world.getComplexBlock(blockClicked);
        	// check if sign might be an IC
        	String line2 = sign.getText(1);
        	if(line2.length() < 8 || !line2.startsWith("[MC"))
            	return;
        	// get IC
        	String id = line2.substring(1, 7).toUpperCase();
        	RegisteredIC ic = icList.get(id);
            if (ic == null)
            	return;
            if(!ic.type.isSelfTriggered)
            	return;
            onSignAdded(CraftBook.getCBWorld(world), sign);
    	}
    }
    
 	@Override
	public void onDisconnect(Player player){
		MCX236.players.remove(player);
		MCX238.players.remove(player);
	}
    
    public boolean selfTriggeredICsEnabled() {
    	return this.enableSelfTriggeredICs;
    }
    
    public boolean selfTriggeredICsUpdating() {
    	return this.updateSelfTriggeredICList;
    }
    
    public RegisteredIC getIC(String id)
    {
    	return icList.get(id);
    }
    /**
     * Register a new IC. Defined by the interface CustomICAccepter.
     * 
     * @param name
     * @param ic
     * @param type
     */
    public void registerIC(String name, IC ic, String type)
            throws CustomICException {
        if (icList.containsKey(name)) {
            throw new CustomICException("IC already defined");
        }
        ICType icType = getICType(type);
        if(!enableSelfTriggeredICs && icType.isSelfTriggered) return;
        
        registerIC(name, ic, icType, false);
    }
    
    /**
     * Registers a new non-PLC IC.
     * 
     * @param name
     * @param ic
     * @param type
     */
    public void registerIC(String name, IC ic, ICType type) {
        registerIC(name, ic, type, false);
    }

    /**
     * Registers a new IC.
     * 
     * @param name
     * @param ic
     * @param isPlc
     */
    public void registerIC(String name, IC ic, ICType type, boolean isPlc) {
        icList.put(name, new RegisteredIC(ic, type, isPlc));
    }

    
    public void registerLang(String name, PlcLang language) {
        plcLanguageList.put(name, language);
        craftBook.getStateManager().addStateHolder(name, language);
    }
    
    // ----------------
    // private methods
    // ----------------
    
    /**
	 * Used for the /listics command.
	 * 
	 * @param p
	 * @return
	 */
	public String[] generateICText(Player p) {
	    ArrayList<String> icNameList = new ArrayList<String>();
	    icNameList.addAll(icList.keySet());
	
	    Collections.sort(icNameList);
	
	    ArrayList<String> strings = new ArrayList<String>();
	    for (String ic : icNameList) {
	        RegisteredIC ric = icList.get(ic);
	        boolean canUse = canCreateIC(p, ic, ric);
	        boolean auto = ric.type.isSelfTriggered;
	        if (listUnusuableICs) {
	            strings.add(Colors.Rose + ic + " (" + ric.type.name + ")"
	                    + (auto ? " (SELF-TRIGGERED)" : "") + ": "
	                    + ric.ic.getTitle() + (canUse ? "" : " (RESTRICTED)"));
	        } else if (canUse) {
	            strings.add(Colors.Rose + ic + " (" + ric.type.name + ")"
	                    + (auto ? " (SELF-TRIGGERED)" : "") + ": "
	                    + ric.ic.getTitle());
	        }
	    }
	    return strings.toArray(new String[0]);
	}

	/**
     * populates property fields from the CraftBook.properties file
     */
    private void setPropertyFields(){
        checkCreatePermissions = properties.getBoolean("check-create-permissions", false);
        redstonePumpkins = properties.getBoolean("redstone-pumpkins", true);
        redstoneNetherstone = properties.getBoolean("redstone-netherstone", false);
        redstoneICs = properties.getBoolean("redstone-ics", true);
        redstonePLCs = properties.getBoolean("redstone-plcs", true);
        redstonePLCsRequirePermission = properties.getBoolean("redstone-plcs-require-permission", false);
        listUnusuableICs = properties.getBoolean("ic-list-show-unusuable",true);
        enableSelfTriggeredICs = properties.getBoolean("enable-self-triggered-ics",true);
        restrictSelfTriggeredICs = properties.getBoolean("self-triggered-ics-require-premission",false);
       	updateSelfTriggeredICList = properties.getBoolean("chunk-updated-self-triggered-ic-list",true);
       	Bounce.bounceCreatures = properties.getBoolean("bounce-creatures", false);
       	if(properties.containsKey("ic-chest-collector-max-range")) {
        	chestCollectorMaxRange = properties.getInt("ic-chest-collector-max-range", 64);
        	if(chestCollectorMaxRange < 1)
        		chestCollectorMaxRange = 1;
        	else if(chestCollectorMaxRange > 64)
        		chestCollectorMaxRange = 64;
        }
        if(properties.containsKey("ic-message-max-range")) {
        	icMessageMaxRange = properties.getInt("ic-message-max-range", 64);
        	if(icMessageMaxRange < 1)
        		icMessageMaxRange = 1;
        	else if(icMessageMaxRange > 64)
        		icMessageMaxRange = 64;
        }
        if(properties.containsKey("ic-in-area-max-length")) {
        	icInAreaMaxLength = properties.getInt("ic-in-area-max-length", 64);
        	if(icInAreaMaxLength < 1)
        		icInAreaMaxLength = 1;
        	else if(icInAreaMaxLength > 64)
        		icInAreaMaxLength = 64;
        }
        if(properties.containsKey("ic-potions-allowed")) {
        	String values = properties.getString("ic-potions-allowed");
        	if(values != null && !values.isEmpty()) {
        		String[] args = values.split(",");
        		for(PotionType type : PotionType.values()) {
        			type.allowed = false;
        		}
        		for(int i = 0; i < args.length; i++) {
        			PotionType type = PotionType.getEffectFromId(Integer.parseInt(args[i]));
        			if(type == null) {
        				logger.warning("craftbook.properties [ic-potions-allowed] contained invalid value: "+args[i]);
        				continue;
        			}
        			type.allowed = true;
        		}
        	}
        }
        if(properties.containsKey("ic-particles-allowed")) {
        	String values = properties.getString("ic-particles-allowed");
        	if(values != null && !values.isEmpty()) {
        		String[] args = values.split(",");
        		for(ParticleType type : ParticleType.values()) {
        			type.allowed = false;
        		}
        		
        		for(int i = 0; i < args.length; i++) {
        			ParticleType type = ParticleType.getParticle(args[i]);
        			if(type == null) {
        				logger.warning("craftbook.properties [ic-particles-allowed] contained invalid value: "+args[i]);
        				continue;
        			}
        			type.allowed = true;
        		}
        	}
        }
        if(properties.containsKey("ic-sfx-allowed")) {
        	String values = properties.getString("ic-sfx-allowed");
        	if(values != null && !values.isEmpty())	{
        		String[] args = values.split(",");
        		for(SFXType type : SFXType.values()) {
        			type.allowed = false;
        		}
        		for(int i = 0; i < args.length; i++) {
        			SFXType type = SFXType.getEffect(args[i]);
        			if(type == null) {
        				logger.warning("craftbook.properties [ic-sfx-allowed] contained invalid value: "+args[i]);
        				continue;
        			}
        			type.allowed = true;
        		}
        	}
        }
        if(properties.containsKey("ic-enchantments-allowed")) {
        	String values = properties.getString("ic-enchantments-allowed");
        	if(values != null && !values.isEmpty()) {
        		String[] args = values.split(",");        		
        		for(EnchantmentType type : EnchantmentType.values()) {
        			type.allowed = false;
        		}
        		for(int i = 0; i < args.length; i++) {
        			EnchantmentType type = EnchantmentType.getEnchantment(args[i]);
        			if(type == null) {
        				logger.warning("craftbook.properties [ic-enchantments-allowed] contained invalid value: "+args[i]);
        				continue;
        			}
        			type.allowed = true;
        		}
        	}
        }
    }
    
    /**
     * loads custom ICs from custom-ics.txt
     */
    private void loadCustomICs(){
	    if (properties.getBoolean("custom-ics", true)) {
	        try {
	            CustomICLoader.load("custom-ics.txt", this, plcLanguageList);
	            logger.info("Custom ICs for CraftBook loaded");
	        } catch (CustomICException e) {
	            Throwable cause = e.getCause();
	            if (cause != null && !(cause instanceof FileNotFoundException)) {
	                logger.log(Level.WARNING,
	                        "Failed to load custom IC file: " + e.getMessage());
	            }
	        }
	    }
    }
    
    /**
     * Populate the IC list with the default ICs.
     */
    private void addDefaultICs() {
        if (enableSelfTriggeredICs) {
            internalRegisterIC("MC0020", new MC0020(), ICType.ZISO);
            internalRegisterIC("MC0111", new MC1111(), ICType.ZISO);
            internalRegisterIC("MC0230", new MC1230(), ICType.ZISO);
            internalRegisterIC("MC0420", new MC1420(), ICType.ZISO);
            internalRegisterIC("MC0500", new MC1500(), ICType.ZISO);
            internalRegisterIC("MC0260", new MC1260(false), ICType.ZISO);
            internalRegisterIC("MC0261", new MC1261(false), ICType.ZISO);
            internalRegisterIC("MC0262", new MC1262(false), ICType.ZISO);
            // CBX self-triggred
            internalRegisterIC("MCO116", new MCM116(), ICType.ZISO);
            internalRegisterIC("MCZ027", new MCX027(), ICType.ZISO);
            internalRegisterIC("MCZ116", new MCX116(), ICType.ZISO);
            internalRegisterIC("MCZ117", new MCX117(), ICType.ZISO);
            internalRegisterIC("MCZ118", new MCX118(), ICType.ZISO);
            internalRegisterIC("MCZ119", new MCX119(), ICType.ZISO);
            internalRegisterIC("MCZ120", new MCX120(), ICType.ZISO);
            internalRegisterIC("MCZ121", new MCX121(), ICType.ZISO);
            internalRegisterIC("MCZ130", new MCX130(), ICType.ZISO);
            internalRegisterIC("MCZ133", new MCX133(), ICType.ZISO);
            internalRegisterIC("MCZ203", new MCX203(), ICType.ZISO);
            internalRegisterIC("MCZ205", new MCX205(), ICType.ZISO);
            internalRegisterIC("MCZ216", new MCX216(), ICType.ZISO);
            internalRegisterIC("MCZ230", new MCX230(), ICType.ZISO);
            internalRegisterIC("MCZ231", new MCX231(), ICType.ZISO);
            internalRegisterIC("MCZ236", new MCX236(), ICType.ZISO);
            internalRegisterIC("MCZ238", new MCX238(), ICType.ZISO);
            internalRegisterIC("MCZ295", new MCX295(), ICType.ZISO);
        }
        // CB SISO
        internalRegisterIC("MC1000", new MC1000(), ICType.SISO);
        internalRegisterIC("MC1001", new MC1001(), ICType.SISO);
        internalRegisterIC("MC1017", new MC1017(), ICType.SISO);
        internalRegisterIC("MC1018", new MC1018(), ICType.SISO);
        internalRegisterIC("MC1020", new MC1020(), ICType.SISO);
        internalRegisterIC("MC1025", new MC1025(), ICType.SISO);
        internalRegisterIC("MC1110", new MC1110(), ICType.SISO);
        internalRegisterIC("MC1111", new MC1111(), ICType.SISO);
        internalRegisterIC("MC1200", new MC1200(), ICType.SISO);
        internalRegisterIC("MC1201", new MC1201(), ICType.SISO);
        internalRegisterIC("MC1202", new MC1202(), ICType.SISO);
        internalRegisterIC("MC1205", new MC1205(), ICType.SISO);
        internalRegisterIC("MC1206", new MC1206(), ICType.SISO);
        internalRegisterIC("MC1207", new MC1207(), ICType.SISO);
        internalRegisterIC("MC1230", new MC1230(), ICType.SISO);
        internalRegisterIC("MC1231", new MC1231(), ICType.SISO);
        internalRegisterIC("MC1240", new MC1240(), ICType.SISO);
        internalRegisterIC("MC1241", new MC1241(), ICType.SISO);
        internalRegisterIC("MC1250", new MC1250(), ICType.SISO);
        internalRegisterIC("MC1260", new MC1260(true), ICType.SISO);
        internalRegisterIC("MC1261", new MC1261(true), ICType.SISO);
        internalRegisterIC("MC1262", new MC1262(true), ICType.SISO);
        internalRegisterIC("MC1420", new MC1420(), ICType.SISO);
        internalRegisterIC("MC1500", new MC1500(), ICType.SISO);
        internalRegisterIC("MC1510", new MC1510(), ICType.SISO);
        internalRegisterIC("MC1511", new MC1511(), ICType.SISO);
        internalRegisterIC("MC1512", new MC1512(), ICType.SISO);
        // CB SI3O
        internalRegisterIC("MC2020", new MC2020(), ICType.SI3O);
        internalRegisterIC("MC2999", new MC2999(), ICType.SI3O);
        // CB _3ISO
        internalRegisterIC("MC3020", new MC3020(), ICType._3ISO);
        internalRegisterIC("MC3002", new MC3002(), ICType._3ISO);
        internalRegisterIC("MC3003", new MC3003(), ICType._3ISO);
        internalRegisterIC("MC3021", new MC3021(), ICType._3ISO);
        internalRegisterIC("MC3030", new MC3030(), ICType._3ISO);
        internalRegisterIC("MC3031", new MC3031(), ICType._3ISO);
        internalRegisterIC("MC3032", new MC3032(), ICType._3ISO);
        internalRegisterIC("MC3033", new MC3033(), ICType._3ISO);
        internalRegisterIC("MC3034", new MC3034(), ICType._3ISO);
        internalRegisterIC("MC3036", new MC3036(), ICType._3ISO);
        internalRegisterIC("MC3040", new MC3040(), ICType._3ISO);
        internalRegisterIC("MC3101", new MC3101(), ICType._3ISO);
        internalRegisterIC("MC3231", new MC3231(), ICType._3ISO);
        internalRegisterIC("MC3456", new MC3456(), ICType._3ISO);
        internalRegisterIC("MC4000", new MC4000(), ICType._3I3O);
        internalRegisterIC("MC4010", new MC4010(), ICType._3I3O);
        internalRegisterIC("MC4100", new MC4100(), ICType._3I3O);
        internalRegisterIC("MC4110", new MC4110(), ICType._3I3O);
        internalRegisterIC("MC4200", new MC4200(), ICType._3I3O);
        // perlstone
        internalRegisterPLC("MC5000", "perlstone_v1.0", ICType.VIVO);
        internalRegisterPLC("MC5001", "perlstone_v1.0", ICType._3I3O);
        // perlstone32
        internalRegisterPLC("MC5032", "perlstone32_v1", ICType.VIVO);
        internalRegisterPLC("MC5033", "perlstone32_v1", ICType._3I3O);
        // CBX SISO
        internalRegisterIC("MCX010", new MCX010(), ICType.SISO);
        internalRegisterIC("MCX027", new MCX027(), ICType.SISO);
        internalRegisterIC("MCX111", new MCX111(), ICType.SISO);
        internalRegisterIC("MCM112", new MCM112(), ICType.SISO);
        internalRegisterIC("MCX112", new MCX112(), ICType.SISO);
        internalRegisterIC("MCX114", new MCX114(), ICType.SISO);
        internalRegisterIC("MCX115", new MCX115(), ICType.SISO);
        internalRegisterIC("MCX116", new MCX116(), ICType.SISO);
        internalRegisterIC("MCX117", new MCX117(), ICType.SISO);
        internalRegisterIC("MCX118", new MCX118(), ICType.SISO);
        internalRegisterIC("MCX119", new MCX119(), ICType.SISO);
        internalRegisterIC("MCX120", new MCX120(), ICType.SISO);
        internalRegisterIC("MCX121", new MCX121(), ICType.SISO);
        internalRegisterIC("MCX130", new MCX130(), ICType.SISO);
        internalRegisterIC("MCX131", new MCX131(), ICType.SISO);
        internalRegisterIC("MCX132", new MCX132(), ICType.SISO);
        internalRegisterIC("MCX133", new MCX133(), ICType.SISO);
        internalRegisterIC("MCX140", new MCX140(), ICType.SISO);
        internalRegisterIC("MCX142", new MCX142(), ICType.SISO);
        internalRegisterIC("MCX144", new MCX144(), ICType.SISO);
        internalRegisterIC("MCX146", new MCX146(), ICType.SISO);
        internalRegisterIC("MCX200", new MCX200(), ICType.SISO);
        internalRegisterIC("MCX201", new MCX201(), ICType.SISO);
        internalRegisterIC("MCX202", new MCX202(), ICType.SISO);
        internalRegisterIC("MCX203", new MCX203(), ICType.SISO);
        internalRegisterIC("MCX204", new MCX204(), ICType.SISO);
        internalRegisterIC("MCX205", new MCX205(), ICType.SISO);
        internalRegisterIC("MCX206", new MCX206(), ICType.SISO);
        internalRegisterIC("MCX207", new MCX207(), ICType.SISO);
        internalRegisterIC("MCX208", new MCX208(), ICType.SISO);
        internalRegisterIC("MCX209", new MCX209(), ICType.SISO);
        internalRegisterIC("MCX210", new MCX210(), ICType.SISO);
        internalRegisterIC("MCX216", new MCX216(), ICType.SISO);
        internalRegisterIC("MCX228", new MCX228(), ICType.SISO);
        internalRegisterIC("MCX230", new MCX230(), ICType.SISO);
        internalRegisterIC("MCX231", new MCX231(), ICType.SISO);
        internalRegisterIC("MCX233", new MCX233(), ICType.SISO);
        internalRegisterIC("MCX235", new MCX235(), ICType.SISO);
        internalRegisterIC("MCX236", new MCX236(), ICType.SISO);
        internalRegisterIC("MCX237", new MCX237(), ICType.SISO);
        internalRegisterIC("MCX238", new MCX238(), ICType.SISO);
        internalRegisterIC("MCX242", new MCX242(), ICType.SISO);
        internalRegisterIC("MCX243", new MCX243(), ICType.SISO);
        internalRegisterIC("MCX244", new MCX244(), ICType.SISO);
        internalRegisterIC("MCX245", new MCX245(), ICType.SISO);
        internalRegisterIC("MCX246", new MCX246(), ICType.SISO);
        internalRegisterIC("MCX250", new MCX250(), ICType.SISO);
        internalRegisterIC("MCX251", new MCX251(), ICType.SISO);
        internalRegisterIC("MCX255", new MCX255(), ICType.SISO);
        internalRegisterIC("MCX256", new MCX256(), ICType.SISO);
        internalRegisterIC("MCX292", new MCX292(), ICType.SISO);
        internalRegisterIC("MCX293", new MCX293(), ICType.SISO);
        internalRegisterIC("MCX295", new MCX295(), ICType.SISO);
        internalRegisterIC("MCX512", new MCX512(), ICType.SISO);
        internalRegisterIC("MCX513", new MCX513(), ICType.SISO);
        internalRegisterIC("MCX515", new MCX515(), ICType.SISO);
        internalRegisterIC("MCX516", new MCX516(), ICType.SISO);
        internalRegisterIC("MCX517", new MCX517(), ICType.SISO);
        // CBX _3ISO
        internalRegisterIC("MCT233", new MCT233(), ICType._3ISO);
        internalRegisterIC("MCT246", new MCT246(), ICType._3ISO);
        // CBX UISO
        internalRegisterIC("MCU113", new MCX113(), ICType.UISO);
        internalRegisterIC("MCU131", new MCU131(), ICType.UISO);
        internalRegisterIC("MCU132", new MCU132(), ICType.UISO);
        internalRegisterIC("MCU140", new MCX140(), ICType.UISO);
        internalRegisterIC("MCU142", new MCX142(), ICType.UISO);
        internalRegisterIC("MCU144", new MCX144(), ICType.UISO);
        internalRegisterIC("MCU146", new MCX146(), ICType.UISO);
        internalRegisterIC("MCU200", new MCX200(), ICType.UISO);
        internalRegisterIC("MCU220", new MCX220(), ICType.UISO);
        internalRegisterIC("MCU221", new MCX221(), ICType.UISO);
        internalRegisterIC("MCU222", new MCX222(), ICType.UISO);
        internalRegisterIC("MCU292", new MCX292(), ICType.UISO);
        internalRegisterIC("MCU293", new MCX293(), ICType.UISO);
        internalRegisterIC("MCU300", new MCX300(), ICType.UISO);
        internalRegisterIC("MCU301", new MCX301(), ICType.UISO);
        internalRegisterIC("MCU302", new MCX302(), ICType.UISO);
        internalRegisterIC("MCU303", new MCX303(), ICType.UISO);
        internalRegisterIC("MCU304", new MCX304(), ICType.UISO);
        internalRegisterIC("MCU440", new MCX440(), ICType.UISO);
        internalRegisterIC("MCU700", new MCX700(), ICType.UISO);
        internalRegisterIC("MCU705", new MCX705(), ICType.UISO);
        // CBX MISO
        internalRegisterIC("MCU211", new MCX211(), ICType.MISO);
        internalRegisterIC("MCU212", new MCX212(), ICType.MISO);
        internalRegisterIC("MCU213", new MCX213(), ICType.MISO);
        internalRegisterIC("MCU214", new MCX214(), ICType.MISO);
        internalRegisterIC("MCU217", new MCX217(), ICType.MISO);
        internalRegisterIC("MCU701", new MCX701(), ICType.MISO);
        internalRegisterIC("MCU702", new MCX702(), ICType.MISO);
    }
    



    /**
     * Checks if the player can create an IC.
     * 
     * @param player
     * @param id
     * @param ic
     */
    private boolean canCreateIC(Player player, String id, RegisteredIC ic) {
        return (!ic.ic.requiresPermission()
                && !(ic.isPlc && redstonePLCsRequirePermission)
                && !(ic.type.isSelfTriggered && restrictSelfTriggeredICs))
                || player.canUseCommand("/allic")
                || player.canUseCommand("/" + id.toLowerCase());
    }

    
    

    /**
     * Get an IC type from its type name.
     * 
     * @param type
     * @return
     * @throws CustomICException thrown if the type does not exist
     */
    private ICType getICType(String type) throws CustomICException {
        ICType typeObject = ICType.forName(type);
        
        if (typeObject == null) {
            throw new CustomICException("Invalid IC type: " + type);
        }
        
        return typeObject;
    }

    /**
     * Registers an non-PLC IC.
     * 
     * @param name
     * @param ic
     * @param type
     */
    private void internalRegisterIC(String name, IC ic, ICType type) {
        if (!icList.containsKey(name)) {
            registerIC(name, ic, type, false);
        }
    }

    /**
     * Registers a PLC
     * 
     * @param name
     * @param ic
     * @param type
     * @param isPlc
     */
    private void internalRegisterPLC(String name, String plclang, ICType type) {
        if (!icList.containsKey(name)) {
            registerIC(name, new DefaultPLC(plcLanguageList.get(plclang)), type, true);
        }
    }

    
    
    private int[][] getIOOrder(String options)
    {
    	int[] abc = null;
    	int[] def = null;
    	int[][] output = new int[][]{null, null};
		
		switch(options.length())
		{
			case 3:
				abc = getOrder('a', 'b', 'c', options);
				if(abc == null)
					def = getOrder('d', 'e', 'f', options);
				break;
			case 6:
				abc = getOrder('a', 'b', 'c', options.substring(0, 3));
				def = getOrder('d', 'e', 'f', options.substring(3));
				break;
		}
		
		output[0] = abc;
		output[1] = def;
		
    	return output;
    }
    
    
    private int[] getOrder(char a, char b, char c, String input)
    {
    	int[] output = new int[]{-1, -1, -1};
    	
    	for(int i = 0; i < 3; i++)
    	{
    		if(input.charAt(i) == ' ')
    			return null;
    		else if(input.charAt(i) == a)
    		{
    			output[i] = 0;
    			a = ' ';
    		}
    		else if(input.charAt(i) == b)
    		{
    			output[i] = 1;
    			b = ' ';
    		}
    		else if(input.charAt(i) == c)
    		{
    			output[i] = 2;
    			c = ' ';
    		}
    		else
    			return null;
    	}
    	
    	return output;
    }
    
    

    // --------------------
    // private classes
    // --------------------    
    
    /**
     * Storage class for registered ICs.
     */
    public static class RegisteredIC {
        final ICType type;
        final IC ic;
        final boolean isPlc;

        /**
         * Construct the object.
         * 
         * @param ic
         * @param type
         * @param isPlc
         */
        public RegisteredIC(IC ic, ICType type, boolean isPlc) {
            this.type = type;
            this.ic = ic;
            this.isPlc = isPlc;
        }

        /**
         * Think.
         * 
         * @param pt
         * @param changedRedstoneInput
         * @param signText
         * @param sign
         * @param r
         */
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
            type.think(cbworld, pt, changedRedstoneInput, signText, sign, ic, r, mode, orderIn, orderOut, extra);
        }

        /**
         * Think.
         * 
         * @param pt
         * @param changedRedstoneInput
         * @param signText
         * @param sign
         */
        void think(CraftBookWorld cbworld, Vector pt, SignText signText, Sign sign, Object extra) {
            type.think(cbworld, pt, signText, sign, ic, extra);
        }
        
    }
   
}
