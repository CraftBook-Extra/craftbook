import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sk89q.craftbook.WorldBlockVector;


public class CBXItemStorage {
	private List<Inventory> storage = new LinkedList<Inventory>();
	private Set<Inventory> changedInventories = new HashSet<Inventory>(8);
	private Set<WorldBlockVector> positions = new HashSet<WorldBlockVector>(8);
	private Set<Integer> allowedBlocks = new TreeSet<Integer>();
	
	
	/** There must be at least one type of storage block allowed, or you won't be able
	 *  to add storage blocks.
	 * 
	 * @param type Block.Type to allow as storage. Invalid types will be ignored.
	 */
	public void addAllowedStorageBlockType(Block.Type type) {
		allowedBlocks.add(type.getType());
	}
	
	/** There must be at least one type of storage block allowed, or you won't be able
	 *  to add storage blocks.
	 * 
	 * @param type block id to allow as storage. Invalid types will be ignored.
	 */
	public void addAllowedStorageBlockType(int type) {
		allowedBlocks.add(type);
	}

	/** There must be at least one type of storage block allowed, or you won't be able
	 *  to add storage blocks.
	 * 
	 * @param type block ids to allow as storage. Invalid types will be ignored.
	 */
	public void addAllowedStorageBlockIds(Collection<Integer> type) {
		allowedBlocks.addAll(type);
	}
	
	/**
	 * Stores an Item.
	 * 
	 * @param item The Item to store. The amount gets reduced by the amount stored. Handle (item.getAmount < 1) !
	 * @return true if all items in the stack have been stored, false otherwise.
	 */
	public boolean storeItem(Item item){
		boolean allStored = false;
		for (Inventory inventory : storage) {
			if (item.getAmount() > 0) {
				int amountBefore=item.getAmount();
				allStored = storeItem(inventory, item);
				if (item.getAmount() != amountBefore) {
					changedInventories.add(inventory);
				}
			} else {
				allStored = true;
			}
			if (allStored) break;
		}
		return allStored;
	}
	
		
	public static boolean storeItem(Inventory inv, Item item) {
		// determine maximum stack size for this item type
		int maxStackSize = maxStackSize(item);
		if (item.getEnchantments().length != 0  && ! InventoryListener.allowEnchantableItemStacking) {
			maxStackSize = 1;
		}
		// the contents array gives us more control than the Inventory methods
		Item[] contents = inv.getContents();
		// stack if possible
		if (maxStackSize > 1) {
			for (int slot = 0; slot < contents.length; slot ++) {
				if (contents[slot] == null) continue;
				// there's something in this slot
				int freeSpace = maxStackSize - contents[slot].getAmount(); 
				if (freeSpace > 0
						&& contents[slot].getItemId() == item.getItemId()
						&& contents[slot].getDamage() == item.getDamage()
						&& (contents[slot].getDataTag() == null
						||contents[slot].getDataTag().equals(item.getDataTag()))
						&& (contents[slot].getEnchantments().length == 0
						|| contents[slot].getEnchantments().equals(item.getEnchantments()))) {
					int storeAmount = (item.getAmount() > freeSpace) ? freeSpace : item.getAmount();
					contents[slot].setAmount(contents[slot].getAmount() + storeAmount);
					item.setAmount(item.getAmount() - storeAmount);
					if (item.getAmount() < 1) {
						return true;
					}
				}
			}
		}
		// we've tried stacking but still have some items left in the stack
		// do we have an empty slot?
		int freeSlot = inv.getEmptySlot();
		if (freeSlot < 0) return false;
		Item itemClone = item.clone();
		itemClone.setSlot(freeSlot);
		inv.addItem(itemClone);
		item.setAmount(0);
		return true;
	}
	
	public Item fetchItem(Item.Type type, int amount){
		return fetchItem(type.getId(), -1, amount);
	}
	
	/**
	 * Takes a stack of items out of storage
	 * @param id
	 * @param datavalue
	 * @param amount The desired amount. 
	 * 			The amount in the returned Item may be less than this
	 * 			if there weren't enough items of this type in storage
	 * 			or if the desired amount exceeds maximum stack size for
	 * 			this item type. 
	 * @return The Item taken from storage, or null if no suitable item was found in this storage.
	 */
	public Item fetchItem(int id, int datavalue, int amount){
		int amountRequested = (amount < 0) ? Integer.MAX_VALUE : amount;
		Item retItem = null;
		int maxRetAmount = 0;
		for (Inventory inventory : storage) {
			// the contents array gives us more control than the Inventory methods
			Item[] contents = inventory.getContents();
			for (int slot=0; slot < contents.length; slot++) {
				if (matchItem(contents[slot], id, datavalue)) {
					if (retItem == null) {
						// prepare a new Item with the right properties
						retItem = contents[slot].clone();
						retItem.setAmount(0);
						maxRetAmount = Math.min(CBXItemStorage.maxStackSize(retItem), amountRequested);
					}
					if (retItem.equalsIgnoreSlotAndAmount(contents[slot])) {
						// take items from this slot
						int fetchAmount = Math.min(maxRetAmount - retItem.getAmount(), contents[slot].getAmount());
						retItem.setAmount(retItem.getAmount() + fetchAmount);
						contents[slot].setAmount(contents[slot].getAmount() - fetchAmount);
						if (contents[slot].getAmount() < 1) {
							// we've emptied the slot
							contents[slot] = null;
							inventory.removeItem(slot);
						}
						changedInventories.add(inventory);
					}
					// Paranoid? Better safe than sorry!
					if (retItem.getAmount() < 1) {
						retItem = null;
					}
				}
			}
			if (retItem != null && retItem.getAmount() >= maxRetAmount) break;
		}
		return retItem;
	}

	public boolean containsItem(Item.Type type, int minAmount) {
		return containsItem(type.getId(), -1,  minAmount);
	}
	
	/** Checks if the storage contains at least the specified amount of the specified item
	 * 
	 * @param id Item Id
	 * @param datavalue data value to look for, -1 matches all
	 * @param minAmount must be greater than 0
	 * @return true if the specified amount was found
	 */
	public boolean containsItem(int id, int datavalue, int minAmount) {
		// we always have 0 or negative amounts of anything
		if (minAmount < 1) return true;
		// search inventory
		int count = 0;
		for (Inventory inventory : this.storage) {
			Item[] contents = inventory.getContents();
			for (int slot = 0; slot < contents.length; slot++) {
				if (contents[slot] != null
						&& matchItem(contents[slot], id, datavalue)) {
					count += contents[slot].getAmount();
					if (count >= minAmount) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Transfers all items in the source inventory into the storage.
	 * @param source The inventory from which to take the items
	 * @return true if all items have been stored
	 */
	public boolean storeAllItems(Inventory source) {
		Item[] contents = source.getContents();
		boolean allStored = true;
		for (int slot = 0; slot < contents.length; slot++) {
			if (contents[slot] == null) {
				continue;
			}
			if (! this.storeItem(contents[slot])) {
				allStored = false;
			}
			if (contents[slot].getAmount() < 1) {
				contents[slot] = null;
				source.removeItem(slot);
			}
		}
		return allStored;
	}
	
	
	public boolean storeAllItems(Inventory source, int id, int datavalue, int amount) {
		int remainingAmount = (amount < 0)? Integer.MAX_VALUE : amount;
		Item[] contents = source.getContents();
		for (int slot = 0; slot < contents.length; slot++) {
			if (remainingAmount < 1) {
				break;
			}
			if (contents[slot] == null || ! matchItem(contents[slot], id, datavalue)) {
				continue;
			}
			Item toStore = contents[slot].clone();
			int amountBefore = toStore.getAmount();
			if (amountBefore > remainingAmount) {
				// store only the remaining amount
				toStore.setAmount(remainingAmount);
				amountBefore = remainingAmount;
			}
			this.storeItem(toStore);
			int amountStored = amountBefore - toStore.getAmount();
			remainingAmount -= amountStored;
			contents[slot].setAmount(contents[slot].getAmount() - amountStored);
			if (contents[slot].getAmount() < 1) {
				contents[slot] = null;
				source.removeItem(slot);
			}
		}
		return remainingAmount < 1;
	}
	
	public boolean fetchAllItems(Inventory target) {
		Item item = null;
		List <Item> couldNotTransfer = new LinkedList<Item>();
		while (true) {
			item = fetchItem(-1, -1, -1);
			if (item == null) {
				break;
			}
			// put the item into the target inventory
			boolean stored = CBXItemStorage.storeItem(target, item);
			if (! stored) {
				couldNotTransfer.add(item);
			}
		}
		if  (couldNotTransfer.isEmpty()){
			return true;
		} else {
			// put everything that doesn't fit in the target back into storage
			for (Item i:couldNotTransfer) {
				storeItem(i);
			}
		}
		return false;
	}
	
	public boolean fetchAllItems(Inventory target, int id, int datavalue, int amount) {
		// negative amount: no limit
		int amountRemaining = (amount < 0) ? Integer.MAX_VALUE: amount;
		while (amountRemaining > 0) {
			Item item = fetchItem(id, datavalue, amountRemaining);
			if (item == null) {
				//storage is empty
				return false;
			}
			int amountBefore = item.getAmount();
			boolean targetFull = ! CBXItemStorage.storeItem(target, item);
			if (targetFull) {
				return false;
			}
			int amountStored = amountBefore - item.getAmount();
			amountRemaining -= amountStored; 
		}
		return true;
	}
	
	
	/**
	 * Adds storage block at wbv to be used as storage space.
	 * @param wbv
	 * @return true if the block was added, false if wbv does not point to a suitable storage block
	 */
	public boolean addStorageBlock(WorldBlockVector wbv) {
		if (!Util.isBlockLoaded(wbv)) return false;
		World world = CraftBook.getWorld(wbv.getCBWorld());
		ComplexBlock cBlock = world.getComplexBlock(wbv.getBlockX(), wbv.getBlockY(), wbv.getBlockZ());
		if (cBlock == null) return false;
		if (! allowedBlocks.contains(cBlock.getBlock().getType())) return false;
		if (cBlock instanceof Inventory) {
			WorldBlockVector cBlockPos = new WorldBlockVector(wbv.getCBWorld(),cBlock.getX(), cBlock.getY(), cBlock.getZ());;
			if (positions.add(cBlockPos)) {
				storage.add((Inventory) cBlock);
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Adds all storage blocks in a 7x7x7 cube centered around wbv to be used as storage space.
	 * @param wbv
	 * @return true, if at least one storage block was added, false if no suitable block was found
	 */
	public boolean addNearbyStorageBlocks(WorldBlockVector wbv) {
		boolean addedAtLeastOne = false;
    	for (int x = -3; x <= 3; x++) {
    		for (int y = -3; y <= 3; y++) {
    			for (int z = -3; z <= 3; z++) {
    				WorldBlockVector cur = new WorldBlockVector(wbv.getCBWorld(), wbv.add(x, y, z));
    				addedAtLeastOne = addStorageBlock(cur) || addedAtLeastOne;
    			}
    		}
    	}
		return addedAtLeastOne;
	}
	
	/**
	 * Adds all storage blocks in a 5x5x2 cuboid centered around wbv,
	 * as well as those located 2 and 3 blocks directly above to be used as storage space.
	 * Used for [Deposit]/[Collect].
	 * 
	 * @param wbv
	 * @return true, if at least one storage block was added, false if no suitable block was found
	 */
	public boolean addNearbyRailStorageBlocks(WorldBlockVector wbv) {
		boolean addedAtLeastOne = false;
		// Add Chests around the block
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				for (int y = -1; y <= 0; y++) {
					WorldBlockVector cur = new WorldBlockVector(wbv.getCBWorld(), wbv.add(x, y, z));
   					addedAtLeastOne = addStorageBlock(cur) || addedAtLeastOne;
				}
			}
		}
		// Also allow the rail system to be under the floor stocking chests directly above.
		// Not in the loop so that the chest cannot be offset on X or Z, only Y.
		addedAtLeastOne = addStorageBlock(new WorldBlockVector(wbv.getCBWorld(), wbv.add(0, 2, 0)))|| addedAtLeastOne;
		addedAtLeastOne = addStorageBlock(new WorldBlockVector(wbv.getCBWorld(), wbv.add(0, 3, 0)))|| addedAtLeastOne;
		return addedAtLeastOne;
	}

	/**
	 * Sends changes to players. Call when you're done storing and fetching for this tick. Don't forget!
	 */
	public void update() {
		for (Inventory inventory : changedInventories) {
			inventory.update();
		}
		changedInventories.clear();
	}
	
	private boolean matchItem(Item item, int id, int dataValue) {
		return (item != null
				&& item.getAmount() > 0
				&& (id < 0 || item.getItemId() == id)
				&& (dataValue < 0 || item.getDamage() == dataValue));
	}
	
	public static int maxStackSize(Item item) {
		int maxStackSize = item.getMaxAmount();
		if (InventoryListener.allowMinecartStacking
				&& (item.getType().equals(Item.Type.Minecart)
						|| item.getType().equals(Item.Type.StorageMinecart)
						|| item.getType().equals(Item.Type.PoweredMinecart)
						|| item.getType().equals(Item.Type.MinecartTNT)
						|| item.getType().equals(Item.Type.MinecartHopper)
						)) {
			maxStackSize = 64;
		}
		if (item.getEnchantments().length != 0 && ! InventoryListener.allowEnchantableItemStacking) {
			maxStackSize = 1;
		}
		return maxStackSize;
	}
}
