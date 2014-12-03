package com.unikitty.project3;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
    public static final int BROADCAST_DELAY = 20;
    public static final String PING = "ping";
    public static final String ATTACK = "attack";
    public static final String UPDATE = "update";
    public static final String WELCOME = "welcome";
    public static final String PLAYER_UPDATE = "player_update";
    public static final int ARENA_WIDTH = 640;
    public static final int ARENA_HEIGHT = 1020;
    public static final int CELL_SIZE = 5;
    
	private static Game game = new Game(); // the state of the game
	private static ConcurrentMap<Integer, Player> playersInGame = new ConcurrentHashMap<Integer, Player>();
	private static ConcurrentMap<Integer, Attack> attacksInGame = new ConcurrentHashMap<Integer, Attack>();
	private static ConcurrentMap<Integer, Session> playerSessions = new ConcurrentHashMap<Integer, Session>();
	private static ObjectMapper mapper = new ObjectMapper(); // mapper for obj<-->JSON
	private static AtomicInteger id = new AtomicInteger(1); // avoid race conditions, use atomic int for ids
	
	static {
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	}
	
    public static void main( String[] args ) {
        startGame(); // this will go away eventually, just here to give the front end something to display
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
            Message<Object> m = new Message<Object>();
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
            	Message<Object> msg = mapper.readValue(message, new TypeReference<Message<Object>>() {});
                switch (msg.getType()) {
	                case (PING): // this is a ping request, send the same message back to correct player
	                    Session s = playerSessions.get(msg.getId());
	                    s.getRemote().sendString(message);
	                    break;
	                case (ATTACK):  // register the attack in the game
	                	//Attack a = (Attack) mapper.readValue(m.getData(), Object.class);
	                	Message<Attack> m = mapper.readValue(message, new TypeReference<Message<Attack>>() {});
	                	Attack a = (Attack) m.getData();
	                	a.setId(getNextId());
	                	game.addAttack(a);
	                	attacksInGame.put(a.getId(), a);
	                	// TODO: keep track of attack location
	                	//	decide if it hits another player in its trajectory
	                	//  figure out how we want to update the positon of the attack
	                	//  perhaps before every broadcast we update the attack positions
	                	break;
	                case (PLAYER_UPDATE):
	                	//Player newInfo = mapper.readValue(m.getData(), Player.class);
	                	Message<Player> m2 = mapper.readValue(message, new TypeReference<Message<Player>>() {});
	                	Player newInfo = (Player) m2.getData();
	                	int identifier = newInfo.getId();
	                	Player oldInfo = playersInGame.get(identifier);
	                	game.updatePlayer(oldInfo, newInfo);  // note: here we remove the old player object and add the new one
	                	playersInGame.put(identifier, newInfo);
	                	break;
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
    
    private static void startGame() {
        Player p1 = new Player(200, 200, getNextId(), 0, 0);
        game.addPlayer(p1);
        new Thread(new GameRunner(p1)).start();
    }
    
    // This thing broadcasts to all active sessions
    private static class GameBroadcaster implements Runnable {
        public void run() {
            while (true) {
                try {
                    Message<Game> m = new Message<Game>();
                    m.setType(UPDATE);
                    m.setData(game);
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
    private static class GameRunner implements Runnable {
        
        private Player player;
        
        private GameRunner(Player p) {
            this.player = p;
        }
        
        public void run() {
            int xDelta = 0;
            int yDelta = 1;
            while (true) {
                try {
                	// build player grid
                	int[][] playerGrid = new int[ARENA_HEIGHT / CELL_SIZE][ARENA_WIDTH / CELL_SIZE];
                	assignPlayersToGrid(playerGrid, game.getPlayers());
                	
                	// update Attack positions and register any hits
                	Iterator<Attack> it = game.getAttacks().iterator();
                	while(it.hasNext()) {
                		Attack a = it.next();
                		a.xPos += a.getxVelocity();
                		a.yPos += a.getyVelocity();
                		if (!inArena(a) || isHit(a, playerGrid)) {
                			it.remove();
                		}
                	}
                	
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
                    Thread.sleep(BROADCAST_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }    
    }
    
    private static boolean inArena(Attack a) {
    	float x = a.getxPos();
    	float y = a.getyPos();
    	if (x < 0 || x >= ARENA_WIDTH || y < 0 || y >= ARENA_HEIGHT) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    private static void assignPlayersToGrid(int[][] grid, Set<Player> players) {
    	for (Player p: players) {
    		int row = ((int) p.getxPos()) / CELL_SIZE;
     		int col = ((int) p.getyPos()) / CELL_SIZE;
     		grid[row][col] = p.getId();
    	}
    }
    
    // checks each surrounding cell of 
    private static boolean isHit(Attack a, int[][] grid) {
    	int row = ((int) a.getxPos()) / CELL_SIZE;
 		int col = ((int) a.getyPos()) / CELL_SIZE;
    	for (int i = -1; i < 1; i++) {
    		for (int j = -1; j < 1; j++) {
    			int x = row + i;
    			int y = col + j;
    			if (inBounds(x, y, grid) && (grid[x][y] != 0)) {
    				int playerHitId = grid[x][y];
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private static boolean inBounds(int x, int y, int[][] grid) {
    	if (x < 0 || x >= grid.length || y < 0 || y > grid[0].length) {
    		return false;
    	} else {
    		return true;
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
