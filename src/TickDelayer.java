// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.sk89q.craftbook.BlockVector;

/**
 * Thread for delaying redstone inputs.
 * 
 * @author Lymia
 * @author sk89q
 */
public class TickDelayer implements Runnable {
    /**
     * List of actions to delay.
     */
    private LinkedHashSet<Action> delayedActions = new LinkedHashSet<Action>();

    /**
     * Delay an action.
     * 
     * @param action
     */
    public void delayAction(Action action) {
        delayedActions.add(action);
    }

    /**
     * Run thread.
     */
    public void run() {
        ArrayList<Action> actionQueue = new ArrayList<Action>();
        
        for (Iterator<Action> it = delayedActions.iterator(); it.hasNext(); ) {
            Action action = it.next();
            long currentTick = action.getWorld().getTime();
            if (action.getRunAt() <= currentTick) {
                it.remove();
                actionQueue.add(action);
            }
        }
        
        for (Action action : actionQueue) {
            try {
                action.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    /**
     * Action to delay.
     * 
     * @author sk89q
     */
    public static abstract class Action {
        /**
         * Stores the point associated with this delayed action.
         */
        private BlockVector pt;
        private World world;
        /**
         * Tick to perform the action at.
         */
        private long runAt = 0;
        
        /**
         * Construct the object.
         * 
         * @param pt
         * @param tickDelay
         */
        public Action(World world, BlockVector pt, long tickDelay) {
            this.pt = pt;
            this.world = world;
            runAt = world.getTime() + tickDelay;
        }
        
        /**
         * Run the action.
         */
        public abstract void run();
        
        /**
         * Get the world.
         * 
         * @return
         */
        public World getWorld() {
            return world;
        }
        
        /**
         * Get the tick to run at.
         * 
         * @return
         */
        public long getRunAt() {
            return runAt;
        }
        
        /**
         * Return the hash code.
         * 
         * @return hash code
         */
        public int hashCode() {
            return pt.hashCode();
        }
        
        /**
         * Returns whether the other object is equal.
         * 
         * @param other
         * @return
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof Action)) {
                return false;
            }
            Action other = (Action)obj;
            return other.pt.equals(pt);
        }
    }
}
