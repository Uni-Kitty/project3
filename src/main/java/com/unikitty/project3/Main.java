package com.unikitty.project3;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.unikitty.jackson.Game;
import com.unikitty.jackson.Model;

/**
 * Here is your main, I wrote JacksonExample so you guys can see how to use it.
 * Eventually main will of course start our servers and do other magical things.
 */
public class Main {
	private static Map<InetAddress, Player> playersInGame = new HashMap<InetAddress, Player>();
	private static Game gameRepresentation = new Game();
	private static int defaultPort;
	
    public static void main( String[] args ) {
    	if (args.length > 0) {
            try {
                defaultPort = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                System.exit(0);
            }
        }
    	try {
             System.out.println("Starting Server on port " + defaultPort);
             DatagramSocket serverSocket = new DatagramSocket(defaultPort);
             byte[] receivedData = new byte[1024];
             byte[] dataToSend  = new byte[1024];
             int playerIDCount = 0;
             int attackIDCount = 0;
             while (true) {
                 DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                 serverSocket.receive(receivedPacket);   // receive a packet
                 InetAddress IPAddress = receivedPacket.getAddress();
                 if (playersInGame.keySet().contains(IPAddress)) {
                	 // this player is in game!
                	 // it is an attack or a position update
                	 String update = new String(receivedPacket.getData());   
                     System.out.println("RECEIVED: " + update);
                 } else {
                	 // new player
                	 int port = receivedPacket.getPort();
                	 Player newPlayer = new Player(IPAddress, port, playerIDCount);
                	 playersInGame.put(IPAddress, newPlayer);
                	 playerIDCount++;
                	 gameRepresentation.addPlayer(newPlayer.getGameState());
                 }
             }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //jacksonExample();
    }
    
    /**
     * runs Jackson example
     */
    private static void jacksonExample() {
        try {
            Model m = new Model(1, 2);
            String JSONString = ObjectToJackson(m);
            System.out.println("\nAttempting to map POJO to JSON String:");
            System.out.println(JSONString + "\n");
            Model n = JSONToModel(JSONString);
            System.out.println("Attempting to map JSON String to POJO");
            if (m.equals(n))
                System.out.println("Successfully deserialized JSON\n");
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Maps Model m to a JSON String with Jackson
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    private static String ObjectToJackson(Model m) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper obj = new ObjectMapper();
        return obj.writeValueAsString(m);
    }
    
    /**
     * Maps a JSON String to a Model
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    private static Model JSONToModel(String mString) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper obj = new ObjectMapper();
        return (Model) obj.readValue(mString, Model.class);
    }
    
}
