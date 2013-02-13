package cbx;

import com.sk89q.craftbook.BlockArea;
import com.sk89q.craftbook.Vector;

public class CBXinRangeBlockArea extends CBXinRangeCuboid {
	
	public CBXinRangeBlockArea(BlockArea bArea) {
		super((Math.abs(bArea.getX() - bArea.getX2()) + 1) / 2.0,
			(Math.abs(bArea.getY() - bArea.getY2()) + 1) / 2.0,
			(Math.abs(bArea.getZ() - bArea.getZ2()) + 1) / 2.0);
	}
	
	public static Vector getSearchCenter(BlockArea bArea) {
		int minX = Math.min(bArea.getX(), bArea.getX2());
		int minY = Math.min(bArea.getY(), bArea.getY2());
		int minZ = Math.min(bArea.getZ(), bArea.getZ2());
		int Xsize = Math.abs(bArea.getX() - bArea.getX2()) + 1; 
		int Ysize = Math.abs(bArea.getY() - bArea.getY2()) + 1; 
		int Zsize = Math.abs(bArea.getZ() - bArea.getZ2()) + 1; 
		return new Vector(minX + (Xsize / 2.0), minY + (Ysize / 2.0), minZ + (Zsize / 2.0));
	}
	
	public static double getSearchDistance(BlockArea bArea) {
		int Xsize = Math.abs(bArea.getX() - bArea.getX2()) + 1; 
		int Zsize = Math.abs(bArea.getZ() - bArea.getZ2()) + 1; 
		return Math.max(Xsize / 2.0, Zsize / 2.0);
	}

}
