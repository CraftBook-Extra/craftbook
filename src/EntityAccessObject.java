import java.util.Set;

/*
 * Minecraft Sitting Mod
 * Copyright (C) 2012  M4411K4
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


public class EntityAccessObject
{
	@SuppressWarnings("rawtypes")
	public final Set trackedEntitySet;
	public final OIntHashMap trackedEntityHashTable;
	public final OIntHashMap entityIdMap;
	
	public EntityAccessObject(@SuppressWarnings("rawtypes") Set trackedEntitySet, OIntHashMap trackedEntityHashTable, OIntHashMap entityIdMap)
	{
		this.trackedEntitySet = trackedEntitySet;
		this.trackedEntityHashTable = trackedEntityHashTable;
		this.entityIdMap = entityIdMap;
	}
}
