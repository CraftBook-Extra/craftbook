/* CBXEntityFinder
 * Part of CraftBook-Extra
 *
 * Copyright (C) 2013 Stefan Steinheimer <nosefish@gmx.net>
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

import java.util.ConcurrentModificationException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import cbx.CBXinRangeSphere;
import cbx.ICBXinRangeTester;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.Vector;




/**
 * Finds entities matching certain criteria within a set distance form a point.
 * This is semi-thread-safe - it won't crash the server if run in parallel, but might
 * in rare cases return wrong or incomplete results.
 * Maximum distance is 128.
 *  
 * @author Stefan Steinheimer (nosefish)
 *
 */
public final class CBXEntityFinder implements Runnable{
	private static final int CME_RETRIES = 5; // maximum number of retries after ConcurrentModificationException
	public static final int DISTANCE_LIMIT = 128;

	// -----------------------------------------------------------------------
	
	private final World world;
	private final Vector origin;
	private final double maxDistance;
	private final ResultHandler resultHandler;
	private List<BaseEntityFilter> baseEntityFilters = new LinkedList<BaseEntityFilter>();
	private ICBXinRangeTester distanceCheck;

	// -----------------------------------------------------------------------------------------
	
	/**
	 * Creates a new CBXEntityFinder with an empty filter. 
	 * 
	 * Use addXXFilter() unless you really want to get <i>all</i> entities in the area.
	 * 
	 * @param cbworld
	 * @param center 
	 * @param maxDistance Maximum distance from the center, uses DistanceType.SPHERE by default.
	 * @param resultHandler The class to handle the result.
	 */
	public CBXEntityFinder(CraftBookWorld cbworld,
							Vector center,
							double maxDistance,
							ResultHandler resultHandler) {
		this.world = CraftBook.getWorld(cbworld);
		this.origin = center;
		// TODO: use world's global entity lists for large maxDistance and remove the artificial limit
		this.maxDistance = (maxDistance <= DISTANCE_LIMIT ? maxDistance : DISTANCE_LIMIT);
		this.resultHandler = resultHandler;
		if (this.distanceCheck == null) {
			this.distanceCheck = new CBXinRangeSphere(maxDistance);
		}
	}


	// ------------------------------------------------------------------------------------
	
	/**
	 * If any of the filters match, the entity will be added to the results passed to the RHBlowUpWithTNT.
	 * @param baseEntityFilter
	 */
	public void addCustomFilter(BaseEntityFilter baseEntityFilter) {
		this.baseEntityFilters.add(baseEntityFilter);
	}
	
	/**
	 * Find players.
	 * 
	 */
	public void addPlayerFilter() {
		this.baseEntityFilters.add(new FilterPlayer());
	}
	
	/**
	 * Find the player with the specified name.
	 * @param name
	 */
	public void addPlayerFilter(String name) {
		this.baseEntityFilters.add(new FilterPlayer(name));
	}
	
	/**
	 * Find players whose names match the regular expression.
	 * @param name
	 */
	public void addPlayerFilterRegEx(Pattern regEx) {
		this.baseEntityFilters.add(new FilterPlayerRegEx(regEx));
	}	

	/**
	 * Uses UtilEntity.parseEntityArgs and UtilEntity.isValidPlayerEntity() to match criteria.
	 * @param name
	 */
	public void addPlayerFilterExtended(String criteria) {
		this.baseEntityFilters.add(new FilterPlayerExtended(criteria));
	}	
	
	/**
	 * Find animals.
	 */
	public void addAnimalFilter() {
		this.baseEntityFilters.add(new FilterAnimal());
	}
	
	/**
	 * Find only animals with the specified name.
	 * @param name
	 */
	public void addAnimalFilter(String name) {
		this.baseEntityFilters.add(new FilterAnimal(name));
	}
	
	
	/**
	 * Find mobs.
	 */
	public void addMobFilter() {
		this.baseEntityFilters.add(new FilterMob());
	}
	

	/**
	 * Find only mobs with the specified name.
	 * @param name
	 */
	public void addMobFilter(String name) {
		this.baseEntityFilters.add(new FilterMob(name));
	}
	

	/**
	 * Find items.
	 */
	public void addItemFilter() {
		this.baseEntityFilters.add(new FilterItem());
	}
	
	/**
	 * Find only items with the specified ID.
	 * 
	 * @param id the item id to match; negative values match all item IDs
	 */
	public void addItemFilter(int id) {
		this.baseEntityFilters.add(new FilterItem(id));
	}
	
	/**
	 * Find only items with the specified ID and data value.
	 * @param id the item id to match; negative values match all item IDs
	 * @param data data value to match; negative values match all data values
	 */
	public void addItemFilter(int id, int data) {
		this.baseEntityFilters.add(new FilterItem(id, data));
	}
	

	/**
	 * Specify how the distance is calculated. Default is CBXinRangeSphere.
	 * 
	 * @param dType
	 */
	public void setDistanceCalculationMethod(ICBXinRangeTester rangeTester) {
		this.distanceCheck = rangeTester;
	}
	
		
	// ------------------------------------------------------------------------------------
	/**
	 * Run the EntiyFinder.
	 */
	@Override
	public void run() {
		List<Object> candidateEntities = new LinkedList<Object>();
		// TODO: use world's global entity lists for large maxDistance
		getCandidateEntities(getCandidateChunks(), candidateEntities);

		Set<BaseEntity> foundEntities = filterCandidateEntities(candidateEntities);
		try {
			this.resultHandler.handleResult(foundEntities);
		} catch(Throwable t) {
			System.out.println("CraftBook-CBXEntityFinder: Exception while running resultHandler");
			t.printStackTrace();
		}
	}
	
	// TODO: check if this is correct, I was tired when I wrote it.
	// Should check all chunks that overlap a cube with sides of
	// length this.maxDistance*2, centered on this.origin
	private List<Chunk>getCandidateChunks() {
		double outerMaxX = (Math.ceil(origin.getX()) + maxDistance) / 16;
		double outerMinX = (Math.floor(origin.getX()) - maxDistance) / 16 - 1;
		double outerMaxZ = (Math.ceil(origin.getZ()) + maxDistance) / 16;
		double outerMinZ = (Math.floor(origin.getZ()) - maxDistance) / 16 - 1;
	
		List<Chunk> result = new LinkedList<Chunk>();
		for (int x = (int)Math.round(Math.floor(outerMinX)); x <= outerMaxX; x++) {
			for (int z = (int)Math.round(Math.floor(outerMinZ)); z <= outerMaxZ; z++) {
				// retry on ConcurrentModificationException 
				for (int retry=0; retry < CME_RETRIES; retry++) {
					try {
						if ( world.isChunkLoaded(x, z)
								&& Util.chunkHasEntities(world.getChunk(x, z)))  {
							result.add(world.getChunk(x,z));
						}
						break; //it worked, no need to do it again
					} catch (ConcurrentModificationException e) {
						sleep1ms();
					} catch (NullPointerException e) {
						break; // something bad happened
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}
		return result;
	}

	
	private void getCandidateEntities(List<Chunk> chunkList, List<Object> output) {
		for (Chunk chunk : chunkList) {
			// retry on ConcurrentModificationException 
			for (int retry=0; retry < CME_RETRIES; retry++) {
				try {
					for (List<Object> entities : Util.getEntityLists(chunk)) {
						output.addAll(entities);
					}
					break; //it worked, no need to do it again
				} catch (ConcurrentModificationException e) {
					sleep1ms();
				} catch (NullPointerException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	private HashSet<BaseEntity> filterCandidateEntities(List<Object> candidateEntities) {
		HashSet<BaseEntity> foundEntities = new HashSet<BaseEntity>(); //BaseEntities are not comparable
		for (Object oEntity : candidateEntities) {
			// retry on ConcurrentModificationException
			for (int retry=0; retry < CME_RETRIES; retry++) {
				try {
					BaseEntity entity = null;
					if (oEntity instanceof BaseEntity) {
						entity = (BaseEntity) oEntity;
					} else if (oEntity instanceof OEntity) {
						entity = new BaseEntity((OEntity)oEntity);
					} else {
						break; //some joker is feeding us invalid objects
					}
					if (! world.isChunkLoaded((int)entity.getX(), (int)entity.getY(), (int)entity.getZ())){
						continue;
					}
					if (inRange(entity) && filter(entity)){
						foundEntities.add(entity);
					}
					break; //it worked, no need to do it again
				} catch (ConcurrentModificationException e) {
					sleep1ms();
				} catch (NullPointerException e) {
					break; //the entity is gone, ignore it
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		return foundEntities;
	}
	

	
	private boolean filter(BaseEntity bEntity) throws ConcurrentModificationException{
		// let dead entities rest in peace
		if (bEntity.isDead()) {
			return false;
		}
		// empty list matches all
		if (this.baseEntityFilters.isEmpty()) {
			return true;
		}
		for (BaseEntityFilter bef : this.baseEntityFilters) {
			if (bef.match(bEntity)) {
				return true;
			}
		}
		// no match
		return false;
	}
	

	private boolean inRange(BaseEntity bEntity) {
		// retry on ConcurrentModificationException 
		for (int retry=0; retry < CME_RETRIES; retry++) {
			try {
				if (this.world == bEntity.getWorld()) {
					Vector entityPos = new Vector(bEntity.getX(), bEntity.getY(), bEntity.getZ());
					return this.distanceCheck.inRange(origin, entityPos);
				}
			} catch (ConcurrentModificationException e) {
				sleep1ms();
			} catch (NullPointerException e) {
				break; // something bad happened
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		return false;
	}
	
	
	private void sleep1ms() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	// -----------------------------------------------------------------------
	public static interface ResultHandler {
		public void handleResult(Set<BaseEntity> foundEntities);
	}
	
	public static interface BaseEntityFilter {
		/**
		 * 
		 * @param bEntity
		 * @return true, if the BaseEntity should be in the results passed to the RHBlowUpWithTNT
		 */
		public boolean match(BaseEntity bEntity);
	}
	
	public static class FilterItem implements BaseEntityFilter{
		private int id;
		private int dataValue;
		
		public FilterItem() {
			this(-1);
		}

		public FilterItem(int id) {
			this(id, -1);
		}
		
		public FilterItem(int id, int data) {
			this.id = id;
			this.dataValue = data;
		}
		
		public boolean match(BaseEntity bEntity) {
			OEntity oEntity = bEntity.getEntity(); 
			if (oEntity instanceof OEntityItem) {
				// Could do this without creating a new object using Notchian, but let's avoid that.
				ItemEntity itemEntity = new ItemEntity((OEntityItem) oEntity);
				Integer itemID = itemEntity.getItem().getItemId();
				Integer itemDV = itemEntity.getItem().getDamage();
				if ((id < 0 || id == itemID) && (dataValue < 0 || dataValue == itemDV )) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static class FilterMob implements BaseEntityFilter {
		private String name = null;
		
		public FilterMob() {			
		}
		
		public FilterMob(String name) {
			this.name = name;
		}
		
		@Override
		public boolean match(BaseEntity bEntity) {
			if (bEntity.isMob()) {
				if (name == null || name.equalsIgnoreCase(bEntity.getName())) {
					return true;
				}
			}
			return false;
		}
	}

	public static class FilterAnimal implements BaseEntityFilter {
		private String name = null;
		
		public FilterAnimal() {
		}
		
		public FilterAnimal(String name) {
			this.name = name;
		}
		
		@Override
		public boolean match(BaseEntity bEntity) {
			if (bEntity.isAnimal()) {
				if (this.name == null || this.name.equalsIgnoreCase(bEntity.getName())) {
					return true;
				}
			}
			return false;
		}
		
	}
		
	public static class FilterPlayer implements BaseEntityFilter {
		private String name = null;
		
		public FilterPlayer() {
		}
		
		public FilterPlayer(String name) {
			this.name = name;
		}	
		
		@Override
		public boolean match(BaseEntity bEntity) {
			if (bEntity.isPlayer()) {
				Player player = new Player((OEntityPlayerMP)bEntity.getEntity());
				if (this.name == null || this.name.equalsIgnoreCase(player.getName())) {
					return true;
				}
			}
			return false;
		}	
	}
	
	public static class FilterPlayerRegEx implements BaseEntityFilter {
		final Pattern regEx;
		public FilterPlayerRegEx(Pattern regEx) {
			this.regEx = regEx;
			
		}
		@Override
		public boolean match(BaseEntity bEntity) {
			if (bEntity.isPlayer()) {
				if (this.regEx == null) {
					return false;
				} else {
					Player player = new Player((OEntityPlayerMP)bEntity.getEntity());
					if (regEx.matcher(player.getName()).matches()) {
						return true;
					}
				}
			}
			return false;
		}	
	}
	
	/**
	 * Uses UtilEntity.parseEntityArgs and UtilEntity.isValidPlayerEntity().
	 *
	 */
	public class FilterPlayerExtended implements BaseEntityFilter {
		private String filterCriteria;
		
		public FilterPlayerExtended(String criteria) {
			this.filterCriteria = criteria;
		}
		
		@Override
		public boolean match(BaseEntity bEntity) {
			if (bEntity.isPlayer()) {
				if (filterCriteria == null || filterCriteria.isEmpty()) {
					return true;
				} else {
					String[] values = UtilEntity.parseEntityArgs(filterCriteria);
					if (values != null && UtilEntity.isValidPlayerEntity(bEntity, values[0], values[1])) {
						return true;
					}
				} 
			}
			return false;
		}
	}
}
