package fi.tonimakkonen.gridutil;

/**
 * Grid index on a standard tile or a hexagona tile
 */
public class GridIndex {

	public int ix;
	public int iy;
	
	public GridIndex() {
		ix = 0;
		iy = 0;
	}
	
	public GridIndex(int sx, int sy) {
		ix = sx;
		iy = sy;
	}
	
}
