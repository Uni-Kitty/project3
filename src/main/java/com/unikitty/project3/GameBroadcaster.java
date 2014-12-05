package com.unikitty.project3;

import java.util.Collection;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;

//This thing broadcasts to all active sessions
public class GameBroadcaster implements Runnable {
	private Game game;
	private Collection<Session> playerSessions;
	private ObjectMapper mapper;
	private String messageType;
	private int broadcastDelay;
	
	
	public GameBroadcaster(Game g, Collection<Session> sessions, 
						   		   int delay, String mType, ObjectMapper map) {
		game = g;
		playerSessions = sessions;
		broadcastDelay = delay;
		messageType = mType;
		mapper = map;
	}
	
	
    public void run() {
        while (true) {
            try {
            	String message = "";
            	synchronized (game) {
                    Message<Game> m = new Message<Game>();
                    m.setType(messageType);
                    m.setData(game);
                    message = mapper.writeValueAsString(m);
            	}
                for (Session session : playerSessions) {
                    if (session.isOpen()) { // TODO: handle this better, do we disconnect player if session goes bad?
                        session.getRemote().sendStringByFuture(message);
                    }
                }
                Thread.sleep(broadcastDelay);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}