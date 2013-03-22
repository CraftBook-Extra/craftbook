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
	 * @return true, if all items in the stack have been stored, false otherwise.
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
		Item[] storageArray = inv.getContents();
		// stack if possible
		if (item.getEnchantment() == null 
				|| InventoryListener.allowEnchantableItemStacking) {
			for (int slot = 0; slot < storageArray.length; slot ++) {
				if (storageArray[slot] == null) continue;
				int maxAmount = storageArray[slot].getMaxAmount();
				if (item.getType().equals(Item.Type.Minecart)) {
					//stack minecarts
					maxAmount = 64;
				}
				int freeSpace = maxAmount - storageArray[slot].getAmount(); 
				if (freeSpace > 0
						&& storageArray[slot].getItemId() == item.getItemId()
						&& storageArray[slot].getColor() == item.getColor()
						&& (storageArray[slot].getDataTag() == null
						||storageArray[slot].getDataTag().equals(item.getDataTag()))
						&& (storageArray[slot].getEnchantment() == null
						|| storageArray[slot].getEnchantments().equals(item.getEnchantments()))) {
					int storeAmount = (item.getAmount() > freeSpace) ? freeSpace : item.getAmount();
					storageArray[slot].setAmount(storageArray[slot].getAmount() + storeAmount);
					item.setAmount(item.getAmount() - storeAmount);
					if (item.getAmount() < 1) {
						return true;
					}
				}
			}
		}
		// we've tried stacking but still have some items left in the stack
		// do we have an empty slot?
		if (inv.getEmptySlot() < 0)	return false;
		inv.addItem(item.clone());
		item.setAmount(0);
		return true;
	}
	
	/**
	 * Takes a stack of items out of storage
	 * @param id
	 * @param datavalue
	 * @param amount The desired amount. The amount in the returned Item may be less than this if there weren't enough items of this type in storage 
	 * @return The Item taken from storage, or null if no suitable item was found in this storage.
	 */
	public Item fetchItem(int id, int datavalue, int amount){
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
						maxRetAmount = Math.min(retItem.getMaxAmount(), amount);
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

	
	/**
	 * Adds storage block at wbv to be used as storage space.
	 * @param wbv
	 * @return true, if the block was added, false if wbv does not point to a suitable storage block
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

//	/** Stores all items from the source array that match the criteria.
//	 * 
//	 * @param source The items to store. The amount of the items will be reduced by the amount stored. Stacks that are stored completely will be set to null.
//	 * @param id Item id to be stored, negative values match all
//	 * @param dataValue Data value of items to be stored, negative values match all 
//	 * @param amount Number of items to store, use 0 to store all that match
//	 * @return true, if the desired amount was transferred, false otherwise
//	 */
//	public boolean storeItemsFromArray(Item[] source, int id, int dataValue, int amount ) {
//		boolean allStored=false;
//		int amountToStore = amount;
//		for (int slot = 0; slot < source.length; slot++) {
//			if (matchItem(source[slot], id, dataValue)) {
//				if (amount == 0) {
//					// store everything
//					storeItem(source[slot]);
//				} else {
//					// keep track of amount
//					if (amountToStore > 0) {
//						int amountToStoreNow = Math.min(amountToStore, source[slot].getAmount());
//						Item itemToStore = source[slot].clone();
//						itemToStore.setAmount(amountToStoreNow);
//						storeItem(itemToStore);
//						int amountStored = amountToStoreNow - itemToStore.getAmount();
//						source[slot].setAmount(source[slot].getAmount() - amountStored);
//						amountToStore -= amountStored;
//					}
//				}
//				// set empty slots to null
//				if (source[slot].getAmount() < 1) {
//					source[slot] = null;
//				}
//			}
//		}
//		// has the desired amount been stored?
//		if (amount == 0) {
//			// check if there are any matching items left in the source array
//			allStored = true;
//			for (Item item : source) {
//				if (matchItem(item, id, dataValue) ){
//					allStored = false;
//					break;
//				}
//			}	
//		} else {
//			// check if there is anything left to store
//			allStored = (amountToStore < 1);
//		}
//		return allStored;
//	}
//	
//	
//	/**
//	 * Takes items that match criteria out of storage and puts them into the target array.
//	 * @param target
//	 * @param id
//	 * @param color
//	 * @param amount
//	 */
//	//TODO: test
//	public void fetchItemsToArray(Item[] target, int id, int datavalue, int amount ) {
//		final int EVERYTHING = 10000000;
//		int amountToFetch = (amount > 0) ? amount : EVERYTHING;
//		int completed = 0;
//		// Temporary space for Items we can't put into target. Ugly, but simple.
//		// TODO: change because this wastes resources
//		List<Item> itemsToReturn = new LinkedList<Item>();
//		while (completed < amountToFetch) {
//			//fetch items from storage
//			Item item = this.fetchItem(id, datavalue, amountToFetch - completed);
//			if (item == null) break; // storage doesn't have the item
//			int amountBefore = item.getAmount(); 
//			//put as many as possible into target
//			storeItem(target, item);
//			int amountTransferred = amountBefore - item.getAmount();
//			completed += amountTransferred;
//			// If it doesn't fit, put it aside to return it later.
//			// Can't put it back now or we'll fetch it again in an endless loop.
//			if (item.getAmount() > 0) {
//				itemsToReturn.add(item);
//			}
//		}
//		//store remaining items
//		for (Item toReturn : itemsToReturn) {
//			storeItem(toReturn);
//		}
//	}
	
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
}
