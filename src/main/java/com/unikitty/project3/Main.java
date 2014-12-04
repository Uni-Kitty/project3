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
    public static final int BROADCAST_DELAY = 20; // Game loop delay in ms
    public static final long PLAYER_TIMEOUT = 5000; // Time to wait before dropping player in ms
    public static final String PING = "ping";
    public static final String ATTACK = "attack";
    public static final String UPDATE = "update";
    public static final String WELCOME = "welcome";
    public static final String PLAYER_UPDATE = "player_update";
    public static final int ARENA_WIDTH = 1000;
    public static final int ARENA_HEIGHT = 600;
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
            Player newPlayer = createNewPlayer(Player.WIZARD);
            game.addPlayer(newPlayer);
            playersInGame.put(newPlayer.getId(), newPlayer);
            playerSessions.put(newPlayer.getId(), session);
            Message<Object> m = new Message<Object>();
            m.setId(newPlayer.getId());
            m.setType(WELCOME);
            try {
                session.getRemote().sendStringByFuture(mapper.writeValueAsString(m));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            try {
            	Message<Object> msg = mapper.readValue(message, new TypeReference<Message<Object>>() {});
                switch (msg.getType()) {
	                case (PING): // this is a ping request, send the same message back to correct player
	                    Session s = playerSessions.get(msg.getId());
	                    s.getRemote().sendStringByFuture(message);
	                    break;
	                case (ATTACK):
	                	//System.out.println(message);
	                	Message<Attack> m = mapper.readValue(message, new TypeReference<Message<Attack>>() {});
	                	Attack a = (Attack) m.getData();
	                	a.setId(getNextId());
	                	Player attackOwner = playersInGame.get(a.getOwnerID());
	                	synchronized (game) {
		                	int ammo = attackOwner.getammo();
		                	if (ammo > 0 ) {
		                		//System.out.println(message);
			                	attackOwner.setammo(ammo - 1);
			                	game.addAttack(a);
			                	attacksInGame.put(a.getId(), a);
		                	}
	                	}
	                	break;
	                case (PLAYER_UPDATE):
	                	//Player newInfo = mapper.readValue(m.getData(), Player.class);
	                	Message<Player> m2 = mapper.readValue(message, new TypeReference<Message<Player>>() {});
	                	Player update = (Player) m2.getData();
	                	int id = update.getId();
	                	if (playersInGame.containsKey(id)) {
		                	Player player = playersInGame.get(id);
		                	updatePlayerInfo(player, update);
	                	}
	                	break;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void updatePlayerInfo(Player player, Player update) {
    	synchronized (player) {
	    	player.setxPos(update.getxPos());
	    	player.setyPos(update.getyPos());
	    	player.setxVelocity(update.getxVelocity());
	    	player.setyVelocity(update.getyVelocity());
	    	player.setLastUpdate(System.currentTimeMillis());
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
        Player p1 = createNewPlayer(Player.WIZARD);
        game.addPlayer(p1);
        p1.setxPos(200);
        p1.setyPos(200);
        new Thread(new GameRunner(p1)).start();
    }
    
    // This thing broadcasts to all active sessions
    private static class GameBroadcaster implements Runnable {
        public void run() {
            while (true) {
                try {
                	String message = "";
                	synchronized (game) {
	                    Message<Game> m = new Message<Game>();
	                    m.setType(UPDATE);
	                    m.setData(game);
	                    message = mapper.writeValueAsString(m);
                	}
                    for (Session session : playerSessions.values()) {
                        if (session.isOpen()) { // TODO: handle this better, do we disconnect player if session goes bad?
                            session.getRemote().sendStringByFuture(message);
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
                	/*
                	int[][] playerGrid = new int[ARENA_HEIGHT / CELL_SIZE][ARENA_WIDTH / CELL_SIZE];
                	assignPlayersToGrid(playerGrid, game.getPlayers());
                	// update Attack positions and register any hits
                	*/
                	synchronized (game) {
	                	Iterator<Attack> it = game.getAttacks().iterator();
	                	while(it.hasNext()) {
	                		Attack a = it.next();
	                		a.xPos += a.getxVelocity();
	                		a.yPos += a.getyVelocity();
	                		if (!inArena(a) || /* isHit(a, playerGrid) */ isHitSimple(a, game.getPlayers())) {
	                			attacksInGame.remove(a.getId());
	                			it.remove();
	                		}
	                	}
	                	// delete inactive players
	                	long currentTime = System.currentTimeMillis();
	                	Iterator<Player> iter = game.getPlayers().iterator();
	                	while (iter.hasNext()) {
	                		Player p = iter.next();
	                		if (currentTime - p.getLastUpdate() > PLAYER_TIMEOUT) {
	                			iter.remove();
	                			playersInGame.remove(p.getId());
	                			sendDisconnectedMessage(p);
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
	                    player.setLastUpdate(System.currentTimeMillis());
                	}
                    Thread.sleep(BROADCAST_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }    
    }
    
    private static void sendDisconnectedMessage(Player p) {
    	// TODO
    }
    
    // Simple O(P) isHit
    private static boolean isHitSimple(Attack a, Set<Player> players) {
    	for (Player p : players) {
    		if (a.getOwnerID() != p.getId()) {
	    		float xDelta = Math.abs(a.getxPos() - p.getxPos());
	    		float yDelta = Math.abs(a.getyPos() - p.getyPos());
	    		if (xDelta < 20 && yDelta < 20) {
	    			// hit
	    			attackHitPlayer(a, p);
	    			return true;
	    		}
    		}
    	}
    	return false;
    }
    
    // are x and y in array bounds
    private static boolean inBounds(int x, int y, int[][] grid) {
    	return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
    }
    
    private static boolean inArena(Attack a) {
    	float x = a.getxPos();
    	float y = a.getyPos();
    	return x > 0 && x < ARENA_WIDTH && y > 0 && y < ARENA_HEIGHT;
    }
    
    private static void assignPlayersToGrid(int[][] grid, Set<Player> players) {
    	for (Player p: players) {
    		int row = Math.min(grid.length - 1, ((int) p.getxPos()) / CELL_SIZE);
     		int col = Math.min(grid[0].length - 1, ((int) p.getyPos()) / CELL_SIZE);
     		grid[row][col] = p.getId();
    	}
    }
    
    // checks each surrounding cell of 
    private static boolean isHit(Attack a, int[][] grid) {
    	int row = ((int) a.getxPos()) / CELL_SIZE;
 		int col = ((int) a.getyPos()) / CELL_SIZE;
    	for (int i = -1; i <= 1; i++) {
    		for (int j = -1; j <= 1; j++) {
    			int x = row + i;
    			int y = col + j;
    			if (inBounds(x, y, grid) && (grid[x][y] != 0)) {
    				int playerHitId = grid[x][y];
    				Player playerHit = playersInGame.get(playerHitId);
    				attackHitPlayer(a, playerHit);
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    // handle attack a hitting player p
    private static void attackHitPlayer(Attack a, Player p) {
    	Player attackOwner = playersInGame.get(a.getOwnerID());
		p.setcurrHP(p.getcurrHP() - a.getAtkDmg());
		attackOwner.sethitCount(attackOwner.gethitCount() + 1);
		if (p.getcurrHP() <= 0) {
			// kill
			attackOwner.setkills(attackOwner.getkills() + 1);
		}
    }
    
    private static Player createNewPlayer(String type) {
        Player p = new Player();
        p.setId(getNextId());
        p.settype(type);
        p.setammo(100);
        p.setmaxHP(10);
        p.setcurrHP(10);
        p.setatkDmg(2);
        p.setLastUpdate(System.currentTimeMillis());
        return p;
    }
    
    // Fetches the next unique id
    private static int getNextId() {
        return id.incrementAndGet();
    }   
}
