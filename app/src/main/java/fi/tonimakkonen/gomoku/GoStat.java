package fi.tonimakkonen.gomoku;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.res.Resources;


public class GoStat extends SQLiteOpenHelper {
	
	// SQLite database related
    private static final int    DATABASE_VERSION = 1;               // version
    
    private static final String DATABASE_NAME    = "Statistics";    // name

    private static final String TABLE_PAIRS      = "pairs";         // one table (list of pairs)
    
    private static final String KEY_ID           = "_id";           // id of entry
    private static final String KEY_CURRENT      = "current";       // current index (complex variable..)
    private static final String KEY_ANAME        = "aname";         // name of player a
    private static final String KEY_BNAME        = "bname";         // name of player b
    private static final String KEY_BAI          = "bai";           // player b ai level
    
    private static final String KEY_RULES        = "rules";         // "rules" only used by latest & current records
    private static final String KEY_BOARD        = "board";         // "board" only used by latest & current records

    private String[][][][] resKey = new String[GoSettings.numRules][GoSettings.numBoards][2][3];         // resuts..


	// A pair of players
    // This forms and entry in the table
	private class PlayerPair {
		
		// Id of this pair (used by SQLite)
		int id;
		
		// Current game flag
		int current;
				
		// Name of players
		// "A" does not necessarily refer to actual A player. "foo vs. bar" is treated as "bar vs. foo"
		String aName = new String();
		String bName = new String();
		// Player B AI level
		int bAI;
		
		// Rules & board (only used by latest game)
		int rules;
		int board;
		
		// Results for this player pair
		int res[][][][] = new int[3][2][2][3];
		
		// Set all game results to zero (used for "latest" and current games)
		public void setResToZero() {
			for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
	    		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
	    			for(int iStart = 0; iStart < 2; iStart += 1) {
	    				for(int iWin = 0; iWin < 3; iWin += 1) {
	    					res[iRules][iBoard][iStart][iWin] = 0;
	    				}
	    			}
	    		}
			}
		}
		
		// Add (or remove) a game result
		public void add(int rules, int board, int aMark, int win, int val) {
			
			// Turn win indicator to win index
			int winIndex = 0;
			if(win == 1) { // X wins
				if(aMark == 1) winIndex = 0; // A is X
				else if(aMark == 2) winIndex = 1; // A is O, B wins
				else {
					Log.e("gomoku", "error in PlayerPair.add (winIdex #1), aMark = " + aMark + ", win = " + win);
				}
			}
			else if(win == 2) { // O wins
				if(aMark == 1) winIndex = 1; // A is X, O wins
				else if(aMark == 2) winIndex = 0; // A is O
				else {
					Log.e("gomoku", "error in PlayerPair.add (winIdex #2), aMark = " + aMark + ", win = " + win);
				}
			}
			else if(win == -1) winIndex = 2; // ties are ease
			else {
				Log.e("gomoku", "error in PlayerPair.add (winIdex #3), aMark = " + aMark + ", win = " + win);
			}
			
			// Starting index
			int startIndex = 0;
			if(aMark == 1) startIndex = 0; // A plays X, A starts
			else if(aMark == 2) startIndex = 1; // A plays O, B starts
			else {
				Log.e("gomoku", "error in PlayerPair.add (startIndex), aMark = " + aMark + ", win = " + win);
			}
			
			// Make sure we have the right set of rules
			if(rules < 0 || rules >= GoSettings.numRules) {
				Log.e("gomoku", "error in PlayerPair.add (rules), aMark = " + aMark + ", win = " + win + ", rules = " + rules);
				rules = 0;
			}
			
			// Make sure we have the right board
			if(board < 0 || board >= GoSettings.numBoards) {
				Log.e("gomoku", "error in PlayerPair.add (board), aMark = " + aMark + ", win = " + win + ", board = " + board);
				board = 0;
			}
			
			//Log.d("gomoku", "rules = " + rules + ", board = " + board + ", startIndex = " + startIndex + ", winIndex = " + winIndex);
			
			res[rules][board][startIndex][winIndex] += val;
			
			// Make sure we don't go below zero
			// This should never happen, just make sure..
			if(res[rules][board][startIndex][winIndex] < 0) res[rules][board][startIndex][winIndex] = 0;
		}
		
		// This function is used when player A in curent game is player B in long time records
		void addOpposite(int rules, int board, int aMark, int win, int val) {
			
			// Player A Mark
			int am = 1;
			if(aMark == 1) am = 2;
			else if(aMark == 2) am = 1;
			else {
				Log.e("gomoku", "error in PlayerPair.addOpposite (aMark), aMark = " + aMark + ", win = " + win);
			}
			
			// win status
			int w = 1;
			if(win == 1) w = 2;
			else if(win == 2) w= 1;
			else if(win == -1) w = -1;
			else {
				Log.e("gomoku", "error in PlayerPair.add (win), aMark = " + aMark + ", win = " + win);
			}
			
			add(rules, board, am, w, val);
		}
	
		// Combine results
		void addPair(PlayerPair p, int mult) {
			for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
	    		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
	    			for(int iStart = 0; iStart < 2; iStart += 1) {
	    				for(int iWin = 0; iWin < 3; iWin += 1) {
	    					res[iRules][iBoard][iStart][iWin] += mult*p.res[iRules][iBoard][iStart][iWin];
	    					
	    					// Make sure we never go below zero (should never happen)
	    					if(res[iRules][iBoard][iStart][iWin] < 0) res[iRules][iBoard][iStart][iWin] = 0;
	    				}
	    			}
	    		}
			}
		}
		
		//
		void addOppositePair(PlayerPair p, int mult) {
			for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
	    		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
	    			for(int iStart = 0; iStart < 2; iStart += 1) {
	    				for(int iWin = 0; iWin < 3; iWin += 1) {
	    					
	    					int is2 = 0;
	    					if(iStart == 0) is2 = 1;
	    					else is2 = 0;
	    					
	    					int iw2 = 0;
	    					if(iWin == 0) iw2 = 1;
	    					else if(iWin == 1) iw2 = 0;
	    					else if(iWin == 2) iw2 = 2;
	    					
	    					res[iRules][iBoard][iStart][iWin] += mult*p.res[iRules][iBoard][is2][iw2];
	    					
	    					// Make sure we never go below zero (should never happen)
	    					if(res[iRules][iBoard][iStart][iWin] < 0) res[iRules][iBoard][iStart][iWin] = 0;
	    				}
	    			}
	    		}
			}
		}
		
		
		
		// Sum the results
		int[] getWins(int rules, int board, int start) {
			
			// rules loop
			int r1 = 0, r2 = 0;
			if(rules >= 0 && rules < GoSettings.numRules) {
				r1 = rules;
				r2 = rules;
			}
			else if(rules == -1) {
				r1 = 0;
				r2 = GoSettings.numRules - 1;
			}
			
			// board loop
			int b1 = 0, b2 = 0;
			if(board >= 0 && board < GoSettings.numBoards) {
				b1 = board;
				b2 = board;
			}
			else if(board == -1) {
				b1 = 0;
				b2 = GoSettings.numBoards - 1;
			}
			
			// Start loop
			int s1 = 0, s2 = 1;
			if(start >= 0) {
				s1 = start;
				s2 = start;
			}
			
			// gather res
			int retval[] = new int[3];
			
			for(int iR = r1; iR <= r2; iR++) {
				for(int iB = b1; iB <= b2; iB++) {
					for(int iS = s1; iS <= s2; iS++) {
						for(int iW = 0; iW < 3; iW++) {
							retval[iW] += res[iR][iB][iS][iW]; 
						}
					}
				}
			}
			
			// Make sure we always get positive values (should not be a problem)
			for(int i = 0; i < 3; i++) if(retval[i] < 0) retval[i] = 0;
			
			return retval;
			
		}
	}
	
    public GoStat(Context context) {
    	
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    	
    	// create results keys
    	for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
    		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
    			for(int iStart = 0; iStart < 2; iStart += 1) {
    				for(int iWin = 0; iWin < 3; iWin += 1) {
    					resKey[iRules][iBoard][iStart][iWin] = "key"
    							+ "_rules" + iRules + "board" + iBoard
    							+ "start" + iStart + "win" + iWin;
    				}
    			}
    		}
    	}
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
 
		// Create the command string
		String CREATE_TABLE = "CREATE TABLE " + TABLE_PAIRS + 
				" ( " +
                KEY_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_CURRENT + " INTEGER, " +
                KEY_ANAME   + " TEXT, " +
                KEY_BNAME   + " TEXT, " +
                KEY_BAI     + " INTEGER, " + 
                KEY_RULES   + " INTEGER, " +
                KEY_BOARD   + " INTEGER"; // note missing , inserted in next loop
		
		for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
    		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
    			for(int iStart = 0; iStart < 2; iStart += 1) {
    				for(int iWin = 0; iWin < 3; iWin += 1) {
    					CREATE_TABLE += ", " + resKey[iRules][iBoard][iStart][iWin] + " INTEGER";
    				}
    			}
    		}
		}
		
		CREATE_TABLE += " ) ";
		
        // create the table
        db.execSQL(CREATE_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAIRS);
 
        // create fresh books table
        this.onCreate(db);
		
	}
	
	// Read and write operations
	
	private List<PlayerPair> getAllRecords() {
		
		List<PlayerPair> ret = new LinkedList<PlayerPair>();
		 
        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_PAIRS;
 
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        // 3. go over each row, build book and add it to list
        PlayerPair pair = null;
        if (cursor.moveToFirst()) {
            do {
                pair = new PlayerPair();
                pair.id      = Integer.parseInt(cursor.getString(0));
                pair.current = Integer.parseInt(cursor.getString(1));
                pair.aName   = cursor.getString(2);
                pair.bName   = cursor.getString(3);
                pair.bAI     = Integer.parseInt(cursor.getString(4));
                pair.rules   = Integer.parseInt(cursor.getString(5));
                pair.board   = Integer.parseInt(cursor.getString(6));
                
                int index = 7;
                for(int iRules = 0; iRules < GoSettings.numRules; iRules += 1) {
            		for(int iBoard = 0; iBoard < GoSettings.numBoards; iBoard += 1) {
            			for(int iStart = 0; iStart < 2; iStart += 1) {
            				for(int iWin = 0; iWin < 3; iWin += 1) {
            					pair.res[iRules][iBoard][iStart][iWin] = Integer.parseInt(cursor.getString(index));
            					index += 1;
            				}
            			}
            		}
                }
                
                // Add this player pair to all records 
                ret.add(pair);
                
            } while (cursor.moveToNext());
        }
        
        db.close();
        
        return ret;	
	}
	
	private int addRecord(PlayerPair pair, boolean update){

		//Log.d("gomoku", "GoStat.addRecord, id = " + pair.id + ", aName = " + pair.aName + " | update = " + update);
		
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		if(update) {
			values.put(KEY_ID     , pair.id);
		}
		values.put(KEY_CURRENT, pair.current);
		values.put(KEY_ANAME  , pair.aName);
		values.put(KEY_BNAME  , pair.bName);
		values.put(KEY_BAI    , pair.bAI);
		values.put(KEY_RULES  , pair.rules);
		values.put(KEY_BOARD  , pair.board);
		for(int iRules = 0; iRules < 3; iRules += 1) {
    		for(int iBoard = 0; iBoard < 2; iBoard += 1) {
    			for(int iStart = 0; iStart < 2; iStart += 1) {
    				for(int iWin = 0; iWin < 3; iWin += 1) {
    					
    					// Make sure we only put positive values into stat
    					int putVal = pair.res[iRules][iBoard][iStart][iWin];
    					if(putVal < 0) putVal = 0;
    					
    					values.put(resKey[iRules][iBoard][iStart][iWin], putVal);
    				}
    			}
    		}
		}
		
		// 	3. insert / update
		int retval = pair.id;
		if(update) {
			db.update(TABLE_PAIRS, values, KEY_ID + " = ?", new String[] { String.valueOf(pair.id) });
		}
		else {
			retval = (int)db.insert(TABLE_PAIRS, null, values);
		}

		// 4. close
		db.close();
		
		return retval;
	}
	
	PlayerPair findPairLatest(List<PlayerPair> pp) {
		
		Log.d("gomoku", "GoStat.findPairLatest");
		
		for(PlayerPair p : pp) {
			if(p.current == 2) return p;
		}
		PlayerPair p = new PlayerPair();
		p.current = 2; // 2 = the latest result
		p.id = addRecord(p, false);
		return p;
	}
	
	PlayerPair findPairCurrent(List<PlayerPair> pp) {
		
		Log.d("gomoku", "GoStat.findPairCurrent");
		
		for(PlayerPair p : pp) {
			if(p.current == 1) return p;
		}
		PlayerPair p = new PlayerPair();
		p.current = 1; // 2 = the latest result
		p.id = addRecord(p, false);
		return p;
	}
	
	PlayerPair findPairLong(List<PlayerPair> pp, String aName, String bName, int bAI) {
		
		Log.d("gomoku", "GoStat.findPairLong, aName = " + aName + "bName = " + bName + "bAI = " + bAI);
		
		// See if this PlayerPair is present
		for(PlayerPair p : pp) {
			
			// Only look for pair with current == 0
			if(p.current != 0) continue;
			
			// Human vs. CPU does not rely on names..
			if(bAI > 0 && bAI == p.bAI) return p;
			
			if(bAI == 0 && bAI == p.bAI) {
				// Do not care what way the names are..
				if(p.aName.equals(aName) && p.bName.equals(bName)) return p;
				if(p.bName.equals(aName) && p.aName.equals(bName)) return p;
			}
		}
		
		// If not, create a new player pair..
		
		PlayerPair np = new PlayerPair();
		
		// Human vs. CPU
		if(bAI > 0) {
			np.aName = "human"; // This will not be shown
			np.bName = "cpu";   // This will not be shown
			np.bAI = bAI;
			np.current = 0;
		}
		// Human vs. Human
		else {
			np.aName = aName;
			np.bName = bName;
			np.bAI = 0;
			np.current = 0;
		}
		
		// Add this record
		np.id = addRecord(np, false);
		
		return np;			
	}
		
	// Look for the long time AI pairs
	PlayerPair findPairAI(List<PlayerPair> pp, int bAI) {
			
		for(PlayerPair p : pp) {
				
			// Only look for pair with current == 0
			if(p.current != 0) continue;
				
			// Human vs. CPU does not rely on names..
			if(bAI > 0 && bAI == p.bAI) return p;
		}
			
		PlayerPair p = new PlayerPair();
		p.bAI = bAI;
		p.id = addRecord(p, false);
		return p;
	}
		
	
	public void cancelLast() {
		
		Log.d("gomoku", "GoStat.cancelLast");
		
		// Get all records (all possible player pairs)
		List<PlayerPair> allRecords = getAllRecords();
		
		// Find the latest game
		PlayerPair pairLatest = findPairLatest(allRecords);
		
		// Find the current game
		PlayerPair pairCurrent = findPairCurrent(allRecords);
		pairCurrent.addPair(pairLatest, -1);
		
		// Find the long time record
		PlayerPair pairLong = findPairLong(allRecords, pairLatest.aName, pairLatest.bName, pairLatest.bAI);
		
		if(pairLong.aName.equals(pairLatest.aName)) pairLong.addPair(pairLatest, -1);
		else pairLong.addOppositePair(pairLatest, -1);
		
		// Update database
		addRecord(pairCurrent, true);
		addRecord(pairLong, true);
		
	}
	
	// A result has been otained
	public void notifyResult(String aName, String bName, int bAI, int playerAMark, int win, int rules, int board) {
	
		Log.d("gomoku", "stats: notifyRes");
		
		// Get all records (all possible player pairs)
		List<PlayerPair> allRecords = getAllRecords();
		
		// Update the very "latest" game results //
		
		PlayerPair pairLatest = findPairLatest(allRecords);
		pairLatest.setResToZero();
		pairLatest.add(rules, board, playerAMark, win, +1);
		addRecord(pairLatest, true);
		
		// Current game //
		
		PlayerPair pairCur = findPairCurrent(allRecords);
		pairCur.add(rules, board, playerAMark, win, +1);
		addRecord(pairCur, true);
		
		// All time record //
		
		PlayerPair pairLong = findPairLong(allRecords, aName, bName, bAI);
		
		// Is player A in record same as in current game?
		if(pairLong.aName.equals(aName)) pairLong.add(rules, board, playerAMark, win, +1);
		else pairLong.addOpposite(rules, board, playerAMark, win, +1);
			
		addRecord(pairLong, true);
		
	}
	
	public void startNewGame(String aName, String bName, int bAI) {
		
		Log.d("gomoku", "GoStat.startNewGame, aName = " + aName + ", bName = " + bName + ", bAI = " + bAI);
		
		// Find all records
		List<PlayerPair> allRecords = getAllRecords();
		
		// Set latest and current game to zero
		
		PlayerPair pairLatest = findPairLatest(allRecords);
		
		pairLatest.aName = aName;
		pairLatest.bName = bName;
		pairLatest.bAI = bAI;
		pairLatest.setResToZero();
		
		addRecord(pairLatest, true);
		
		PlayerPair pairCurrent = findPairCurrent(allRecords);
		pairCurrent.aName = aName;
		pairCurrent.bName = bName;
		pairCurrent.bAI = bAI;
		pairCurrent.setResToZero();
		addRecord(pairCurrent, true);
		
		// Start a new long time record for this pair
		findPairLong(allRecords, aName, bName, bAI);
		
	}
	
	// Init the statistics
	public void initializeStat() {
		
	}
	
	public String getWinString(int win[], String eol) {
		
		int total = win[0] + win[1] + win[2];
		
		if(total == 0) {
			return String.format(" %5d  - %3d  - %5d%s    --%% -  --%% -    --%%", win[0], win[2], win[1], eol);
		}
		else {
			float pro[] = new float[3];
			for(int i = 0; i < 3; i++) pro[i] = 100.0f*(float)win[i]/(float)total;
			if(win[0] == total) return String.format(Locale.US, " %5d  - %3d  - %5d%s   %3.0f%% - %3.0f%% -  %4.1f%%", win[0], win[2], win[1], eol, pro[0], pro[2], pro[1]);
			if(win[1] == total) return String.format(Locale.US, " %5d  - %3d  - %5d%s  %4.1f%% - %3.0f%% -   %3.0f%%", win[0], win[2], win[1], eol, pro[0], pro[2], pro[1]);
			if(win[2] == total) return String.format(Locale.US, " %5d  - %3d  - %5d%s  %4.1f%% -   %3.0f%% -  %4.1f%%", win[0], win[2], win[1], eol, pro[0], pro[2], pro[1]);
			return String.format(Locale.US, " %5d  - %3d  - %5d%s  %4.1f%% -  %2.0f%% -  %4.1f%%", win[0], win[2], win[1], eol, pro[0], pro[2], pro[1]);
			
		}
		
	}
	
	public String getInfoStringCur(Resources appRes) {
	
		String eol = System.getProperty("line.separator");
		
		List<PlayerPair> allRecords = getAllRecords();
		
		String str = new String();
		
		// Current game //
		
		PlayerPair ppc = findPairCurrent(allRecords);
		int wins[] = ppc.getWins(-1, -1, -1);
		
		if(ppc.bAI == 0) {
			str += ppc.aName + " - " + ppc.bName + eol;
		}
		else {
			if(ppc.bAI == 1) str += "Against AI 1" + eol;
			if(ppc.bAI == 2) str += "Against AI 2" + eol;
			if(ppc.bAI == 3) str += "Against AI 3" + eol;
		}
		str += getWinString(wins, eol) + eol + eol;
		
		return str;
	}
	
	// Get the info string
	public String getInfoStringLong(Resources appRes, int rulesSelection, int boardSelection) {
		
		String eol = System.getProperty("line.separator");
		
		List<PlayerPair> allRecords = getAllRecords();
		
		String str = new String();
		
		// Current game //
		
		// Against AI
		for(int ai = 1; ai <= 3; ai++) {
			PlayerPair pp = findPairAI(allRecords, ai);
			int win_hus[] = pp.getWins(rulesSelection, boardSelection, 0);
			int win_ais[] = pp.getWins(rulesSelection, boardSelection, 1);
			int win_all[] = pp.getWins(rulesSelection, boardSelection, -1);
			str += "Against AI " + ai + eol;
			str += " you start" + eol;
			str += getWinString(win_hus, eol) + eol;
			str += " AI" + ai + " starts" + eol;
			str += getWinString(win_ais, eol) + eol;
			str += " total" + eol;
			str += getWinString(win_all, eol) + eol;
			str += eol;
		}
		
		// Against Human players
		for(PlayerPair pph : allRecords) {
			
			if(pph.current != 0) continue; // skip current
			if(pph.bAI != 0) continue;     // skip AI
			
			int win1[] = pph.getWins(rulesSelection, boardSelection, 0);
			int win2[] = pph.getWins(rulesSelection, boardSelection, 1);
			int win3[] = pph.getWins(rulesSelection, boardSelection, -1);
			
			str += pph.aName + " vs. " + pph.bName + eol;
			str += " " + pph.aName + " starts" + eol;
			str += getWinString(win1, eol) + eol;
			str += " " + pph.bName + " starts" + eol;
			str += getWinString(win2, eol) + eol;
			str += " total" + eol;
			str += getWinString(win3, eol) + eol;
		}
				
		return str;
	}

}
