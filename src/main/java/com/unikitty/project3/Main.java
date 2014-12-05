package com.unikitty.project3;

import java.util.Queue;
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
    public static final String JOIN_GAME = "join_game";
    public static final String PLAYER_UPDATE = "player_update";
    public static final String PLAYER_JOINED = "player_joined";
    public static final int ARENA_WIDTH = 800;
    public static final int ARENA_HEIGHT = 600;
    public static final int CELL_SIZE = 5;
    public static final int HIT_DISTANCE = 20;
    public static final int PING_BUFFER_SIZE = 5;
    
	private static Game game = new Game(); // the state of the game
	private static ConcurrentMap<Integer, Player> playersInGame = new ConcurrentHashMap<Integer, Player>();
	private static ConcurrentMap<Integer, Attack> attacksInGame = new ConcurrentHashMap<Integer, Attack>();
	private static ConcurrentMap<Integer, Session> playerSessions = new ConcurrentHashMap<Integer, Session>();
	private static ConcurrentMap<Integer, Queue<Long>> playerPings = new ConcurrentHashMap<Integer, Queue<Long>>();
	private static ObjectMapper mapper = new ObjectMapper(); // mapper for obj<-->JSON
	private static AtomicInteger id = new AtomicInteger(1); // avoid race conditions, use atomic int for ids
	
	static {
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	}
	
    public static void main( String[] args ) {
        startGame();
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
        	// send welcome message
        	// send ping
        	// start broadcasting gamegit 
        	
        	// create temp player for the new connection ID
        	int id = getNextId();
        	
            // send welcome message
            Message<Object> m = new Message<Object>();
            m.setId(id);
            m.setType(WELCOME);
            try {
                session.getRemote().sendStringByFuture(mapper.writeValueAsString(m));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // add session to active sessions so player gets game broadcasts
            playerSessions.put(id, session);
            playerPings.put(id, new FixedSizeQueue<Long>(PING_BUFFER_SIZE));
            // send first ping
            // ping(session);
            // TODO: work on pinging system
            
            System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        }
        
        public void ping(Session session) {
        	Message<Object> m = new Message<Object>();
        	m.setType(PING);
            m.setData(new Long(System.currentTimeMillis()));
            try {
                session.getRemote().sendStringByFuture(mapper.writeValueAsString(m));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            try {
            	Message<Object> msg = mapper.readValue(message, new TypeReference<Message<Object>>() {});
                switch (msg.getType()) {
	                case (PING):
	                    // Session s = playerSessions.get(msg.getId());
	                	long start = (long) msg.getData();
	                	playerPings.get(msg.getId()).add(System.currentTimeMillis() - start);
	                    break;
	                case (ATTACK):
	                	//System.out.println(message);
	                	Message<Attack> m = mapper.readValue(message, new TypeReference<Message<Attack>>() {});
	                	Attack a = m.getData();
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
	                	Player update = m2.getData();
	                	int id = update.getId();
	                	if (playersInGame.containsKey(id)) {
		                	Player player = playersInGame.get(id);
		                	updatePlayerInfo(player, update);
	                	}
	                	break;
	                case (JOIN_GAME):
	                	Message<Player> m3 = mapper.readValue(message, new TypeReference<Message<Player>>() {});
	                	Player playerInfo = m3.getData();
	                	Player newPlayer = new Player(playerInfo.getUsername(), playerInfo.gettype(), m3.getId());
	                	newPlayer.setxPos(ARENA_WIDTH / 2);
	                	newPlayer.setyPos(ARENA_HEIGHT / 2);
	                	game.addPlayer(newPlayer);
	                	playersInGame.put(newPlayer.getId(), newPlayer);
	                	m3.setType(PLAYER_JOINED);
	                	m3.setData(newPlayer);
	                	System.out.println("Player " + newPlayer.getUsername() + " joined");
	                	playerSessions.get(m3.getId()).getRemote().sendString(mapper.writeValueAsString(m3));
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
        new Thread(new GameBroadcaster(game, playerSessions.values(), BROADCAST_DELAY, UPDATE, mapper)).start();
    }
    
    private static void startGame() {
        new Thread(new PresentBuilder(game, ARENA_WIDTH, ARENA_HEIGHT)).start();
        new Thread(new GameRunner(game, attacksInGame, playersInGame, PLAYER_TIMEOUT, BROADCAST_DELAY, ARENA_WIDTH, ARENA_HEIGHT, HIT_DISTANCE)).start();
    }
    
    // Fetches the next unique id
    public static int getNextId() {
        return id.incrementAndGet();
    }   
}
