package fi.tonimakkonen.gomoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import fi.tonimakkonen.R;

public class StatDialog extends DialogFragment implements OnItemSelectedListener {
	
	MainActivity mainActivity;
	
	TextView tvCur;
	TextView tvStat;
	Spinner spinnerRules;
	Spinner spinnerBoard;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Acquire the main activity (this will also work in cases of restart when orientation changes..)
		mainActivity = (MainActivity)getActivity();
		
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        // Set the New Game title
        //builder.setTitle(R.string.mb_new);
        
        // Set the view
        View view = inflater.inflate(R.layout.stat_layout, null);
        builder.setView(view);
        
        // Find the text views and set initial state
        tvCur = (TextView)view.findViewById(R.id.stat_curgame);
        tvCur.setText(mainActivity.statistics.getInfoStringCur(mainActivity.getResources()));
        tvStat = (TextView)view.findViewById(R.id.stat_long);
        tvStat.setText(mainActivity.statistics.getInfoStringLong(mainActivity.getResources(), -1, -1));
      
        // What rules to look for //
        
        spinnerRules = (Spinner)view.findViewById(R.id.stat_rules_spinner);
        ArrayAdapter<CharSequence> adapterRules = ArrayAdapter.createFromResource(getActivity(),
        R.array.stat_rules_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterRules.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerRules.setAdapter(adapterRules);
        spinnerRules.setOnItemSelectedListener(this);
        
        // What board types to look for //
        
        spinnerBoard = (Spinner)view.findViewById(R.id.stat_board_spinner);
        ArrayAdapter<CharSequence> adapterBoard = ArrayAdapter.createFromResource(getActivity(),
        R.array.stat_board_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBoard.setAdapter(adapterBoard);
        spinnerBoard.setOnItemSelectedListener(this);
        
        
        // Set the tite
        builder.setTitle(R.string.stat_dialog_title);
        
        return builder.create();
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		// What rules
		int rulesSelection = spinnerRules.getSelectedItemPosition();
		if(rulesSelection == 0) rulesSelection = -1;
		else if(rulesSelection > 0 && rulesSelection <= GoSettings.numRules) rulesSelection -= 1;
		else {
			Log.e("gomoku", "StatDialog.onItemSelected: rules = " + rulesSelection);
			rulesSelection = -1;
		}
		
		// What What board
		int boardSelection = spinnerBoard.getSelectedItemPosition();
		if(boardSelection == 0) boardSelection = -1;
		else if(boardSelection > 0 && boardSelection <= GoSettings.numBoards) boardSelection -= 1;
		else {
			Log.e("gomoku", "StatDialog.onItemSelected: baord = " + boardSelection);
			boardSelection = -1;
		}
		
		// Define the stat text
		tvStat.setText(mainActivity.statistics.getInfoStringLong(mainActivity.getResources(), rulesSelection, boardSelection));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

}
