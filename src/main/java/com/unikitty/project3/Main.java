package com.unikitty.project3;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * How to use Jackson:
 * 
 * Object to JSON : mapper.writeValueAsString(m);
 * 
 * JSON to Object : (Attack) mapper.readValue(inputString, Attack.class);
 */
public class Main {
    
    public static final int PORT = 9999;
    public static final int BROADCAST_DELAY = 15;
    
	private static Game game = new Game(); // the state of the game
	private static HashMap<Session, Player> playersInGame = new HashMap<Session, Player>();
	private static ObjectMapper mapper = new ObjectMapper(); // mapper for obj<-->JSON
	private static AtomicInteger id = new AtomicInteger(); // avoid race conditions, use atomic int for ids
	
    public static void main( String[] args ) {
        startWebSocketServer();
    }
    
    private static void startWebSocketServer() {
        startMockGame(); // this will go away eventually, just here to give the front end something to display
        startBroadcasting();
        try {
            Server server = new Server(PORT);
            WebSocketHandler wsHandler = new WebSocketHandler() {
                @Override
                public void configure(WebSocketServletFactory factory) {
                    factory.register(ClientSocketHandler.class);
                }
            };
            server.setHandler(wsHandler);
            server.start();
            server.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            
        }
    }
    
    private static void startBroadcasting() {
        new Thread(new GameBroadcaster()).start();
    }
    
    private static void startMockGame() {
        Player p1 = new Player(200, 300, getNextId());
        Player p2 = new Player(500, 300, getNextId());
        Player p3 = new Player(800, 300, getNextId());
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        new Thread(new MockGameRunner()).start();
    }
    
    // This thing broadcasts to all active sessions
    private static class GameBroadcaster implements Runnable {
        
        public void run() {
            while (true) {
                try {
                    for (Session session : playersInGame.keySet()) {
                        if (session.isOpen()) // TODO: handle this better, do we disconnect player if session goes bad?
                            session.getRemote().sendString(mapper.writeValueAsString(game));
                    }
                    Thread.sleep(BROADCAST_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    // Temporary thing, makes mock wizards run around in the game giving the ui something to display
    private static class MockGameRunner implements Runnable {
        
        public void run() {
            int i = Integer.MIN_VALUE;
            while (true) {
                i++;
                try {
                    int j = i % 400;
                    int xDelta = 0;
                    int yDelta = 0;
                    if (j > 300)
                        xDelta = 1;
                    else if (j > 200)
                        yDelta = 1;
                    else if (j > 100)
                        xDelta = -1;
                    else
                        yDelta = -1;
                    for (Player p : game.getPlayers()) {
                        p.xPos += xDelta;
                        p.yPos += yDelta;
                    }
                    Thread.sleep(10);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    @WebSocket
    public static class ClientSocketHandler {

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
        }

        @OnWebSocketError
        public void onError(Throwable t) {
            System.out.println("Error: " + t.getMessage());
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            playersInGame.put(session, createNewPlayer());
            System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            // TODO: handle incoming messages from clients, I guess this would be the clients sending attack info
            System.out.println("Message: " + message);
        }
    }
    
    private static Player createNewPlayer() {
        // TODO: register new player with a fresh id etc
        return new Player();
    }
    
    // Fetches the next unique id
    private static int getNextId() {
        return id.incrementAndGet();
    }
    
    /* OLD CODE
    
    private static void startUDPServer() {
        try {
            //serverSocket = new DatagramSocket(serverPort);
            System.out.println("Server udp started on port " + PORT);
            ExecutorService threadPool = Executors.newCachedThreadPool(); // thread pool to avoid creating too many threads
            while (true) {
                try { // need another try inside this loop so the server doesn't die on an error
                    byte[] receivedData = new byte[1024];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                    //serverSocket.receive(receivedPacket);
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
        }
    }
    
    private static class PacketHandler implements Runnable {
        
        private DatagramPacket receivedPacket;
        
        private PacketHandler(DatagramPacket receivedPacket) {
            this.receivedPacket = receivedPacket;
        }
        
        public void run() {
            InetAddress IPAddress = receivedPacket.getAddress();
            if (playersInGame.keySet().contains(IPAddress)) {
                // this player is in game!
                // it is an attack or a position update
                String update = new String(receivedPacket.getData());   
                System.out.println("RECEIVED: " + update);
            } else {
                // new player
                Player newPlayer = new Player(IPAddress, clientPort, getNextId());
                playersInGame.put(IPAddress, newPlayer);
                gameRepresentation.addPlayer(newPlayer.getGameState());
            }
        }
        
    }

*/
    
    
}
