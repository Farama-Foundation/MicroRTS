/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.socket;

import ai.abstraction.WorkerRush;
import ai.core.AIWithComputationBudget;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class XMLSocketWrapperAI {
    public static int DEBUG = 0;
    
    public static void main(String[] args) throws Exception {
        DEBUG = 1;
        runServer(new WorkerRush(new UnitTypeTable()), 9898);
    }
    
    
    public static void runServer(AIWithComputationBudget ai, int socket) throws Exception {
        if (DEBUG>=1) System.out.println("XMLSocketWrapperAI server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(socket);
        try {
            while (true) {
                new SocketWrapperAI(listener.accept(), clientNumber++, (AIWithComputationBudget)ai.clone()).start();
            }
        } finally {
            listener.close();
        }
    }
    

    private static class SocketWrapperAI extends Thread {
        Socket socket = null;
        int clientNumber = 0;
        int time_budget = 100;
        int iterations_budget = 0;
        UnitTypeTable utt = null;
        AIWithComputationBudget ai = null;

        public SocketWrapperAI(Socket a_socket, int a_clientNumber, AIWithComputationBudget a_ai) {
            socket = a_socket;
            clientNumber = a_clientNumber;
            ai = a_ai;
            if (DEBUG>=1) System.out.println("New connection with client# " + a_clientNumber + " at " + a_socket);
        }


        public void run() {
            try {
                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("XMLSocketWrapperAI: you are client #" + clientNumber);
                
                // Get messages from the client, line by line
                while (true) {
                    String input = in.readLine();
                    if (input == null) break;

                    if (input.startsWith("end")) {
                        System.exit(0);                        
                    } else if (input.startsWith("budget")) {
                        String []tokens = input.split(" ");
                        time_budget = Integer.parseInt(tokens[1]);
                        iterations_budget = Integer.parseInt(tokens[2]);
                        if (DEBUG>=1) System.out.println("setting the budget to: " + time_budget  + ", " + iterations_budget);
                        
                        // reset the AI:
                        ai.reset();
                        ai.setTimeBudget(time_budget);
                        ai.setIterationsBudget(iterations_budget);
                        out.append("ack\n");
                        out.flush();
                    } else if (input.startsWith("utt")) {
                        input = in.readLine();
                        if (DEBUG>=1) System.out.println("setting the utt to: " + input);
                        // parse the unit type table:
                        utt = UnitTypeTable.fromXML(new SAXBuilder().build(new StringReader(input)).getRootElement());
                        ai.reset(utt);
                        out.append("ack\n");
                        out.flush();
                    } else if (input.startsWith("getAction")) {
                        String []tokens = input.split(" ");
                        int player = Integer.parseInt(tokens[1]);
                        if (DEBUG>=1) System.out.println("getAction for player " + player);
                        
                        input = in.readLine();
                        if (DEBUG>=1) System.out.println("with game state: " + input);
                        // parse the game state:
                        GameState gs = GameState.fromXML(new SAXBuilder().build(new StringReader(input)).getRootElement(), utt);
                        if (DEBUG>=1) System.out.println(gs);
                        
                        // generate an action and send it through the socket:
                        PlayerAction pa = ai.getAction(player, gs);
                        XMLWriter xml = new XMLWriter(out," ");
                        pa.toxml(xml);
                        xml.flush();
                        out.append("\n");
                        out.flush();
                        if (DEBUG>=1) System.out.println("action sent!");
                    } else if (input.startsWith("preGameAnalysis")) {
                        String []tokens = input.split(" ");
                        int milliseconds = Integer.parseInt(tokens[1]);
                        String readWriteFolder = null;
                        if (tokens.length>=2) {
                            readWriteFolder = tokens[2];
                            if (readWriteFolder.startsWith("\"")) readWriteFolder = readWriteFolder.substring(1, readWriteFolder.length()-1);
                        }
                        if (DEBUG>=1) System.out.println("preGameAnalysis");
                        
                        input = in.readLine();
                        if (DEBUG>=1) System.out.println("with game state: " + input);
                        // parse the game state:
                        GameState gs = GameState.fromXML(new SAXBuilder().build(new StringReader(input)).getRootElement(), utt);
                        if (DEBUG>=1) System.out.println(gs);

                        if (readWriteFolder != null) {
                            ai.preGameAnalysis(gs, milliseconds, readWriteFolder);                            
                        } else {
                            ai.preGameAnalysis(gs, milliseconds);
                        }
                        
                        out.append("ack\n");
                        out.flush();
                    } else if (input.startsWith("gameOver")) {
                        String []tokens = input.split(" ");
                        int winner = Integer.parseInt(tokens[1]);
                        if (DEBUG>=1) System.out.println("gameOver " + winner);
                        ai.gameOver(winner);
                        out.append("ack\n");
                        out.flush();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error handling client# " + clientNumber + ": " + e);
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }
        }
    }
    
}
