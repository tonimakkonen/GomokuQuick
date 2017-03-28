package fi.tonimakkonen.gomoku;

import android.content.SharedPreferences;

import java.util.Scanner;

import fi.tonimakkonen.gridutil.GridCalc;
import fi.tonimakkonen.gridutil.GridIndex;
import fi.tonimakkonen.hexutil.HexCalc;
import fi.tonimakkonen.hexutil.HexNeighborLooper;

/**
 * The class containing the state of the Gomoku game, the rule set, the used board, and the moves.
 * This class also contains a lot of helper functions that are used by the AI.
 */
public class GoGame implements Cloneable {

    // what type of gomoku game?
    int gameRules; // rules? standard, freestyle, 6-in-a-row
    int gameBoard; // normal, hex?

    // status of board (employ primitive data types for max speed, AI)
    int status[] = new int[GoSettings.goMaxSize];  // current status of the board
    int history[] = new int[GoSettings.goMaxSize]; // history of play (back & possibly forward)
    int historyLen = 0;            // total length of history
    int historyCur = 0;            // current location in history (typically same as len)

    // utilities related to the board
    int neighbor1[] = new int[GoSettings.goMaxSize]; // How many neighboring tiles
    int neighbor2[] = new int[GoSettings.goMaxSize]; // How many neigh. tiles in 2 squares

    // win status (-1 = tie, 0 = no win, 1 = X win, 2 = O win)
    int winStatus = 0;

    // winning set of tiles
    int winTile[] = new int[GoSettings.goMaxSize];

    // count of various "objects" on game board
    // 1, 2, 3, 4, 5, over 5 in a row [6]
    // free, capped, bounded [3]
    // X, O [2]
    int objectCount[][][] = new int[6][3][2];

    // List of predetrmined rows rows
    // This is board specific
    int rowList[][];

    // From tile to row, board specific
    int tileRow[][];

    // init class
    private GoGame() {

    }

    public GoGame(int rules, int board) {
        gameRules = rules;
        gameBoard = board;

        // Calculate the rows
        initRows();
    }

    // Create the rows
    private void initRows() {

        // Normal square board
        if (gameBoard == 0) {

            // from every tile to the four rows
            tileRow = new int[GoSettings.goNormalSize][4];

            // Init the row list for a standard board
            // hor. + vert. + two diagonals
            rowList = new int[GoSettings.goNormalSide * 2 + (GoSettings.goNormalSide * 2 - 1) * 2][];
            int rowIndex = 0;

            // horizontal rows (trivial)
            for (int i = 0; i < GoSettings.goNormalSide; i++) {
                rowList[rowIndex] = new int[GoSettings.goNormalSide];
                for (int j = 0; j < GoSettings.goNormalSide; j++) {

                    int index = i * GoSettings.goNormalSide + j;

                    rowList[rowIndex][j] = index;
                    tileRow[index][0] = rowIndex;

                }
                rowIndex += 1;
            }

            // vertical rows (trivial)
            for (int i = 0; i < GoSettings.goNormalSide; i++) {
                rowList[rowIndex] = new int[GoSettings.goNormalSide];
                for (int j = 0; j < GoSettings.goNormalSide; j++) {

                    int index = i + j * GoSettings.goNormalSide;

                    rowList[rowIndex][j] = index;
                    tileRow[index][1] = rowIndex;

                }
                rowIndex += 1;
            }

            // diagnoal 1
            for (int i = 0; i < GoSettings.goNormalSide * 2 - 1; i++) {

                // Length of row, indexing
                int rowLen = i + 1;
                int border = i;
                if (rowLen > GoSettings.goNormalSide) {
                    rowLen = 2 * GoSettings.goNormalSide - rowLen;
                    border = (i - GoSettings.goNormalSide + 2) * GoSettings.goNormalSide - 1;
                }
                rowList[rowIndex] = new int[rowLen];

                // what direction to go
                int delta = -1 + GoSettings.goNormalSide;

                // set values
                for (int j = 0; j < rowLen; j++) {

                    int index = border + j * delta;

                    rowList[rowIndex][j] = index;
                    tileRow[index][2] = rowIndex;
                }

                rowIndex += 1;
            }

            // diagonal 2
            for (int i = 0; i < GoSettings.goNormalSide * 2 - 1; i++) {

                // Length of row, indexing
                int rowLen = i + 1;
                int border = GoSettings.goNormalSide - 1 - i;
                if (rowLen > GoSettings.goNormalSide) {
                    rowLen = 2 * GoSettings.goNormalSide - rowLen;
                    border = (i - GoSettings.goNormalSide + 1) * GoSettings.goNormalSide;
                }
                rowList[rowIndex] = new int[rowLen];

                // what direction to go
                int delta = +1 + GoSettings.goNormalSide;

                // set values
                for (int j = 0; j < rowLen; j++) {

                    int index = border + j * delta;

                    rowList[rowIndex][j] = index;
                    tileRow[index][3] = rowIndex;
                }

                rowIndex += 1;
            }

        }
        // Hexagonal rows
        else if (gameBoard == 1) {

            // from every tile to the three rows
            tileRow = new int[GoSettings.goNormalSize][3];

            // How many rows?
            rowList = new int[GoSettings.goNormalSide + 2 * (GoSettings.goNormalSide + (GoSettings.goNormalSide - 1) / 2)][];
            int rowIndex = 0;

            // vertical rows (trivial)
            for (int i = 0; i < GoSettings.goNormalSide; i++) {

                rowList[rowIndex] = new int[GoSettings.goNormalSide];

                for (int j = 0; j < GoSettings.goNormalSide; j++) {

                    int index = i + j * GoSettings.goNormalSide;

                    rowList[rowIndex][j] = index;
                    tileRow[index][0] = rowIndex;

                }
                rowIndex += 1;
            }

            // do both diagonal at the same time
            for (int i = 0; i < GoSettings.goNormalSide + (GoSettings.goNormalSide - 1) / 2; i++) {

                // Starting index for both types of rows..
                GridIndex start1 = new GridIndex();
                GridIndex start2 = new GridIndex();

                if (i < GoSettings.goNormalSide) {
                    start1.ix = GoSettings.goNormalSide - 1;
                    start1.iy = i;

                    start2.ix = 0;
                    start2.iy = i;
                } else {
                    start1.ix = (i - GoSettings.goNormalSide) * 2;
                    start1.iy = GoSettings.goNormalSide - 1;

                    start2.ix = (i - GoSettings.goNormalSide + 1) * 2;
                    start2.iy = GoSettings.goNormalSide - 1;
                }

                // Get the length of both lines
                boolean onGrid = true;

                int length1 = 0;
                GridIndex runner1 = new GridIndex(start1.ix, start1.iy);
                while (onGrid) {

					/*
					if(rowIndex == 19) {
						Log.d("gomoku","runner1 = " + runner1.ix + ", " + runner1.iy);
					}
					*/

                    length1 += 1;
                    HexCalc.move(runner1, 3);
                    if (runner1.ix < 0 || runner1.iy < 0) onGrid = false;
					
					/*
					if(rowIndex == 19) {
						Log.d("gomoku","      --> " + runner1.ix + ", " + runner1.iy);
					}
					*/
                }

                onGrid = true;

                int length2 = 0;
                GridIndex runner2 = new GridIndex(start2.ix, start2.iy);
                while (onGrid) {
                    length2 += 1;
                    HexCalc.move(runner2, 5);
                    if (runner2.ix >= GoSettings.goNormalSide || runner2.iy < 0) onGrid = false;
                }

                // Create first line
                rowList[rowIndex] = new int[length1];
                runner1.ix = start1.ix;
                runner1.iy = start1.iy;
                for (int j = 0; j < length1; j++) {

                    int index = runner1.ix + runner1.iy * GoSettings.goNormalSide;

                    rowList[rowIndex][j] = index;
                    tileRow[index][1] = rowIndex;

                    HexCalc.move(runner1, 3);
                }
                rowIndex += 1;

                // Create second line
                rowList[rowIndex] = new int[length2];
                runner2.ix = start2.ix;
                runner2.iy = start2.iy;
                for (int j = 0; j < length2; j++) {

                    int index = runner2.ix + runner2.iy * GoSettings.goNormalSide;

                    rowList[rowIndex][j] = index;
                    tileRow[index][2] = rowIndex;

                    HexCalc.move(runner2, 5);
                }
                rowIndex += 1;

            }

        } else {
            // Serious error!!
            // Wrong gameBoard
        }
    }

    // Clone this game (useful when passing to AI thread)
    public GoGame clone() {

        // Note that we create the game with private constructor
        // Do not recalculate everything, just copy..
        GoGame ng = new GoGame();

        ng.gameRules = gameRules;
        ng.gameBoard = gameBoard;

        for (int i = 0; i < GoSettings.goMaxSize; i++) ng.status[i] = status[i];
        for (int i = 0; i < GoSettings.goMaxSize; i++) ng.history[i] = history[i];
        for (int i = 0; i < GoSettings.goMaxSize; i++) ng.neighbor1[i] = neighbor1[i];
        for (int i = 0; i < GoSettings.goMaxSize; i++) ng.neighbor2[i] = neighbor2[i];

        ng.historyLen = historyLen;
        ng.historyCur = historyCur;
        ng.winStatus = winStatus;

        for (int i = 0; i < GoSettings.goMaxSize; i++) ng.winTile[i] = winTile[i];

        for (int i = 0; i < objectCount.length; i++) {
            for (int j = 0; j < objectCount[i].length; j++) {
                for (int k = 0; k < objectCount[i][j].length; k++) {
                    ng.objectCount[i][j][k] = objectCount[i][j][k];
                }
            }
        }

        ng.rowList = new int[rowList.length][];
        for (int i = 0; i < rowList.length; i++) {
            ng.rowList[i] = new int[rowList[i].length];
            for (int j = 0; j < rowList[i].length; j++) {
                ng.rowList[i][j] = rowList[i][j];
            }
        }

        ng.tileRow = new int[tileRow.length][];
        for (int i = 0; i < tileRow.length; i++) {
            ng.tileRow[i] = new int[tileRow[i].length];
            for (int j = 0; j < tileRow[i].length; j++) {
                ng.tileRow[i][j] = tileRow[i][j];
            }
        }

        return ng;
    }

    // Count the number of objects in a row
    // This function is rule-board-independent
    private void rowCount(int[] row, int add) {

        // current group
        int last = -1; // we start outside
        int count = 0;
        boolean cap = true;

        // Go trough every tile in the row (notice how we go one beyond, it's a -1)
        for (int rowindex = 0; rowindex < row.length + 1; rowindex++) {

            // Current tile mark?
            int current = -1;
            if (rowindex < row.length) {
                int index = row[rowindex];
                current = status[index];
            }

            if (current == last) {
                count += 1;
            } else {
                // Should we add this group?
                if (last > 0) {

                    // count index
                    int countIndex = count - 1;
                    if (countIndex > 5) countIndex = 5;

                    // capped index
                    int capIndex = 0;
                    if (cap) capIndex += 1;
                    if (current != 0) capIndex += 1;

                    // mark index
                    int markIndex = last - 1;

                    // Add to count
                    objectCount[countIndex][capIndex][markIndex] += add;

                    // Is this a winning object?
                    if ((gameRules == 0 && count == 5) ||                   // standard rules
                            (gameRules == 1 && count == 5 && capIndex == 0) ||  // caro (gomoku+)
                            (gameRules == 2 && count >= 6)) {                  // 6 in a row

                        for (int j = 1; j <= count; j++) {
                            int winIndex = row[rowindex - j];
                            winTile[winIndex] += add;
                        }

                    }

                }

                // start of next group
                count = 1;
                if (last == 0) cap = false;
                else cap = true;
            }

            last = current;

        }

    }

    // Do the full count
    // This function is rule-game-independent
    private void fullCount() {
        // Go trough every row, just add the number of different ovject to the list
        for (int i = 0; i < rowList.length; i++) {
            rowCount(rowList[i], +1);
        }

        // Plot the debug value
		/*
		for(int i = 0; i < objectCount.length; i++) {
			for(int j = 0; j < objectCount[i].length; j++) {
				for(int k = 0; k < objectCount[i][j].length; k++) {
					if(objectCount[i][j][k] != 0) {
						Log.d("fullCount", objectCount[i][j][k] +": "  + "len = " + (i+1) + ", cap = " + j + ", mark = " + (k+1));
					}
				}
			}
			
		}
		*/

    }

    // Calculate the "value", used in minmax
    // TODO: Make this much, much better!!
    // This function is rule specific
    // This function is board independent
    public int value(int ai, int defence) {

        // First, check for winnning conditions
        // If so, return a very high (or low) value
        // Do not return MAX_VALUE or MIN_VALUE so you can safely take multiply by -1.
        int winCond = getWin();
        if (winCond == 1) {
            // Max value = 2^32 - 1, make it even
            return (Integer.MAX_VALUE - 1023);
        } else if (winCond == 2) {
            // Min Value = -2^32
            return (Integer.MIN_VALUE + 1024);
        }

        // No win, calculate the score..

        // get active player (value depends on that)
        int markIndex = getActivePlayer() - 1;

        //

        // positive value is good for mark X, negative for O

        int retval = 0;
        int multLen = 1;

        // Length of "object", skip ones
        for (int i = 1; i < 6; i++) {

            // Free, capped, bounded
            for (int j = 0; j < 3; j++) {

                int multCap = 1;
                if (j == 0) multCap = 5; // uncapped more valuable
                if (j == 1) multCap = 1;
                if (j == 2 && i < 4) multCap = 0;

                // Player index
                for (int k = 0; k < 2; k++) {

                    int markMult;
                    if (k == 0) markMult = 1;
                    else markMult = -1;

                    // value more on turn
                    if (markIndex == k) markMult *= 10;

                    retval += multLen * multCap * markMult * objectCount[i][j][k];


                }


            }

            // increase length by one, increase value by 100
            multLen *= 100;
        }

        return retval;
    }

    // Check if winning conditions have been achieved
    // -1 = tie (all occupied & no win => tie), 0 = no win, 1 = mark X, 2 = mark O
    private int checkWin() {

        // Standard gomoku
        // Free, capped, and bound fives are a win (6 or higher are not a win)
        if (gameRules == 0) {
            for (int markIndex = 0; markIndex < 2; markIndex++) {
                if (objectCount[4][0][markIndex] >= 1 || objectCount[4][1][markIndex] >= 1 || objectCount[4][2][markIndex] >= 1) {
                    return markIndex + 1;
                }
            }
        }

        // Caro (gomoku plus)
        // Only free fives are a win
        if (gameRules == 1) {
            for (int markIndex = 0; markIndex < 2; markIndex++) {
                if (objectCount[4][0][markIndex] >= 1) {
                    return markIndex + 1;
                }
            }
        }

        // 6 in a row
        // 6 in a row or higher, free, capped, or bound are considered a win
        if (gameRules == 2) {
            for (int markIndex = 0; markIndex < 2; markIndex++) {
                if (objectCount[5][0][markIndex] >= 1 || objectCount[5][1][markIndex] >= 1 || objectCount[5][2][markIndex] >= 1) {
                    return markIndex + 1;
                }
            }
        }

        // No win found
        // Check if board is full
        if (historyCur >= GoSettings.goNormalSize) {
            return -1;
        }

        // no win, no tie
        return 0;
    }

    // Get the win status of the game
    public int getWin() {
        return winStatus;
    }

    // Get the winning row
    public int[] getWinRow() {

        // How many tiles?
        int count = 0;
        for (int i = 0; i < GoSettings.goNormalSize; i++) {
            if (winTile[i] != 0) count += 1;
        }

        // If none?
        if (count == 0) return null;

        // Create the winning row
        int wr[] = new int[count];
        count = 0;
        for (int i = 0; i < GoSettings.goNormalSize; i++) {
            if (winTile[i] != 0) {
                wr[count] = i;
                count += 1;
            }
        }

        return wr;
    }

    // Length of current history = number of marks on the board
    public int numMarks() {
        return historyCur;
    }

    // Get the last tile
    public int lastTile() {
        if (historyCur == 0) return -1;
        return history[historyCur - 1];
    }

    // Can we undo & redo moves
    public boolean canUndo() {
        return historyCur > 0;
    }

    public boolean canRedo() {
        return historyCur < historyLen;
    }

    // Undo and redo moves
    public void undo() {

        // If we are at beginning, do nothing
        if (historyCur == 0) return;

        // We act on this tile
        int tile = history[historyCur - 1];

        // Remove objects on rows associated with this tile
        for (int i = 0; i < tileRow[tile].length; i++) rowCount(rowList[tileRow[tile][i]], -1);
        // remove neighbors
        addNeighbor(tile, -1);

        // Update board status
        status[tile] = 0;

        // recalculate objects
        for (int i = 0; i < tileRow[tile].length; i++) rowCount(rowList[tileRow[tile][i]], +1);

        // Move history one down
        historyCur -= 1;

        // board status changed, update win conditions
        winStatus = checkWin();
    }

    public void redo() {

        // If we are at end..
        if (historyCur == historyLen) return;

        // We act on this tile
        int tile = history[historyCur];

        // Remove objects on rows associated with this tile
        for (int i = 0; i < tileRow[tile].length; i++) rowCount(rowList[tileRow[tile][i]], -1);

        // update board status
        status[tile] = getActivePlayer();

        // update neighbors
        addNeighbor(history[historyCur], +1);
        // recalculate objects
        for (int i = 0; i < tileRow[tile].length; i++) rowCount(rowList[tileRow[tile][i]], +1);

        // Move up in history
        historyCur += 1;

        // board status changed, update win conditions etc.
        winStatus = checkWin();
    }

    // Get the player who will add the next piece
    public int getActivePlayer() {
        if (historyCur % 2 == 0) return 1; // 0, 2, 4...
        else return 2;
    }

    // Add a X or O, depending on active player, if tile is empty
    public boolean addMark(int ti) {

        // This is assuming normal gomoku

        // Do not add outside map
        if (ti < 0 || ti >= GoSettings.goNormalSize) return false;
        // Do not add to occupied squares
        if (status[ti] != 0) return false;

        // Now, we are ready to add a new mark
        // Get player
        int player = getActivePlayer();

        // Remove objects on rows associated with this tile
        for (int i = 0; i < tileRow[ti].length; i++) rowCount(rowList[tileRow[ti][i]], -1);

        // Change status of board
        status[ti] = player;
        // Update neighbors
        addNeighbor(ti, +1);

        // Change history
        // Notice how this will cut of future history
        history[historyCur] = ti;
        historyCur += 1;
        historyLen = historyCur;

        // Update objects
        for (int i = 0; i < tileRow[ti].length; i++) rowCount(rowList[tileRow[ti][i]], +1);

        // board status changed, update win conditions etc.
        winStatus = checkWin();

        return true;
    }

    // Neighboring tiles, AI helpers //

    private void buildNeighbors() {
        // start by resetting neighbprs
        for (int i = 0; i < neighbor1.length; i++) {
            neighbor1[i] = 0;
            neighbor2[i] = 0;
        }
        // Add neighbors
        for (int i = 0; i < GoSettings.goNormalSize; i++) {
            if (status[i] != 0) addNeighbor(i, +1);
        }
    }

    private void addNeighbor(int ti, int val) {

        // Do not check parameter for max speed, private functions

        // Get x and y from tile indes
        int x = ti % GoSettings.goNormalSide;
        int y = (ti - x) / GoSettings.goNormalSide;

        // Neighbor indeces are board specific
        if (gameBoard == 0) {

            int x0 = x - 1;
            if (x0 < 0) x0 = 0;
            int x1 = x + 1;
            if (x1 >= GoSettings.goNormalSide) x1 = GoSettings.goNormalSide - 1;
            int y0 = y - 1;
            if (y0 < 0) y0 = 0;
            int y1 = y + 1;
            if (y1 >= GoSettings.goNormalSide) y1 = GoSettings.goNormalSide - 1;

            for (int ix = x0; ix <= x1; ix++) {
                for (int iy = y0; iy <= y1; iy++) {
                    int tile = ix + iy * GoSettings.goNormalSide;
                    neighbor1[tile] += val;
                }
            }

        } else if (gameBoard == 1) {
            HexNeighborLooper nb = new HexNeighborLooper(x, y);
            for (int i = 0; i < 6; i++) {

                GridIndex gi = nb.getNeighbor();
                if (gi.ix >= 0 && gi.iy >= 0 && gi.ix < GoSettings.goNormalSide && gi.iy < GoSettings.goNormalSide) {
                    int tile = gi.ix + gi.iy * GoSettings.goNormalSide;
                    neighbor1[tile] += val;
                }

                nb.next();
            }
        }

    }

    // Create a list of possible moves
    // Returns the tile index
    public int[] createMoveList(int size) {

        // The board is empty of marks, return a random away from the edges
        if (historyCur == 0) {
            int retval[] = new int[1];

            // This assumes GoSettings.goNormalSide > 10
            int x = 5 + (int) Math.floor(Math.random() * (GoSettings.goNormalSide - 10));
            int y = 5 + (int) Math.floor(Math.random() * (GoSettings.goNormalSide - 10));

            retval[0] = x + y * GoSettings.goNormalSide;
            return retval;
        }

        // The board has marks on it..

        // Count loop & add loop
        int count = 0;
        int retval[] = null;

        int used[] = new int[GoSettings.goMaxSize];

        // Two loops, one counts potential moves, the other one list them
        for (int loopInd = 0; loopInd < 2; loopInd++) {

            // We only look close to recent additions
            for (int indRecent = 0; indRecent < 2; indRecent++) {

                // Make sure we have enough history
                if (historyCur <= indRecent) break;

                int latest = history[historyCur - 1 - indRecent];
                int latestX = latest % GoSettings.goNormalSide;
                int latestY = (latest - latestX) / GoSettings.goNormalSide;

                GridIndex cur = new GridIndex();
                int dirNum = 0;
                if (gameBoard == 0) dirNum = 8;
                else dirNum = 6;

                for (int dir = 0; dir < dirNum; dir++) {

                    cur.ix = latestX;
                    cur.iy = latestY;

                    for (int dist = 1; dist < 6; dist++) {
                        if (gameBoard == 0) GridCalc.move(cur, dir);
                        else HexCalc.move(cur, dir);

                        if (cur.ix < 0 || cur.iy < 0 || cur.ix >= GoSettings.goNormalSide || cur.iy >= GoSettings.goNormalSide) {
                            break;
                        }

                        // Check if this tile is eligable
                        int index = cur.ix + cur.iy * GoSettings.goNormalSide;

                        if (neighbor1[index] > 0 && status[index] == 0 && used[index] == 0) {
                            if (loopInd == 0) {
                                used[index] = 1;
                                count += 1;
                            } else {
                                used[index] = 1;
                                retval[count] = index;
                                count += 1;
                            }
                        }

                    }
                }
            } // for(indRecent = 0...

            // Then, check everything else..
            if (size > 0) {
                for (int i = 0; i < GoSettings.goMaxSize; i++) {
                    if (neighbor1[i] > 0 && status[i] == 0 && used[i] == 0) {
                        if (loopInd == 0) {
                            used[i] = 1;
                            count += 1;
                        } else {
                            used[i] = 1;
                            retval[count] = i;
                            count += 1;
                        }
                    }
                }
            }

            if (loopInd == 0) {
                retval = new int[count];
                count = 0;
                for (int k = 0; k < used.length; k++) used[k] = 0;
            }
        }


        return retval;
    }

    //                           //
    // Saving & loading settings //
    //                           //

    // Load the state of the game
    public void loadState(SharedPreferences prefs) {

        // Get the type of the game
        gameRules = prefs.getInt("gogame_type", 0);
        gameBoard = prefs.getInt("gogame_board", 0);

        // Generate the rows based on gameBoard
        initRows();

        // Load the history length and current locatin in history
        historyLen = prefs.getInt("gogame_histlen", 0);
        historyCur = prefs.getInt("gogame_histcur", 0);

        // Load & interpret the history string
        String hstr = prefs.getString("gogame_hist", "");
        Scanner scan = new Scanner(hstr);
        for (int i = 0; i < historyLen; i++) {
            history[i] = scan.nextInt();
        }

        // Build up rest of game state from history
        int player = 1;
        for (int i = 0; i < historyCur; i++) {
            status[history[i]] = player;
            if (player == 1) player = 2;
            else player = 1;
        }

        // Build neighbors
        buildNeighbors();

        // Get the ful count
        fullCount();

        // status of board has changed, update status & value
        winStatus = checkWin();

    }

    // Save the state of the game
    public void saveState(SharedPreferences.Editor edit) {

        // Save game type
        edit.putInt("gogame_type", gameRules);
        edit.putInt("gogame_board", gameBoard);

        // Only save the history, everything can be built from that..
        edit.putInt("gogame_histlen", historyLen);
        edit.putInt("gogame_histcur", historyCur);

        // create a string from history
        String hstr = new String();
        for (int i = 0; i < historyLen; i++) {
            hstr += String.format("%d ", history[i]);
        }

        // save history
        edit.putString("gogame_hist", hstr);
    }

}
