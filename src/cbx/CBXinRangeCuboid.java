package cbx;

import com.sk89q.craftbook.Vector;

public class CBXinRangeCuboid implements ICBXinRangeTester {
	final Vector distFromCenter;
	
	public CBXinRangeCuboid(double maxDistX, double maxDistY, double maxDistZ) {
		distFromCenter = new Vector(maxDistX, maxDistY, maxDistZ);
	}
	
	@Override
	public boolean inRange(Vector reference, Vector other) {
		Vector minVec = reference.subtract(distFromCenter);
		Vector maxVec = reference.add(distFromCenter);
		return other.containedWithin(minVec, maxVec);
	}
}