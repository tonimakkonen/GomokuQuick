package fi.tonimakkonen.hexutil;

import fi.tonimakkonen.gridutil.GridIndex;

/**
 * Class used to simplify calculations on a hex grid.
 */
public class HexCalc {

    // Generic math values //

    // Size of a single hex tile
    public static float tileHalfWidth = 1.0f / (float) Math.sqrt(3.0f);
    public static float tileFullWidth = 2.0f * tileHalfWidth;

    // Length of edges
    public static float tileEdgeLength = tileHalfWidth;

    // Separation in X direction
    public static float tileSeparationX = (float) Math.sqrt(3.0f) / 2.0f;

    // Numbers related to the grid
    public static float gridSizeX = 2.0f * tileSeparationX;
    public static float gridStartX = -(float) Math.sqrt(3.0f) / 6.0f;

    // Numbers related to hex tile shape
    public static float shapeX[] = {tileHalfWidth, 0.5f * tileHalfWidth, -0.5f * tileHalfWidth, -tileHalfWidth, -0.5f * tileHalfWidth, 0.5f * tileHalfWidth};
    public static float shapeY[] = {0.0f, 0.5f, 0.5f, 0.0f, -0.5f, -0.5f};

    // Tile positions //

    public static float getTileCenterX(int ix, int iy) {
        return tileHalfWidth + ix * tileSeparationX;
    }

    public static float getTileCenterY(int ix, int iy) {
        return 1.0f + (float) iy - 0.5f * (float) (ix % 2);
    }

    public static GridIndex getTileIndex(float px, float py) {

        // Even grid //

        // Position in tile space
        float tx1 = (px - gridStartX) / gridSizeX;
        float ty1 = (py - 0.5f);

        // index
        float ix1 = (float) Math.floor(tx1);
        float iy1 = (float) Math.floor(ty1);

        // distance from tile center
        float dx1 = tx1 - ix1 - 0.5f;
        float dy1 = ty1 - iy1 - 0.5f;
        float dis1 = dx1 * dx1 + dy1 * dy1;

        // Uneven grid //

        float tx2 = (px - tileHalfWidth) / gridSizeX;
        float ty2 = py;

        float ix2 = (float) Math.floor(tx2);
        float iy2 = (float) Math.floor(ty2);

        float dx2 = tx2 - ix2 - 0.5f;
        float dy2 = ty2 - iy2 - 0.5f;
        float dis2 = dx2 * dx2 + dy2 * dy2;

        // Select the correct grid based on distance
        if (dis1 < dis2) {
            return new GridIndex(((int) ix1) * 2, (int) iy1);
        } else {
            return new GridIndex(((int) ix2) * 2 + 1, (int) iy2);
        }
    }

    // Moving on hexagonal grid //

    public static void move(GridIndex location, int dir) {

        // What direction
        switch (dir) {

            case 0: // upright
                if (location.ix % 2 == 0) location.iy += 1;
                location.ix += 1;
                break;

            case 1: // up
                location.iy += 1;
                break;

            case 2: // upleft
                if (location.ix % 2 == 0) location.iy += 1;
                location.ix -= 1;
                break;

            case 3: // downleft
                if (location.ix % 2 != 0) location.iy -= 1;
                location.ix -= 1;
                break;

            case 4: // down
                location.iy -= 1;
                break;

            case 5: // downright
                if (location.ix % 2 != 0) location.iy -= 1;
                location.ix += 1;
                break;

            default:
                break;
        }
    }

}
