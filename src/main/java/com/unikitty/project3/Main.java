package com.unikitty.project3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * How to use Jackson:
 * 
 * Object to JSON : mapper.writeValueAsString(m);
 * 
 * JSON to Object : (Attack) mapper.readValue(inputString, Attack.class);
 */
public class Main {
    
	private static Map<InetAddress, Player> playersInGame = new HashMap<InetAddress, Player>();
	private static Game gameRepresentation = new Game();
	private static int serverPort = 9999;
	private static int clientPort = 9998;
	private static ObjectMapper mapper = new ObjectMapper(); // mapper for obj<-->JSON
	private static AtomicInteger id = new AtomicInteger(); // avoid race conditions, use atomic int for ids
	private static DatagramSocket serverSocket;
	private static DatagramSocket clientSocket;
	
    public static void main( String[] args ) {
        try {
            serverSocket = new DatagramSocket(serverPort);
            clientSocket = new DatagramSocket();
            System.out.println("Server started on port " + serverPort);
            ExecutorService threadPool = Executors.newCachedThreadPool(); // thread pool to avoid creating too many threads
            while (true) {
                try { // need another try inside this loop so the server doesn't die on an error
                    byte[] receivedData = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                    serverSocket.receive(receivedPacket);
                    threadPool.execute(new PacketHandler(receivedPacket));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            System.out.println("Failed to start server, exiting");
            System.exit(0);
        }
        finally {
            serverSocket.close();
            clientSocket.close();
        }
    }
    
    private static void sendHelloWorldUDP(InetAddress address) {
        String hello = "Hello World";
        byte[] data = hello.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address, clientPort);
        try {
            clientSocket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Worker class to handle each request as it comes in
     */
    private static class PacketHandler implements Runnable {
        
        private DatagramPacket receivedPacket;
        
        private PacketHandler(DatagramPacket receivedPacket) {
            this.receivedPacket = receivedPacket;
        }
        
        /**
         * Main method to handle requests as they come in
         */
        public void run() {
            InetAddress IPAddress = receivedPacket.getAddress();
            if (playersInGame.keySet().contains(IPAddress)) {
                // this player is in game!
                // it is an attack or a position update
                String update = new String(receivedPacket.getData());   
                System.out.println("RECEIVED: " + update);
            } else {
                // new player
                int port = 9998;
                Player newPlayer = new Player(IPAddress, port, getNextId());
                playersInGame.put(IPAddress, newPlayer);
                gameRepresentation.addPlayer(newPlayer.getGameState());
            }
            
            // this will go away, here for just getting things working
            sendHelloWorldUDP(IPAddress);
        }
        
    }
    
    // Fetches the next unique id
    private static int getNextId() {
        return id.incrementAndGet();
    }
}
