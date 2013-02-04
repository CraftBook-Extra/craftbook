package cbx;

import com.sk89q.craftbook.Vector;

public interface ICBXinRangeTester {
	public boolean inRange(Vector reference, Vector other);
}

// TODO: implement and remove from this comment
//
//private boolean inRange_CUBE(Vector v) {
//	return (origin.getX() - maxDistance <= v.getX()) && (v.getX() <= origin.getX() + maxDistance)
//		&& (origin.getY() - maxDistance <= v.getY()) && (v.getY() <= origin.getY() + maxDistance)
//		&& (origin.getZ() - maxDistance <= v.getZ()) && (v.getZ() <= origin.getZ() + maxDistance);
//}
//
//private boolean inRange_MANHATTAN(Vector v) {
//	double xDist = Math.abs(origin.getX() - v.getX());
//	double yDist = Math.abs(origin.getY() - v.getY());
//	double zDist = Math.abs(origin.getZ() - v.getZ());
//	
//	return xDist + yDist + zDist <= maxDistance;
//}
//
//private boolean inRange_CYLINDER(Vector v) {
//	double xDist = origin.getX() - v.getX();
//	double zDist = origin.getZ() - v.getZ();
//	
//	return ((xDist * xDist) + (zDist * zDist) <= maxDistance * maxDistance)
//		 && (origin.getY() - maxDistance <= v.getY()) && (v.getY() <= origin.getY() + maxDistance);
//}