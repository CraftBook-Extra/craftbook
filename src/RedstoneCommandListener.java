import java.util.Map;
import java.util.logging.Level;


import com.sk89q.craftbook.ic.MCX120;
import com.sk89q.craftbook.ic.MCX121;

public class RedstoneCommandListener extends CraftBookDelegateListener {
    private boolean listICs = true;
	
	private RedstoneListener redstoneListener;

	public RedstoneCommandListener(CraftBook craftBook,
			CraftBookListener listener, RedstoneListener redstoneListener) {
		super(craftBook, listener);
		this.redstoneListener = redstoneListener;
	}

	@Override
	public void loadConfiguration() {
        listICs = properties.getBoolean("enable-ic-list",true);
	}

	   /**
		 * Called when a command is run
		 *
		 * @param player
		 * @param split
		 * @return whether the command was processed
		 */
		@Override
		public boolean onCheckedCommand(Player player, String[] split){
		    // mcx120
			if (isAllowedCommand_mcx120(player, split)) {
				return onCommand_mcx120(player, split);
			}
		    // mcx120list
			if (isAllowedCommand_mcx120list(player, split)) {
				return onCommand_mcx120list(player, split);
			}
		    // mcx121
			if (isAllowedCommand_mcx121(player, split)) {
				return onCommand_mcx121(player, split);
			}
		    // mcx121pass
			if (isAllowedCommand_mcx121pass(player, split)) {
				return onCommand_mcx121pass(player, split);
			}
		    // mcx121remove
			if (isAllowedCommand_mcx121remove(player, split)) {
				return onCommand_mcx121remove(player, split);
			}
		    // mcx121list
			if (isAllowedCommand_mcx121list(player, split)) {
				return onCommand_mcx121list(player, split);
			}
			// listICs
			if (isAllowedCommand_listICs(player, split)) {
				return onCommand_listICs(player, split);
			}
			// command is not handled here
			return false;
		}
	
		
		private boolean isAllowedCommand_mcx120(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx120")
					&& player.canUseCommand("/mcx120");
		}

		private boolean onCommand_mcx120(Player player, String[] split) {
			if (split.length < 2) {
				player.sendMessage(Colors.Gold
						+ "Usage: /mcx120 [band name] <on/off/state>");
				player.sendMessage(Colors.Rose
						+ "You must specify a band name after /mcx120.");
				player.sendMessage(Colors.Gold
						+ "Optional \"on\" or \"off\" or \"state\". Will toggle if left blank.");
			} else {
				Boolean out = MCX120.airwaves.get(split[1]);
				if (out == null) {
					player.sendMessage(Colors.Rose + "Could not find band name: "
							+ split[1]);
				} else {
					if (split.length > 2) {
						if (split[2].equalsIgnoreCase("on"))
							out = true;
						else if (split[2].equalsIgnoreCase("off"))
							out = false;
						else if (split[2].equalsIgnoreCase("state")) {
							String state = out ? "on" : "off";
							player.sendMessage(Colors.Gold + "Command IC "
									+ split[1] + " current state: " + Colors.White
									+ state);
							return true;
						} else {
							player.sendMessage(Colors.Rose
									+ "Unknown command option: " + split[2]);
							return true;
						}
					} else
						out = !out;
		
					MCX120.airwaves.put(split[1], out);
		
					String state = out ? "on" : "off";
					player.sendMessage(Colors.Gold + "Command IC turned: "
							+ Colors.White + state);
				}
			}
			return true;
		}

		private boolean isAllowedCommand_mcx120list(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx120list")
					&& player.canUseCommand("/mcx120list");
		}

		private boolean onCommand_mcx120list(Player player, String[] split) {
			StringBuilder out = new StringBuilder(128);
			for (Map.Entry<String, Boolean> entry : MCX120.airwaves.entrySet()) {
				String color;
				Boolean state = entry.getValue();
				if (state == null) {
					color = Colors.Gray;
				} else {
					color = state ? Colors.Green : Colors.Red;
				}
				out.append(" [")
					.append(color)
					.append(entry.getKey())
					.append(Colors.White + "]");
			}
			if (out.length() == 0) {
				player.sendMessage(Colors.Red + "No command ICs found.");
			} else {
				player.sendMessage(out.toString());
			}
			return true;
		}

		private boolean isAllowedCommand_mcx121(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx121")
					&& player.canUseCommand("/mcx121");
		}

		private boolean onCommand_mcx121(Player player, String[] split) {
			if (split.length < 3) {
				player.sendMessage(Colors.Gold
						+ "Usage: /mcx121 [band name] [password] <on/off/state>");
				player.sendMessage(Colors.Rose
						+ "You must specify a band name and password after /mcx121.");
				player.sendMessage(Colors.Gold
						+ "Optional \"on\" or \"off\" or \"state\". Will toggle if left blank.");
			} else {
				Boolean out = MCX121.airwaves.get(split[1]);
				if (out == null) {
					player.sendMessage(Colors.Rose + "Could not find band name: "
							+ split[1]);
				} else {
					Boolean ispass = MCX121Pass.isPassword(split[1], split[2]);
		
					if (ispass == null) {
						player.sendMessage(Colors.Rose + "Password file not found!");
					} else if (ispass) {
						if (split.length > 3) {
							if (split[3].equalsIgnoreCase("on"))
								out = true;
							else if (split[3].equalsIgnoreCase("off"))
								out = false;
							else if (split[3].equalsIgnoreCase("state")) {
								String state = out ? "on" : "off";
								player.sendMessage(Colors.Gold + "Command IC "
										+ split[1] + " current state: "
										+ Colors.White + state);
								return true;
							} else {
								player.sendMessage(Colors.Rose
										+ "Unknown command option: " + split[3]);
								return true;
							}
						} else
							out = !out;
		
						MCX121.airwaves.put(split[1], out);
		
						String state = out ? "on" : "off";
						player.sendMessage(Colors.Gold + "Command IC turned: "
								+ Colors.White + state);
					} else {
						player.sendMessage(Colors.Rose + "Incorrect password used!");
						logger.log(Level.INFO, player.getName()
								+ " used an incorrect mcx121 password.");
					}
				}
		
			}
		
			return true;
		}

		private boolean isAllowedCommand_mcx121pass(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx121pass")
					&& player.canUseCommand("/mcx121pass");
		}

		private boolean onCommand_mcx121pass(Player player, String[] split) {
			if (split.length < 3
					|| (split[1].equalsIgnoreCase("add") && split.length < 4)
					|| (split[1].equalsIgnoreCase("change") && split.length < 5)) {
				player.sendMessage(Colors.Gold
						+ "Usage: adding or changing passwords for [MCX121]");
				player.sendMessage(Colors.Gold
						+ "/mcx121pass add [band name] [password]");
				player.sendMessage(Colors.Gold
						+ "/mcx121pass change [band name] [current pass] [new pass]");
				player.sendMessage(Colors.Gold + "/mcx121pass has [band name]");
			} else {
				Boolean changed;
		
				if (split[1].equalsIgnoreCase("add")) {
					if (split[3].length() > 15 || split[3].length() < 3) {
						player.sendMessage(Colors.Rose
								+ "Passwords must be 3 to 15 characters long.");
						return true;
					}
		
					changed = MCX121Pass.setPassword(split[2], split[3]);
				} else if (split[1].equalsIgnoreCase("change")) {
					if (split[4].length() > 15 || split[4].length() < 3) {
						player.sendMessage(Colors.Rose
								+ "Passwords must be 3 to 15 characters long.");
						return true;
					}
		
					changed = MCX121Pass.setPassword(split[2], split[3], split[4]);
				} else if (split[1].equalsIgnoreCase("has")) {
					Boolean haspass = MCX121Pass.hasPassword(split[2]);
					if (haspass == null) {
						player.sendMessage(Colors.Rose
								+ "Could not find password file!");
					} else if (haspass) {
						player.sendMessage(Colors.Gold + "Password " + Colors.White
								+ split[2] + Colors.Gold + " exists");
					} else {
						player.sendMessage(Colors.Gold + "Password " + Colors.White
								+ split[2] + Colors.Gold + " does not exist");
					}
					return true;
				} else {
					player.sendMessage(Colors.Rose + "Unknown command option: "
							+ split[1]);
					return true;
				}
		
				if (changed == null) {
					player.sendMessage(Colors.Rose
							+ "Could not edit or create the password file!");
					logger.log(Level.INFO, player.getName()
							+ " attempted to set mcx121 pass, but password"
							+ " file could not be changed or edited.");
				} else if (changed) {
					player.sendMessage(Colors.Gold + "> Password Set");
					player.sendMessage(Colors.Rose + "NOTE: Passwords are "
							+ Colors.White + "NOT" + Colors.Rose
							+ " encrypted! Admins " + Colors.White + "CAN"
							+ Colors.Rose + " read them!");
		
					// logger.log(Level.INFO, player.getName()+" set mcx121 pass");
				} else {
					if (split[1].equalsIgnoreCase("change")) {
						player.sendMessage(Colors.Rose
								+ "Failed to set password. Incorrect password entered.");
					} else {
						player.sendMessage(Colors.Rose
								+ "Failed to set password. Password exists or invalid band name");
						player.sendMessage(Colors.Rose
								+ "Change existing passwords with:");
						player.sendMessage(Colors.Rose
								+ "/mcx121pass change [name] [current pass] [new pass]");
					}
		
					logger.log(
							Level.INFO,
							player.getName()
									+ " attempted to set a mcx121 password, but failed. Bad password?");
				}
			}
		
			return true;
		
		}

		private boolean isAllowedCommand_mcx121remove(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx121remove")
					&& player.canUseCommand("/mcx121remove");
		}

		private boolean onCommand_mcx121remove(Player player, String[] split) {
		
			if (split.length < 2) {
				player.sendMessage(Colors.Gold + "Usage: /mcx121remove [band name]");
			} else {
				Boolean removed = MCX121Pass.setPassword(split[1], "0", "", true);
		
				if (removed == null) {
					player.sendMessage(Colors.Rose
							+ "Could not edit or create the password file!");
					logger.log(
							Level.INFO,
							player.getName()
									+ " attempted to remove mcx121 password, but could not "
									+ "edit or create the password file.");
				} else if (removed) {
					player.sendMessage(Colors.Gold + "Password " + Colors.White
							+ split[1] + Colors.Gold + " removed!");
					logger.log(Level.INFO, player.getName()
							+ " removed mcx121 password");
				} else {
					player.sendMessage(Colors.Rose + "Failed to remove password: "
							+ split[1]);
					logger.log(Level.INFO, player.getName()
							+ " failed to remove mcx121 password");
				}
			}
		
			return true;
		
		}

		private boolean isAllowedCommand_mcx121list(Player player, String[] split) {
			return split[0].equalsIgnoreCase("/mcx121list")
					&& player.canUseCommand("/mcx121list");
		}

		private boolean onCommand_mcx121list(Player player, String[] split) {
			String out = "";
			for (Map.Entry<String, Boolean> entry : MCX121.airwaves.entrySet()) {
				String color;
				Boolean state = entry.getValue();
		
				if (state == null)
					color = Colors.Gray;
				else
					color = state ? Colors.Green : Colors.Red;
		
				out += " [" + color + entry.getKey() + Colors.White + "]";
			}
			if (out.length() == 0)
				player.sendMessage(Colors.Red + "No command ICs found.");
			else {
				player.sendMessage(out);
			}
			return true;
		}

		private boolean isAllowedCommand_listICs(Player player, String[] split) {
			return listICs && split[0].equalsIgnoreCase("/listics")
		            && Util.canUse(player, "/listics");
		}

		private boolean onCommand_listICs(Player player, String[] split) {
		        String[] lines = redstoneListener.generateICText(player);
		        int pages = ((lines.length - 1) / 10) + 1;
		        int accessedPage;
		        
		        try {
		            accessedPage = split.length == 1 ? 0 : Integer
		                    .parseInt(split[1]) - 1;
		            if (accessedPage < 0 || accessedPage >= pages) {
		                player.sendMessage(Colors.Rose + "Invalid page \""
		                        + split[1] + "\"");
		                return true;
		            }
		        } catch (NumberFormatException e) {
		            player.sendMessage(Colors.Rose + "Invalid page \"" + split[1]
		                    + "\"");
		            return true;
		        }
		
		        player.sendMessage(Colors.Blue + "CraftBook ICs (Page "
		                + (accessedPage + 1) + " of " + pages + "):");
		        
		        for (int i = accessedPage * 10; i < lines.length
		                && i < (accessedPage + 1) * 10; i++) {
		            player.sendMessage(lines[i]);
		        }
		
		        return true;
		}
		



}