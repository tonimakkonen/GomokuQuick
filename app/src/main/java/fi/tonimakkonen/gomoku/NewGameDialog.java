package fi.tonimakkonen.gomoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * New game dialog
 */
public class NewGameDialog extends DialogFragment implements OnItemSelectedListener, DialogInterface.OnClickListener {

    MainActivity mainActivity;

    EditText tvA;
    EditText tvB;
    Spinner spinnerAI;
    Spinner spinnerStart;
    Spinner spinnerRules;
    Spinner spinnerBoard;

    void defineInitialState() {
        //spinnerAI.setSelection(mainActivity.players.playerBAI);
        //tvA.setText(mainActivity.players.playerAName);
        //tvB.setText(mainActivity.players.playerBName);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Acquire the main activity (this will also work in cases of restart when orientation changes..)
        mainActivity = (MainActivity) getActivity();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set the New Game title
        //builder.setTitle(R.string.mb_new);

        // Set the view
        View view = inflater.inflate(R.layout.newgame, null);
        builder.setView(view);

        // Get text views
        tvA = (EditText) view.findViewById(R.id.ng_aname);
        tvB = (EditText) view.findViewById(R.id.ng_bname);

        //tvA.setText(mainActivity.players.playerAName);
        //tvB.setText(mainActivity.players.playerBName);

        // AI LEVEL SELECTION //

        spinnerAI = (Spinner) view.findViewById(R.id.ng_ai_spinner);
        ArrayAdapter<CharSequence> adapterAI = ArrayAdapter.createFromResource(getActivity(),
                R.array.ng_ai_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterAI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerAI.setAdapter(adapterAI);
        //spinnerAI.setSelection(mainActivity.players.playerBAI);

        // This class will listen to changes
        spinnerAI.setOnItemSelectedListener(this);

        // STARTING SELECTION //

        spinnerStart = (Spinner) view.findViewById(R.id.ng_start_spinner);
        ArrayAdapter<CharSequence> adapterStart = ArrayAdapter.createFromResource(getActivity(),
                R.array.ng_start_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterStart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerStart.setAdapter(adapterStart);

        // RULES SPINNER //

        spinnerRules = (Spinner) view.findViewById(R.id.ng_rules_spinner);
        ArrayAdapter<CharSequence> adapterRules = ArrayAdapter.createFromResource(getActivity(),
                R.array.ng_rules_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterRules.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerRules.setAdapter(adapterRules);


        // BOARD SPINNER //

        spinnerBoard = (Spinner) view.findViewById(R.id.ng_board_spinner);
        ArrayAdapter<CharSequence> adapterBoard = ArrayAdapter.createFromResource(getActivity(),
                R.array.ng_board_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBoard.setAdapter(adapterBoard);

        // Set positive and negative buttons //

        builder.setPositiveButton(R.string.ng_go, this);
        builder.setNegativeButton(R.string.ng_cancel, null);

        // Set initial states //

        //tvA.setText(R.string.human_name);
        //tvB.setText(R.string.opponent_name);

        // Set the title
        builder.setTitle("New Game");

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {

        // Check player B anme
        if (spinnerAI.getSelectedItemPosition() == 0) {
            if (!tvB.isEnabled()) {
                tvB.setEnabled(true);
                tvB.setText(R.string.opponent_name);
            }
        } else {
            tvB.setEnabled(false);
            if (spinnerAI.getSelectedItemPosition() == 1) tvB.setText(R.string.ai_name1);
            if (spinnerAI.getSelectedItemPosition() == 2) tvB.setText(R.string.ai_name2);
            if (spinnerAI.getSelectedItemPosition() == 3) tvB.setText(R.string.ai_name3);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onClick(DialogInterface arg0, int arg1) {
        // User clicked Start New Game!

        int rules = spinnerRules.getSelectedItemPosition();
        int board = spinnerBoard.getSelectedItemPosition();

        mainActivity.game = new GoGame(rules, board);

        String aName = tvA.getText().toString();
        String bName = tvB.getText().toString();
        mainActivity.players.playerBAI = spinnerAI.getSelectedItemPosition();
        if (mainActivity.players.playerBAI > 0) {
            aName = "human"; // will not be shown
            bName = "cpu";   // will not be shown
        }
        if (aName.length() < 1) {
            aName = mainActivity.getResources().getString(R.string.ng_playeraname);
        }
        if (bName.length() < 1) {
            bName = mainActivity.getResources().getString(R.string.ng_playerbname);
        }
        mainActivity.players.playerAName = aName;
        mainActivity.players.playerBName = bName;


        if (spinnerStart.getSelectedItemPosition() == 0) {
            mainActivity.players.playerAMark = 1;
        } else {
            mainActivity.players.playerAMark = 2;
        }

        // Notify statistics that a new game has been created
        mainActivity.statistics.startNewGame(mainActivity.players.playerAName, mainActivity.players.playerBName, mainActivity.players.playerBAI);

        mainActivity.boardUpdated(true);

    }


}
