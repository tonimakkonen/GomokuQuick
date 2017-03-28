package fi.tonimakkonen.gomoku;

/**
 * An interface where the AI informs moves.
 */
public interface GoAIListener {

    //
    public void informAIChoice(int tile, GoAI iai);

}
