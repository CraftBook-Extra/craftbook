/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class SignPatch extends OBlockSign {
    public static final CopyOnWriteArrayList<ExtensionListener> LISTENERS = new CopyOnWriteArrayList<ExtensionListener>();
    
    private static Class<OBlockSign> CLASS = OBlockSign.class;
    private static Field[] FIELDS;
    
    private static int TYPE = Block.Type.WallSign.getType();
    
    private OBlock old;
    
    static {
        ArrayList<Field> fields = new ArrayList<Field>();
        for(Class<?> c = CLASS;;c=c.getSuperclass()) {
            if(c==Object.class) break;
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        FIELDS = fields.toArray(new Field[0]);
    }
    
    public SignPatch(OBlock old) {
        super(nullId(), OTileEntitySign.class, false);
        
        this.old = old;
        
        for(Field f:FIELDS) try {
            if(Modifier.isStatic(f.getModifiers())||Modifier.isFinal(f.getModifiers())) continue;
            f.setAccessible(true);
            Object o = f.get(old);
            f.setAccessible(true);
            f.set(this, o);
        } catch (Exception e) {
            System.out.println("Failed to copy field: "+f.getName());
            e.printStackTrace();
        }
    }
    private static int nullId() {
    	OBlock.m[TYPE] = null;
        return TYPE;
    }

    /**
     * Patch method.
     * Should not be called.
     */
    @Deprecated
    public void a(OWorld world, int x, int y, int z, int type, int direction) {
    	if(!(world instanceof OWorldServer))
    		return;
    	
        ExtensionListener[] tasks = LISTENERS.toArray(new ExtensionListener[0]);
        for(int i=0;i<tasks.length;i++) tasks[i].onSignAdded(new World((OWorldServer)world), x,y,z);
        super.a(world,x,y,z,type,direction);
    }
    
    /**
     * Applies the patch, overriding any old instances that may exist.
     * Call before using addListener or getListenerList().
     */
    public static void applyPatch() {
        new SignPatch(OBlock.m[TYPE]);
    }
    /**
     * Removes the patch if it is applied.
     */
    public static void removePatch() {
        if(OBlock.m[TYPE] instanceof SignPatch) OBlock.m[TYPE] = ((SignPatch)OBlock.m[TYPE]).old;
    }
    
    /**
     * Adds a new task.
     */
    public static void addListener(ExtensionListener r) {
        LISTENERS.add(r);
    }
    
    /**
     * Wraps a HookExtension to allow easier use by plugins.
     */
    public static ExtensionListener wrapListener(final Plugin p, final ExtensionListener r) {
        return new ExtensionListener() {
            private PluginLoader l = etc.getLoader();
            private long lastCheck = 0;
            public void onSignAdded(World world, int x, int y, int z) {
                if(world.getTime()!=lastCheck) {
                    if(l.getPlugin(p.getName())!=p) {
                        CopyOnWriteArrayList<ExtensionListener> taskList = LISTENERS;
                        while(taskList.contains(this)) taskList.remove(this);
                        return;
                    }
                    lastCheck = world.getTime();
                }
                if(p.isEnabled()) r.onSignAdded(world, x,y,z);
            }
        };
    }
    
    public static interface ExtensionListener {
        public void onSignAdded(World world, int x, int y, int z);
    }
}
