package ai.socket;

import rts.GameState;
import rts.PlayerAction;

public interface SocketAIInterface {
	public PlayerAction getAction(int player, GameState gs) throws Exception;
    public void connectToServer() throws Exception;
    public void reset();
    public void gameOver(int winner) throws Exception;
	public void computeReward(int i, int j, GameState gs) throws Exception;
	public void gameOver(int winner, GameState gs) throws Exception;
	public boolean getDone();
	public boolean getFinished();
}