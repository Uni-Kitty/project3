package com.unikitty.project3;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
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
    public static final String PING = "ping";
    public static final String ATTACK = "attack";
    public static final String UPDATE = "update";
    public static final String WELCOME = "welcome";
    public static final String MOVEMENT = "movement";
    
	private static Game game = new Game(); // the state of the game
	private static HashMap<Integer, Player> playersInGame = new HashMap<Integer, Player>();
	private static HashMap<Integer, Session> playerSessions = new HashMap<Integer, Session>();
	private static ObjectMapper mapper = new ObjectMapper(); // mapper for obj<-->JSON
	private static AtomicInteger id = new AtomicInteger(1); // avoid race conditions, use atomic int for ids
	
	static {
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	}
	
    public static void main( String[] args ) {
        startMockGame(); // this will go away eventually, just here to give the front end something to display
        startBroadcasting();
        startWebSocketServer();
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
            Player newPlayer = createNewPlayer();
            playersInGame.put(newPlayer.getId(), newPlayer);
            playerSessions.put(newPlayer.getId(), session);
            Message m = new Message();
            m.setId(newPlayer.getId());
            m.setType(WELCOME);
            try {
                session.getRemote().sendString(mapper.writeValueAsString(m));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            // TODO: handle incoming messages from clients, I guess this would be the clients sending attack info
            System.out.println("Message: " + message);
            try {
                Message m = mapper.readValue(message, Message.class);
                switch (m.getType()) {
	                case (PING): // this is a ping request, send the same message back to correct player
	                    Session s = playerSessions.get(m.getId());
	                    s.getRemote().sendString(message);
	                    break;
	                case (ATTACK):  // register the attack in the game
	                	Attack a = mapper.readValue(m.getData(), Attack.class);
	                	game.addAttack(a);
	                	// TODO: keep track of attack location
	                	//	decide if it hits another player in its trajectory
	                	//  figure out how we want to update the positon of the attack
	                	//  perhaps before every broadcast we update the attack positions
	                case (MOVEMENT):
	                	Player newInfo = mapper.readValue(m.getData(), Player.class);
	                	int identifier = newInfo.getId();
	                	Player oldInfo = playersInGame.get(identifier);
	                	game.updatePlayer(oldInfo, newInfo);  // note: here we remove the old player object and add the new one
	                	playersInGame.put(identifier, newInfo);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void startWebSocketServer() {
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
        Player p1 = new Player(200, 200, getNextId());
        game.addPlayer(p1);
        new Thread(new MockGameRunner(p1)).start();
    }
    
    // This thing broadcasts to all active sessions
    private static class GameBroadcaster implements Runnable {
        public void run() {
            while (true) {
                try {
                    Message m = new Message();
                    m.setType(UPDATE);
                    m.setData(mapper.writeValueAsString(game));
                    String message = mapper.writeValueAsString(m);
                    for (Session session : playerSessions.values()) {
                        if (session.isOpen()) { // TODO: handle this better, do we disconnect player if session goes bad?
                            session.getRemote().sendString(message);
                        }
                    }
                    Thread.sleep(BROADCAST_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Temporary thing, makes mock wizard run around in the game giving the ui something to display
    private static class MockGameRunner implements Runnable {
        
        private Player player;
        
        private MockGameRunner(Player p) {
            this.player = p;
        }
        
        public void run() {
            int xDelta = 0;
            int yDelta = 1;
            while (true) {
                try {
                    if (player.xPos == 200 && player.yPos == 200) {
                        xDelta = 0;
                        yDelta = 1;
                    }
                    else if (player.xPos == 200 && player.yPos == 400) {
                        xDelta = 1;
                        yDelta = 0;
                    }
                    else if (player.xPos == 800 && player.yPos == 400) {
                        xDelta = 0;
                        yDelta = -1;
                    }
                    else if (player.xPos == 800 && player.yPos == 200) {
                        xDelta = -1;
                        yDelta = 0;
                    }
                    player.xPos += xDelta;
                    player.yPos += yDelta;
                    Thread.sleep(10);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    private static Player createNewPlayer() {
        // TODO: what all do we have to do here, adding a new player to the game on the server side
        Player p = new Player();
        p.setId(getNextId());
        return p;
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
