package fi.tonimakkonen.gomoku;

import fi.tonimakkonen.R;
import fi.tonimakkonen.drawutil.DrawUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class MainActivity extends Activity
		implements OnTouchListener, GestureDetector.OnGestureListener, OnScaleGestureListener, GoAIListener, OnDoubleTapListener  {
	
	// Current game //
	
	// The go board and it's state (does not care who is playing etc..)
	GoGame game;
	// The players of the game (names, ai, who is X, who is O...)
	GoPlayers players;
	// Current AI thread
	GoAI ai = null; // current ai object
	
	// Statistics //
	
	GoStat statistics;
	
	
	// Renderer //
	
	GoRenderer goRender;
	
	// detecting gestures and scaling //
	
	GestureDetectorCompat gesture;
	ScaleGestureDetector gscale;
	
	
	// Generic App options //
	
	
	//                        //
	// Android App life cycle //
	//                        //
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up game objects (real state set in loadGameState)
		game = new GoGame(0, 0);
		players = new GoPlayers();
		
		// Statistics
		statistics = new GoStat(this);
		
		// Set up gesture & scale detection
		gesture = new GestureDetectorCompat(this, this);
		gesture.setOnDoubleTapListener(this);
		gscale = new ScaleGestureDetector(this, this);
		
		
		// Init utilities
		DrawUtil.init();
		
		// set view to what is define din layout
		setContentView(R.layout.activity_main);
		
		// Set uo the renderer
		goRender= new GoRenderer(getResources());
		
		// Define "connections" //
	
		// set rendered for to gomoku view
		GoView goView = (GoView) findViewById(R.id.goview);
		goView.setOnTouchListener(this);
		
		goView.setRenderer(goRender);
		
		// Set music stream
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		// Load previous game from 
		loadGameData();
		
		// Set action bar subtitle
		//updateActionBarSubTitle();

		// First time user of this app //
		
		SharedPreferences settings = getSharedPreferences("GomokuFastPref", MODE_PRIVATE);
		if (settings.getBoolean("activity_firstlaunch", true)) {
			
			// Start a new game regarding statistics
			statistics.initializeStat();
			statistics.startNewGame(players.playerAName, players.playerBName, players.playerBAI);
			
			// Launch info screen
			onPressInfo(null);
		    settings.edit().putBoolean("activity_firstlaunch", false).commit(); 
		}
	}
	
	@Override
	public void onStop() {
		saveGameData();
		super.onStop();
	}
	
	//                       //
	// Data loading & saving //
	//                       //
	
	public void loadGameData() {
		
       SharedPreferences settings = getSharedPreferences("GomokuFastPref", MODE_PRIVATE);
       
       // game state
       game.loadState(settings);
       // game players
       players.loadState(getResources(), settings);
       // renderer state
       goRender.loadState(settings);
       
       // Statistics
       //statistics.loadState(settings);
       
       // The board is in a new state, check what to do now?
       // Note that we do not update statistics when loading the game state
       boardUpdated(false);
       
	}
	
	public void saveGameData() {
		
	      SharedPreferences settings = getSharedPreferences("GomokuFastPref", MODE_PRIVATE);
	      SharedPreferences.Editor editor = settings.edit();

	      // game
	      game.saveState(editor);
	      // players
	      players.saveState(editor);
	      // render state
	      goRender.saveState(editor);
	      
	      // statistics
	      // Save andload is done when game is finished
	      //statistics.saveState(editor);
	      
	      // Commit the edits!
	      editor.commit();
		
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		
		MenuItem bundo = menu.findItem(R.id.action_undo);
		MenuItem bredo = menu.findItem(R.id.action_redo);
		
		// Check if we can undo
		
		// TODO: Check this with AI play
		boolean canUndo = game.canUndo();
		
		if(canUndo) {
			bundo.setEnabled(true);
			bundo.getIcon().setAlpha(255);
		}
		else {
			bundo.setEnabled(false);
			bundo.getIcon().setAlpha(130);
		}
		
		if(game.canRedo()) {
			bredo.setEnabled(true);
			bredo.getIcon().setAlpha(255);
		}
		else {
			bredo.setEnabled(false);
			bredo.getIcon().setAlpha(130);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	// Action bar
	
	void updateActionBarTitle() {
		
		// Title (rules of the game) //
		
		String[] rules = getResources().getStringArray(R.array.ng_rules_array);
		getActionBar().setTitle(rules[game.gameRules]);
		
		// Subtitle //
		
		String st = "";
		int win = game.getWin();
		
		switch(win) {
		
		case 0: //No win, place mark, X or O
			
			int mark = game.getActivePlayer();
			
			// The only human playing, do not show his name
			if(players.isMarkOnlyHuman(mark)) {
				if(mark == 1) st = getResources().getString(R.string.ab_human_x);
				if(mark == 2) st = getResources().getString(R.string.ab_human_o);
			}
			// Human (but not only) playing, show name
			else if(players.isMarkHuman(mark)) {
				String name = players.getMarkName(mark);
				if(mark == 1) st = getResources().getString(R.string.ab_human_x) + " (" +name + ")";
				if(mark == 2) st = getResources().getString(R.string.ab_human_o) + " (" +name + ")";
			}
			// AI (placING not place)
			else {
				String name = " ";
				if(players.playerBAI == 1) name = getResources().getString(R.string.ai_name1);
				if(players.playerBAI == 2) name = getResources().getString(R.string.ai_name2);
				if(players.playerBAI == 3) name = getResources().getString(R.string.ai_name3);
				if(mark == 1) st = getResources().getString(R.string.ab_ai_x) + " (" +name + ")";
				if(mark == 2) st = getResources().getString(R.string.ab_ai_o) + " (" +name + ")";
			}
			
			break;
		
		case -1: // Tie
			st = getResources().getString(R.string.ab_tie);
			break;
		
		case 1: // X wins
		case 2: // O wins
			
			// Only human player winning (You win!)
			if(players.isMarkOnlyHuman(win)) {
				st = getResources().getString(R.string.ab_youwin);
			}
			// AI or human in PvP game wins (X wins!)
			else {
				String name = players.getMarkName(win);
				st = name + " " + getResources().getString(R.string.ab_win);
			}
			
			break;
		
		default: // Mistake
				
			
		}
		
		getActionBar().setSubtitle(st);
	}
	
	// Menu buttons
	
	public void onPressUndo(MenuItem mi) {
		
		// Is the game a win or tie?
		// If so, cancel win notification
		if(game.getWin() != 0) {
			statistics.cancelLast();
		}
		
		// Do the action on the board
		if(players.isAIPresent()) {
			
			// If the AI is placing it's stone
			if(!players.isMarkHuman(game.getActivePlayer())) {
				
				// Stop AI calculation
				//if(ai != null) ai.cancel(true); // cancel ai calculation
				ai = null; // set ai to null, just in case not to recieve any message beyound the grave
				
				// cancel players lats move
				// will work with just one stone
				game.undo();
			}
			else {
				
				// cancel players lats move, and AI move before that
				// will work with just one stone
				game.undo();
				game.undo();
				
			}
		}
		else {
			// No AI present, just undo the last move
			game.undo();
		}
		
		
		// board updated
    	boardUpdated(true);
	}
	
	public void onPressRedo(MenuItem mi) {
		
		// Do the action on the board
		if(players.isAIPresent()) {
			game.redo();
			game.redo();
		}
		else {
			game.redo();
		}
		
		// board updated, action bar, renderer, ai?
		boardUpdated(true);
	}
	
	public void onPressZoomIn(MenuItem mi) {
		goRender.applyZoom(0.70710678118f); // 1 / sqrt(2), good value
	}
	public void onPressZoomOut(MenuItem mi) {
		goRender.applyZoom(1.41421356237f); // sqrt(2.0f), good value
	}
	public void onPressZoomReset(MenuItem mi) {
		// Get the display metrics
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		// How many pixels per inch -> how many pixels per desired 9 mm grid;
		goRender.setBestZoom(metrics.xdpi * 9.0f/25.4f);
	}
	
	public void onPressNew(MenuItem mi) {
		NewGameDialog ngd = new NewGameDialog();
		ngd.show(getFragmentManager(), "newGameDialog");
	}
	
	public void onPressInfo(MenuItem mi) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		WebView wv = new WebView(this);
		
		dialog.setView(wv);
		dialog.setTitle(R.string.mb_info);
		
		// Make sure this is done inside the dialog
		wv.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        view.loadUrl(url);
		        return true;
		    }
		});
		
		// Load the info page
		wv.loadUrl("file:///android_asset/gomokufast_info.html");
		
		// Show the dialog
		dialog.show();
		
	}
	
	public void onPressGraph(MenuItem mi) {
		
	}
	
	public void onPressStat(MenuItem mi) {
		StatDialog sd = new StatDialog();
		sd.show(getFragmentManager(), "statDialog");
	}


	// Handle touch to screen
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// pass it to the gesture detector
		gesture.onTouchEvent(event);
		gscale.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		
		// Move the view
		goRender.moveView(distanceX, distanceY);
		
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
		// Cant do anothing if we have a win (or tie)
		if(game.getWin() != 0) return true;
		// Waiting for AI player B (can be O or X) 
		if(players.playerBAI != 0 && game.getActivePlayer() != players.playerAMark) return true;
		
		// Now we have human player A or human player B..
		
		// Get the location where the user tapped
		int tile = goRender.getTileIndex(e.getX(), e.getY());
		// outside
		if(tile < 0) return true;
		// if in map
		if(game.addMark(tile)) {
			// board is in a new state, what should we do
			boardUpdated(true);
		}
		return true;
		
		// If AI action is needed, it is started..
	}
	
	// Handle scale //

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		goRender.applyZoom(1.0f / detector.getScaleFactor());
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		
	}
	
	// double tap listener
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		
		// If the game has ended (win for X, win for O, or tie), start a new game with a double tap
		if(game.getWin() == 0) return true;
		
		// We do not start a new game
		
		// start new game
		game = new GoGame(game.gameRules, game.gameBoard);
		
		// switch player starting order
		if(players.playerAMark == 1) players.playerAMark = 2;
		else players.playerAMark = 1;
		
		// The board has been updated, update action bar sub title, check if input from AI is needed, etc..
		boardUpdated(true);
		
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}
	
	//                         //
	// Handle feedback from AI //
	//                         //

	// Check if we need to start a new ai thread
	private void checkForAI() {
		
		// If the game is a win (or tie), no AI action is needed
		if(game.getWin() != 0) return;
		
		// Do we need input from the AI now
		if(!players.isMarkHuman(game.getActivePlayer())) {
			ai = new GoAI(game.clone(), players.playerBAI, this);
			Thread th =new Thread(ai);
			th.setPriority(Thread.MAX_PRIORITY);
			th.start();
			//ai.execute();	
		}

	}
	
	// This function is called from the AI thread
	public void informAIChoice(int tile, GoAI iai) {
		
		//Log.d("MainActivity", "informAIChoice, tile = " + tile);
		
		// First, make sure this is from the latest ai thread
		if(iai != ai) return;
		
		// Now, add the mark
		if(!game.addMark(tile)) {
			// Mark position invalid, serious error!!
		}
		
		goRender.setNotification(tile);
		
		// board has changed
		boardUpdated(true);
	}
	
	// The board has been updated, check how to update menus, ask AI for feedback?, update stat?
	void boardUpdated(boolean updateStat) {
		
		// Update action bar subtitle
		invalidateOptionsMenu();           // undo & redo buttons
		this.updateActionBarTitle();       // e.g. "Place X" or "Placing O (AI 1)"
		
		// check if input from AI is needed (this will potentially start a new thread)
		// AI is not started if win or tie
		checkForAI();
		
		// Set new renderer state (true for fade effect)
		goRender.setBoardStyle(game.gameBoard, 0);
		
		// Set the status of the board
		goRender.updateBoard(game.status, true);
		
		// Mark the last tile
		goRender.setLastTile(game.lastTile());
		
		// win row?
		if(game.getWin() != 0) {
			
			goRender.setWinRow(game.getWinRow());
			
			//goRender.setNextGameInfo(true);
			
			// Double tao for new game
			Toast.makeText(this, R.string.toast_taptap, Toast.LENGTH_LONG).show();
			
			// Notify statistics of win or tie
			if(updateStat) {
				statistics.notifyResult(players.playerAName, players.playerBName, players.playerBAI, players.playerAMark, game.getWin(), game.gameRules, game.gameBoard);
			}
			
		}
		else {
			goRender.setWinRow(null);
			//goRender.setNextGameInfo(false);
		}
	}


}
