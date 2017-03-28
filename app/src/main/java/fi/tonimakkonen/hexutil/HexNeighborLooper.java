package fi.tonimakkonen.hexutil;

import fi.tonimakkonen.gridutil.GridIndex;

/**
 * A class used to loop neighbors on a hexagonal grid.
 */
public class HexNeighborLooper {

    public static int oddDeltaX[] = {1, 0, -1, -1, 0, 1};
    public static int oddDeltaY[] = {0, 1, 0, -1, -1, -1};

    public static int evenDeltaX[] = {1, 0, -1, -1, 0, 1};
    public static int evenDeltaY[] = {1, 1, 1, 0, -1, 0};

    private int usedDeltaX[];
    private int usedDeltaY[];

    private GridIndex start;
    private GridIndex current;

    private int neighbor;

    public HexNeighborLooper(int ix, int iy) {

        // Set start location
        start = new GridIndex(ix, iy);

        // Is this an even tile
        if (ix % 2 == 0) {
            usedDeltaX = evenDeltaX;
            usedDeltaY = evenDeltaY;
        } else {
            usedDeltaX = oddDeltaX;
            usedDeltaY = oddDeltaY;
        }

        neighbor = 0;
        current = new GridIndex(start.ix + usedDeltaX[0], start.iy + usedDeltaY[0]);
    }

    public GridIndex getNeighbor() {
        return current;
    }

    public void next() {
        neighbor += 1;
        if (neighbor == 6) neighbor -= 6;

        current.ix = start.ix + usedDeltaX[neighbor];
        current.iy = start.iy + usedDeltaY[neighbor];
    }


}
