package com.unikitty.project3;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

public class PlayerPinger implements Runnable {
    
    public static final int BUFFER_SIZE = 10;
    public static final long PING_DELAY = 1000;
    public static final String PING = "ping";
    
    private static ObjectMapper mapper = new ObjectMapper();
    
    private Set<Integer> ids;
    
    public PlayerPinger() {  
        ids = new HashSet<Integer>();
    }
    
    public void addPlayer(int id) {
        ids.add(id);
    }
    
    public void recordPing(int id, long time) {
        time = System.currentTimeMillis() - time;
        if (ids.contains(id)) {
            Main.recordTime(id, time);
        }
    }
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(PING_DELAY);
                Message<Long> msg = new Message<Long>();
                msg.setType(PING);
                msg.setData(System.currentTimeMillis());
                String message = mapper.writeValueAsString(msg);
                for (int id : ids)
                    Main.sendMessageToPlayer(id, message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

}
