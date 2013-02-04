package cbx;

import com.sk89q.craftbook.Vector;

public class CBXinRangeSphere implements ICBXinRangeTester {
	private final double radius;
	
	public CBXinRangeSphere(double radius) {
		this.radius = radius;
	}

	@Override
	public boolean inRange(Vector center, Vector other) {
		// compare squares of the distances
		double xDist = center.getX() - other.getX();
		double yDist = center.getY() - other.getY();
		double zDist = center.getZ() - other.getZ();
		return (xDist * xDist)
			 + (yDist * yDist) 
			 + (zDist * zDist) 
			 <= radius * radius;
	}

}
