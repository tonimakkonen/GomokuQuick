package fi.tonimakkonen.gomoku;

import android.util.Log;

/**
 * Runnable class
 */
public class GoAI implements Runnable {
	
	// A trivial class that will be executed on the UI thread informing
	// the main activity of the choice the AI makes.
	class Informer implements Runnable {
		
		GoAI ai;
		int tile;
		MainActivity activity;
		
		Informer(GoAI sai, int st, MainActivity sa) {
			ai = sai;
			tile = st;
			activity = sa;
		}
		
		@Override
		public void run() {
			activity.informAIChoice(tile, ai);
		}
	}

	// Copy of game state
	GoGame game;
	// Listener
	MainActivity activity;
	// Level
	int level;
	
	// the actual results
	int result;
	
	// a variable teling how many nodes have been seacthed
	int nodeCount;
	
	// 
	GoAI(GoGame gg, int lev, MainActivity ma) {
		game = gg;
		activity = ma;
		level = lev;
	}

	// Do the calculations
	@Override
	public void run() {
				
		// Start AI timing
		long timer = System.nanoTime();
		
		// Who are we?
		// 1 = X (positive is good), 2  = O (negative is good)
		int ai = game.getActivePlayer();
		// We always try to maximize the value -> use aiMult for this
		int aiMult = 1; if(ai == 2) aiMult = -1;
		
		nodeCount = 0;
		result = alphaBeta(0, level + 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true, aiMult);
		
		Log.d("gomoku", "ai time in ss = " + 1.0e-9*(float)(System.nanoTime() - timer));
		Log.d("gomoku", "   nodes searched = " + nodeCount);
		
		// Inform result to AI thread
		activity.runOnUiThread(new Informer(this, result, activity));
	}
		
	private int alphaBeta(int curDepth, int maxDepth, int alpha, int beta, boolean maximize, int aiMult) {
		
		nodeCount += 1;
		
		// Are we the last node (or terminal node)
		// This assumes maxDepth != never 0
		if(curDepth == maxDepth) return aiMult*game.value(0, 0);
		
		// If X wins
		if( game.getWin() == 1) return aiMult*game.value(0, 0);
		
		// We want to maximize
		if(maximize) {
			// Generate childen
			int size = 0;
			if(curDepth < 3) size = 1;
			else size = 0;
			int moves[] = game.createMoveList(size);
			
			// Debug
			if(curDepth == 0) {
				Log.d("gomoku", "first node move count = " + moves.length);
			}
			
			// Best actual move, not value
			// return this on zero node
			int bestMove = -1;
			
			for(int i = 0; i < moves.length; i++) {
				
				game.addMark(moves[i]);
				int newAlpha = alphaBeta(curDepth + 1, maxDepth, alpha, beta, false, aiMult);
				game.undo();
				
				if(newAlpha > alpha) {
					alpha = newAlpha;
					bestMove = moves[i];
				}
				
				if(beta <= alpha) {
					if(curDepth == 0) return i;
					return beta;
				}
			}
			
			// Return the value
			// If zero node, return the actual move (this only happens on a amx node)
			if(curDepth == 0) return bestMove;
			else return alpha;
			
		}
		
		// We want to minimize
		else {
			
			// Generate childen
			int size = 0;
			if(curDepth < 3) size = 1;
			else size = 0;
			int moves[] = game.createMoveList(size);
			
			for(int i = 0; i < moves.length; i++) {
				
				game.addMark(moves[i]);
				int newBeta = alphaBeta(curDepth + 1, maxDepth, alpha, beta, true, aiMult);
				game.undo();
				
				if(newBeta < beta) {
					beta = newBeta;
				}
				
				if(beta <= alpha) return alpha;
				
			}
			
			// Return the value
			return beta;
		}
	}
	
	private int maximize(int mult, int maxLevel, int curLevel) {
		
		// Get all available moves where we are now
		int moves[] = game.createMoveList(1);

		
		int bestVal = 0;
		int bestInd = 0;
		
		// Try all moves, see which one is best
		for(int moveInd = 0; moveInd < moves.length; moveInd++) {
			
			// Try the move
			game.addMark(moves[moveInd]);
			
			// Is it a winning move?
			// If so, choose it automatically..
			if(game.getWin() != 0) {
				
				if(curLevel == 0) {
					game.undo();
					return moves[moveInd];
				}
				else {
					int val = mult*game.value(0, 1);
					game.undo();
					return val;
				}
			}
			
			// Find the value of this move, recursively
			int curValue;
			if(curLevel == maxLevel) curValue = mult*game.value(1, 1);
			else curValue = maximize(mult, maxLevel, curLevel + 1);
			
			// Choose the biggest (even) or smallest (odd)
			if(curLevel % 2 == 0) {
				if(moveInd == 0 || bestVal < curValue) {
					bestVal = curValue;
					bestInd = moveInd;
				}
			}
			else {
				if(moveInd == 0 || bestVal > curValue) {
					bestVal = curValue;
					bestInd = moveInd;
				}
				
			}
			
			
			game.undo();
		}
		
		
		// return the biggest tile
		if(curLevel == 0) return moves[bestInd];
		return bestVal;
	}

}
