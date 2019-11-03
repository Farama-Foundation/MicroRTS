/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.sockets;

import ai.abstraction.WorkerRush;
import ai.core.AIWithComputationBudget;
import ai.socket.JSONSocketWrapperAI;
import ai.socket.XMLSocketWrapperAI;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 * 
 * Run this file first in the computer where you want the server to
 * be running.
 * 
 */
public class RunServerExample {
    public static void main(String args[]) throws Exception {
        AIWithComputationBudget ai = new WorkerRush(new UnitTypeTable());
        int port = 9898;
        
//        XMLSocketWrapperAI.DEBUG = 1;
//        XMLSocketWrapperAI.runServer(ai, port);
        JSONSocketWrapperAI.DEBUG = 1;
        JSONSocketWrapperAI.runServer(ai, port);
    }
}
