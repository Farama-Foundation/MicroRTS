/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.jni;

/**
 *
 * @author costa
 */
public class Response {
    public int[][][] observation;
    public double[] reward;
    public boolean[] done;
    public String info;

    public Response(int[][][] observation, double reward[], boolean done[], String info) {
        this.observation = observation;
        this.reward = reward;
        this.done = done;
        this.info = info;
    }

    public void set(int[][][] observation, double reward[], boolean done[], String info) {
        this.observation = observation;
        this.reward = reward;
        this.done = done;
        this.info = info;
    }
}