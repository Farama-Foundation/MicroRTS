package ai.jni;

public class Responses {
    public int[][][][] observation;
    public double[][] reward;
    public boolean[][] done;
    // public String info;

    public Responses(int[][][][] observation, double reward[][], boolean done[][]) {
        this.observation = observation;
        this.reward = reward;
        this.done = done;
        // this.info = info;
    }

    public void set(int[][][][] observation, double reward[][], boolean done[][]) {
        this.observation = observation;
        this.reward = reward;
        this.done = done;
        // this.info = info;
    }
}