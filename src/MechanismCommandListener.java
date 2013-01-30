import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.HistoryHashMap;
import com.sk89q.craftbook.InsufficientArgumentsException;
import com.sk89q.craftbook.Vector;

public class MechanismCommandListener extends CraftBookDelegateListener{
	// properties
    private int maxToggleAreaSize;
    private int maxUserToggleAreas;
    private boolean usePageWriter = false;
    private int pageMaxCharacters = 400;
    private int maxPages = 20;
    /**
     * Tracks copy saves to prevent flooding.
     */
    private Map<String,Long> lastCopySave =
            new HashMap<String,Long>();
    
    
	public MechanismCommandListener(CraftBook craftBook, CraftBookListener listener) {
		super(craftBook, listener);
	}


	@Override
	public void loadConfiguration() {
        maxToggleAreaSize = Math.max(0, properties.getInt("toggle-area-max-size", 5000));
        maxUserToggleAreas = Math.max(0, properties.getInt("toggle-area-max-per-user", 30));
        if(properties.containsKey("pagewriter-enable"))
        	usePageWriter = properties.getBoolean("pagewriter-enable", false);
        if(properties.containsKey("page-max-characters"))
        	pageMaxCharacters = properties.getInt("page-max-characters", 400);
        if(properties.containsKey("max-pages"))
        	maxPages = properties.getInt("max-pages", 20);
	}

    /**
    *
    * @param player
    */
   @Override
   public void onDisconnect(Player player) {
       lastCopySave.remove(player.getName());
   }

   /**
	 * Called when a command is run
	 *
	 * @param player
	 * @param split
	 * @return whether the command was processed
	 */
	@Override
	public boolean onCheckedCommand(Player player, String[] split)
	        throws InsufficientArgumentsException, LocalWorldEditBridgeException {
	    // savearea 
		if (isAllowedCommand_savearea(player, split)) {
			return onCommand_savearea(player, split);
		}
	    // sit 
		if (isAllowedCommand_sit(player, split)) {
			return onCommand_sit(player, split);
		}
	    // stand
		if (isAllowedCommand_stand(player, split)) {
			return onCommand_stand(player, split);
		}
	    // cbwarp
		if (isAllowedCommand_cbwarp(player, split)) {
			return onCommand_cbwarp(player, split);
		}
	    // cbwarpx
		if (isAllowedCommand_cbwarpx(player, split)) {
			return onCommand_cbwarpx(player, split);
		}
	    // cbwarppreload
		if (isAllowedCommand_cbwarppreload(player, split)) {
			return onCommand_cbwarppreload(player, split);
		}
	    // cbmusic
		if (isAllowedCommand_cbmusic(player, split)) {
			return onCommand_cbmusic(player, split);
		}		if (isAllowedCommand_cbpage(player, split)) {
			return onCommand_cbpage(player, split);
		}
	    // cbpage
		if (isAllowedCommand_cbpage(player, split)) {
			return onCommand_cbpage(player, split);
		}
		// admincbpage -----------------
		if (isAllowedCommand_admincbpage(player, split)) {
			return onCommand_admincbpage(player, split);
		}
		// reloadcbenchantrecipes
		if (isAllowedCommand_reloadcbenchantrecipes(player, split)) {
			return onCommand_reloadcbenchantrecipes(player, split);
		}
		// command is not handled here
	    return false;
	}
	
	
	private boolean isAllowedCommand_savearea(Player player, String[] split) {
		return (split[0].equalsIgnoreCase("/savearea") 
				&& Util.canUse(player,"/savearea"))
				||
				(split[0].equalsIgnoreCase("/savensarea") 
				&& Util.canUse(player, "/savensarea"));
	}

	private boolean onCommand_savearea(Player player, String[] split)
			throws LocalWorldEditBridgeException,
			InsufficientArgumentsException {
		boolean namespaced = split[0].equalsIgnoreCase("/savensarea");
		Util.checkArgs(split, namespaced ? 2 : 1, -1, split[0]);
		String id;
		String namespace;

		if (namespaced) {
			id = Util.joinString(split, " ", 2);
			namespace = split[1];

			if (namespace.equalsIgnoreCase("@")) {
				namespace = "global";
			} else {
				if (!CopyManager.isValidNamespace(namespace)) {
					player.sendMessage(Colors.Rose
							+ "Invalid namespace name. For the global namespace, use @");
					return true;
				}
				namespace = "~" + namespace;
			}
		} else {
			id = Util.joinString(split, " ", 1);
			String nameNamespace = player.getName();

			// Sign lines can only be 15 characters long while names
			// can be up to 16 characters long
			if (nameNamespace.length() > 15) {
				nameNamespace = nameNamespace.substring(0, 15);
			}

			if (!CopyManager.isValidNamespace(nameNamespace)) {
				player.sendMessage(Colors.Rose
						+ "You have an invalid player name.");
				return true;
			}

			namespace = "~" + nameNamespace;
		}

		if (!CopyManager.isValidName(id)) {
			player.sendMessage(Colors.Rose + "Invalid area name.");
			return true;
		}

		try {
			Vector min = LocalWorldEditBridge.getRegionMinimumPoint(player);
			Vector max = LocalWorldEditBridge.getRegionMaximumPoint(player);
			Vector size = max.subtract(min).add(1, 1, 1);

			// [TODO]: use world from WorldEdit bridge instead if WorldEdit ever
			// gets support
			// Can have potential exploits without it!
			CraftBookWorld cbworld = CraftBook.getCBWorld(player.getWorld());

			// Check maximum size
			if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > maxToggleAreaSize) {
				player.sendMessage(Colors.Rose + "Area is larger than allowed "
						+ maxToggleAreaSize + " blocks.");
				return true;
			}

			// Check to make sure that a user doesn't have too many toggle
			// areas (to prevent flooding the server with files)
			if (maxUserToggleAreas >= 0 && !namespace.equals("global")) {
				int count = listener.getCopyManager().meetsQuota(cbworld,
						namespace, id, maxUserToggleAreas);

				if (count > -1) {
					player.sendMessage(Colors.Rose + "You are limited to "
							+ maxUserToggleAreas + " toggle area(s). You have "
							+ count + " areas.");
					return true;
				}
			}

			// Prevent save flooding
			Long lastSave = lastCopySave.get(player.getName());
			long now = System.currentTimeMillis();

			if (lastSave != null) {
				if (now - lastSave < 1000 * 3) {
					player.sendMessage(Colors.Rose
							+ "Please wait before saving again.");
					return true;
				}
			}

			lastCopySave.put(player.getName(), now);

			// Copy
			CuboidCopy copy = new CuboidCopy(cbworld, min, size);
			copy.copy();

			logger.info(player.getName() + " saving toggle area with folder '"
					+ namespace + "' and ID '" + id + "'.");

			// Save
			try {
				listener.getCopyManager().save(namespace, id, copy);
				if (namespaced) {
					player.sendMessage(Colors.Gold + "Area saved as '" + id
							+ "' under the specified namespace.");
				} else {
					player.sendMessage(Colors.Gold + "Area saved as '" + id
							+ "' under your player.");
				}
			} catch (IOException e) {
				player.sendMessage(Colors.Rose + "Could not save area: "
						+ e.getMessage());
			}
		} catch (NoClassDefFoundError e) {
			player.sendMessage(Colors.Rose
					+ "WorldEdit.jar does not exist in plugins/.");
		}
		return true;
	}

	private boolean isAllowedCommand_sit(Player player, String[] split) {
		return Sitting.enabled && split[0].equalsIgnoreCase("/sit")
				&& player.canUseCommand("/sit");
	}

	private boolean onCommand_sit(Player player, String[] split) {
		BaseEntity ridingEntity = player.getRidingEntity();
		if (ridingEntity != null) {
			Sitting.stand(player, 0,
					UtilEntity.getMountedYOffset(ridingEntity.getEntity()), 0);
		} else {
			SitType[] types = new SitType[1];
			switch (Sitting.healWhileSitting) {
			case ALL:
			case SITCOMMANDONLY:
				types[0] = SittingType.SIT_HEAL.getType();
				break;
			default:
				types[0] = null;
			}
			Sitting.sit(player, types, player.getWorld(), player.getX(),
					player.getY(), player.getZ(), player.getRotation(), -0.05D,
					0.0D);
		}
		return true;
	}

	private boolean isAllowedCommand_stand(Player player, String[] split) {
		return Sitting.enabled && split[0].equalsIgnoreCase("/stand")
				&& player.canUseCommand("/stand");
	}

	private boolean onCommand_stand(Player player, String[] split) {
		BaseEntity ridingEntity = player.getRidingEntity();
		if (ridingEntity == null
				|| !(ridingEntity.getEntity() instanceof EntitySitting)) {
			return true;
		}
		Sitting.stand(player, 0,
				((EntitySitting) ridingEntity.getEntity()).getOffsetY(), 0);
		return true;
	}

	private boolean isAllowedCommand_cbwarp(Player player, String[] split) {
		return split[0].equalsIgnoreCase("/cbwarp")
				&& player.canUseCommand("/cbwarp");
	}

	private boolean onCommand_cbwarp(Player player, String[] split) {
		if (split.length < 2
				|| (split.length == 2 && split[1].matches("[0-9]+"))) {
			int set;
			if (split.length < 2)
				set = 1;
			else {
				try {
					set = Integer.parseInt(split[1]);
				} catch (NumberFormatException e) {
					// shouldn't reach here, but just incase
					player.sendMessage(Colors.Rose
							+ "Invalid CBWarp page number");
					return true;
				}
			}

			String[] output = CBWarp.listWarps(set, false);

			for (String line : output) {
				if (line == null)
					break;
				player.sendMessage(line);
			}
		} else {
			if (split.length == 2) {
				CBWarp.WarpError error = CBWarp.warp(player, split[1], null);
				if (error != null) {
					player.sendMessage(error.MESSAGE);
				}
			} else {
				if (split[1].equalsIgnoreCase("set")
						|| split[1].equalsIgnoreCase("add")) {
					if (!player.canUseCommand("/cbwarpadd")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to add warps.");
						return true;
					}

					String title = "";
					if (split.length > 3)
						title = Util.joinString(split, " ", 3);
					CBWarp.WarpError error = CBWarp.addWarp(player, split[2],
							Util.locationToWorldLocation(
									CraftBook.getCBWorld(player.getWorld()),
									player.getLocation()), title, null);

					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "warp added");
					}
				} else if (split[1].equalsIgnoreCase("remove")
						|| split[1].equalsIgnoreCase("rm")
						|| split[1].equalsIgnoreCase("delete")
						|| split[1].equalsIgnoreCase("clear")) {
					if (!player.canUseCommand("/cbwarpremove")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to remove warps.");
						return true;
					}

					CBWarp.WarpError error = CBWarp.removeWarp(player,
							split[2], null);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "warp removed");
					}
				} else if (split[1].equalsIgnoreCase("title")
						|| split[1].equalsIgnoreCase("settitle")
						|| split[1].equalsIgnoreCase("info")
						|| split[1].equalsIgnoreCase("setinfo")
						|| split[1].equalsIgnoreCase("description")) {
					if (!player.canUseCommand("/cbwarpeditinfo")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to change warp titles.");
						return true;
					}

					String title = "";
					if (split.length > 3)
						title = Util.joinString(split, " ", 3);
					CBWarp.WarpError error = CBWarp.setTitle(player, split[2],
							title, null);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "title changed");
					}
				} else if (split[1].equalsIgnoreCase("message")
						|| split[1].equalsIgnoreCase("setmessage")
						|| split[1].equalsIgnoreCase("msg")
						|| split[1].equalsIgnoreCase("setmsg")) {
					if (!player.canUseCommand("/cbwarpeditinfo")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to change warp messages.");
						return true;
					}

					String message = "";
					if (split.length > 3)
						message = Util.joinString(split, " ", 3);
					CBWarp.WarpError error = CBWarp.setMessage(player,
							split[2], message, null);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "message changed");
					}
				}
			}
		}
		return true;
	}

	private boolean isAllowedCommand_cbwarpx(Player player, String[] split) {
		return split[0].equalsIgnoreCase("/cbwarpx")
				&& player.canUseCommand("/cbwarpx");
	}

	private boolean onCommand_cbwarpx(Player player, String[] split) {
		if (split.length < 3
				|| (split.length == 2 && split[1].matches("[0-9]+"))) {
			int set;
			if (split.length < 2)
				set = 1;
			else {
				try {
					set = Integer.parseInt(split[1]);
				} catch (NumberFormatException e) {
					// shouldn't reach here, but just incase
					player.sendMessage(Colors.Rose
							+ "Invalid CBWarp page number");
					return true;
				}
			}

			String[] output = CBWarp.listWarps(set, true);

			for (String line : output) {
				if (line == null)
					break;
				player.sendMessage(line);
			}
		} else {
			if (split.length == 3) {
				CBWarp.WarpError error = CBWarp
						.warp(player, split[1], split[2]);
				if (error != null) {
					player.sendMessage(error.MESSAGE);
				}
			} else {
				if (split[1].equalsIgnoreCase("set")
						|| split[1].equalsIgnoreCase("add")) {
					if (!player.canUseCommand("/cbwarpxadd")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to add warps.");
						return true;
					}

					String title = "";
					if (split.length > 4)
						title = Util.joinString(split, " ", 4);
					CBWarp.WarpError error = CBWarp.addWarp(player, split[2],
							Util.locationToWorldLocation(
									CraftBook.getCBWorld(player.getWorld()),
									player.getLocation()), title, split[3]);

					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "warp added");
					}
				} else if (split[1].equalsIgnoreCase("remove")
						|| split[1].equalsIgnoreCase("rm")
						|| split[1].equalsIgnoreCase("delete")
						|| split[1].equalsIgnoreCase("clear")) {
					if (!player.canUseCommand("/cbwarpxremove")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to remove warps.");
						return true;
					}

					CBWarp.WarpError error = CBWarp.removeWarp(player,
							split[2], split[3]);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "warp removed");
					}
				} else if (split[1].equalsIgnoreCase("title")
						|| split[1].equalsIgnoreCase("settitle")
						|| split[1].equalsIgnoreCase("info")
						|| split[1].equalsIgnoreCase("setinfo")
						|| split[1].equalsIgnoreCase("description")) {
					if (!player.canUseCommand("/cbwarpxeditinfo")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to change warp titles.");
						return true;
					}

					String title = "";
					if (split.length > 4)
						title = Util.joinString(split, " ", 4);
					CBWarp.WarpError error = CBWarp.setTitle(player, split[2],
							title, split[3]);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "title changed");
					}
				} else if (split[1].equalsIgnoreCase("message")
						|| split[1].equalsIgnoreCase("setmessage")
						|| split[1].equalsIgnoreCase("msg")
						|| split[1].equalsIgnoreCase("setmsg")) {
					if (!player.canUseCommand("/cbwarpxeditinfo")) {
						player.sendMessage(Colors.Rose
								+ "you do not have permissions to change warp messages.");
						return true;
					}

					String message = "";
					if (split.length > 4)
						message = Util.joinString(split, " ", 4);
					CBWarp.WarpError error = CBWarp.setMessage(player,
							split[2], message, split[3]);
					if (error != null) {
						player.sendMessage(error.MESSAGE);
					} else {
						player.sendMessage(Colors.Gold + "message changed");
					}
				}
			}
		}
		return true;

	}

	private boolean isAllowedCommand_cbwarppreload(Player player, String[] split) {
		return split[0].equalsIgnoreCase("/cbwarpreload")
				&& player.canUseCommand("/cbwarpreload");
	}

	private boolean onCommand_cbwarppreload(Player player, String[] split) {
		CBWarp.WarpError error = CBWarp.reload();
		if (error != null) {
			player.sendMessage(error.MESSAGE);
		} else {
			player.sendMessage(Colors.Gold + "cbwarp reloaded");
		}
		return true;
	}

	private boolean isAllowedCommand_cbmusic(Player player, String[] split) {
		return split[0].equalsIgnoreCase("/cbmusic")
				&& player.canUseCommand("/cbmusic");
	}

	private boolean onCommand_cbmusic(Player player, String[] split) {
		if (split.length > 1) {
			if (split[1].equalsIgnoreCase("stopall")
					|| split[1].equalsIgnoreCase("stop")) {
				for (MusicPlayer mplayer : MCX700.music.values()) {
					mplayer.turnOff();
				}
	
				for (MusicPlayer mplayer : MCX701.music.values()) {
					mplayer.turnOff();
				}
	
				for (MusicPlayer mplayer : MCX705.music.values()) {
					mplayer.turnOff();
				}
	
				player.sendMessage(Colors.Gold + "All music ICs stopped.");
			} else if (split[1].equalsIgnoreCase("stoploop")
					|| split[1].equalsIgnoreCase("stoploops")
					|| split[1].equalsIgnoreCase("stoprepeat")
					|| split[1].equalsIgnoreCase("stoprepeats")) {
				for (MusicPlayer mplayer : MCX700.music.values()) {
					if (mplayer.loops())
						mplayer.turnOff();
				}
	
				for (MusicPlayer mplayer : MCX701.music.values()) {
					if (mplayer.loops())
						mplayer.turnOff();
				}
	
				for (MusicPlayer mplayer : MCX705.music.values()) {
					if (mplayer.loops())
						mplayer.turnOff();
				}
	
				player.sendMessage(Colors.Gold + "All looping music stopped.");
			} else if (split[1].equalsIgnoreCase("disable")) {
				if (MCX700.music != null) {
					MCX700.music.clear();
					MCX700.music = null;
				}
				if (MCX701.music != null) {
					MCX701.music.clear();
					MCX701.music = null;
				}
				if (MCX705.music != null) {
					MCX705.music.clear();
					MCX705.music = null;
				}
	
				player.sendMessage(Colors.Rose
						+ "Music DISABLED. To allow music again: "
						+ Colors.White + "/cbmusic enable");
			} else if (split[1].equalsIgnoreCase("enable")
					|| split[1].equalsIgnoreCase("restart")) {
				if (MCX700.music != null)
					MCX700.music.clear();
				else
					MCX700.music = new HistoryHashMap<String, MusicPlayer>(100);
	
				if (MCX701.music != null)
					MCX701.music.clear();
				else
					MCX701.music = new HistoryHashMap<String, MusicPlayer>(50);
	
				if (MCX705.music != null)
					MCX705.music.clear();
				else
					MCX705.music = new HistoryHashMap<String, MusicPlayer>(100);
	
				player.sendMessage(Colors.Gold + "Music restarted.");
			} else {
				player.sendMessage(Colors.Rose
						+ "Unknown /cbmusic Command. For Help type: "
						+ Colors.White + "/cbmusic");
			}
		} else {
			player.sendMessage(Colors.Gold
					+ "Usage: Stops, disables, or enables Music ICs");
			player.sendMessage(Colors.Gold + "  /cbmusic stopall"
					+ Colors.White + " -Stops all Music");
			player.sendMessage(Colors.Gold + "  /cbmusic stoploops"
					+ Colors.White + " -Stops all looping Music");
			player.sendMessage(Colors.Gold + "  /cbmusic disable"
					+ Colors.White + " -Disables Music");
			player.sendMessage(Colors.Gold + "  /cbmusic enable" + Colors.White
					+ " -Enables Music");
			player.sendMessage(Colors.Gold + "  /cbmusic restart"
					+ Colors.White + " -Restarts Music");
		}
		return true;
	}

	private boolean isAllowedCommand_cbpage(Player player, String[] split) {
		return usePageWriter && split[0].equalsIgnoreCase("/cbpage") && player.canUseCommand("/cbpage");
	}

	private boolean onCommand_cbpage(Player player, String[] split) {
		PageWriter.handleCommand(player, split, pageMaxCharacters, maxPages);
		return true;
	}

	private boolean isAllowedCommand_admincbpage(Player player, String[] split) {
		return usePageWriter && split[0].equalsIgnoreCase("/admincbpage")
				&& player.canUseCommand("/admincbpage");
	}

	private boolean onCommand_admincbpage(Player player, String[] split) {
		PageWriter.handleNSCommand(player, split, pageMaxCharacters, maxPages);
		return true;
	}

	private boolean isAllowedCommand_reloadcbenchantrecipes(Player player, String[] split) {
		return split[0].equalsIgnoreCase("/reloadcbenchantrecipes")
				&& player.canUseCommand("/reloadcbenchantrecipes");
	}

	private boolean onCommand_reloadcbenchantrecipes(Player player,	String[] split) {
		EnchantCraft.load();
		player.sendMessage(Colors.Gold + "CraftBook Enchantment recipes reloaded");
		return true;
	
	}

}