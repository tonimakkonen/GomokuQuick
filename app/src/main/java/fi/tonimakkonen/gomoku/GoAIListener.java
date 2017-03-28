package fi.tonimakkonen.gomoku;

//
// An interface that listens to choices the AI makes
//

public interface GoAIListener {
	
	// 
	public void informAIChoice(int tile, GoAI iai);

}
