package fi.tonimakkonen.gridutil;

public class GridCalc {
	
	// Moving on normal grid //
	
	public static void move(GridIndex location, int dir) {
		
		switch(dir) {
		
		case 0:
			location.ix += 1;
			break;
			
		case 1:
			location.ix += 1;
			location.iy += 1;
			break;
			
		case 2:
			location.iy += 1;
			break;
			
		case 3:
			location.ix -= 1;
			location.iy += 1;
			break;
			
		case 4:
			location.ix -= 1;
			break;
			
		case 5:
			location.ix -= 1;
			location.iy -= 1;
			break;
			
		case 6:
			location.iy -= 1;
			break;
			
		case 7:
			location.ix += 1;
			location.iy -= 1;
		}
	}

}
