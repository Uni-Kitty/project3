package com.unikitty.project3;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.ObjectMapper;

public class PlayerPinger implements Runnable {
    
    public static final int BUFFER_SIZE = 10;
    public static final long PING_DELAY = 1000;
    public static final String PING = "ping";
    
    private static ObjectMapper mapper = new ObjectMapper();
    
    private ConcurrentHashMap<Integer, FixedSizeQueue<Long>> playerPings;
    
    public PlayerPinger() {
        playerPings = new ConcurrentHashMap<Integer, FixedSizeQueue<Long>>();
    }
    
    public void addPlayer(int id) {
        playerPings.put(id, new FixedSizeQueue<Long>(BUFFER_SIZE));
    }
    
    public void recordPing(int id, long time) {
        time = System.currentTimeMillis() - time;
        if (playerPings.containsKey(id)) {
            playerPings.get(id).add(time);
        }
    }
    
    private void recordAverages() {
        HashSet<Integer> inactiveIDS = new HashSet<Integer>();
        for (int id : playerPings.keySet()) {
            if (!Main.isPlayerActive(id)) {
                inactiveIDS.add(id);
            }
            else if (!playerPings.get(id).isEmpty()) {
                long sum = 0;
                int count = 0;
                for (long time : playerPings.get(id)) {
                    sum += time;
                    count++;
                }
                Main.recordTime(id, sum / count);
            }
        }
        for (int id : inactiveIDS)
            playerPings.remove(id);
    }
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(PING_DELAY);
                recordAverages();
                Message<Long> msg = new Message<Long>();
                msg.setType(PING);
                msg.setData(System.currentTimeMillis());
                String message = mapper.writeValueAsString(msg);
                for (int id : playerPings.keySet())
                    Main.sendMessageToPlayer(id, message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

}
