package fi.tonimakkonen.gomoku;

import android.content.SharedPreferences;
import android.content.res.Resources;

public class GoPlayers {
	
	// Names
	String playerAName = "";
	String playerBName = "";
	
	// Player A is always human, what is player B? (0 = human, 1-> = ai level)
	int playerBAI = 0;
	
	// Who plays X and who plays O, this is reversed every game
	// 1 = player A is X, 2 = player A is O
	int playerAMark = 1;
	
	//           //
	// Utilities //
	//           //
	
	// Get the name of a mark
	public String getMarkName(int mark) {
		if(playerAMark == mark) return playerAName;
		return playerBName;
	}
	
	// Is mark (X or O) a human player
	public boolean isMarkHuman(int mark) {
		// This is the mark of player A, always human
		if(playerAMark == mark) return true;
		// It is the mark of player B, is it human?
		return playerBAI == 0;
	}
	
	// Is the mark the only human?
	public boolean isMarkOnlyHuman(int mark) {
		// Mark of player A (always human), what about player B
		if(playerAMark == mark) return playerBAI > 0;
		// Player B mark, A is always human so not only
		return false;
	}
	
	// Is there Ai playing
	public boolean isAIPresent() {
		return playerBAI > 0;
	}
	
	//                           //
	// Saving & loading settings //
	//                           //

	public void loadState(Resources res, SharedPreferences prefs) {

		// Note: here we define initial state of the game when starting the app the first time
		
		// Names
		playerAName = prefs.getString("goplayers_namea", "human");
		playerBName = prefs.getString("goplayers_nameb", "cpu");
		
		// Get the player B ai level (app start with simple AI)
		playerBAI = prefs.getInt("goplayers_ai", 1);
		
		// Get the player A mark
		playerAMark = prefs.getInt("goplayers_marka", 1);
				
	}

	// Save the state of the game
	public void saveState(SharedPreferences.Editor edit) {
		
		// Save names
		edit.putString("goplayers_namea", playerAName);
		edit.putString("goplayers_nameb", playerBName);
		
		// AI level
		edit.putInt("goplayers_ai", playerBAI);
		
		// player A mark
		edit.putInt("goplayers_marka", playerAMark);
		
	}
}
