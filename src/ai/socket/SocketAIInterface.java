package ai.socket;

import gui.PhysicalGameStateJFrame;
import rts.GameState;
import rts.PlayerAction;

public interface SocketAIInterface {
	public PlayerAction getAction(int player, GameState gs) throws Exception;
	public void sendGameStateRGBArray(PhysicalGameStateJFrame w);
    public void connectToServer(boolean useUnixSocket) throws Exception;
    public void reset();
    public void gameOver(int winner) throws Exception;
	public void computeReward(int i, int j, GameState gs) throws Exception;
	public boolean getReset();
	public boolean getFinished();
	public boolean getRender();
	public double getReward();
}