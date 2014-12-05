package com.unikitty.project3;

public class PlayerPinger implements Runnable {
    
    public static long PING_DELAY = 2000;
    
    public void run() {
        while (true) {
            try {
                
                
                Thread.sleep(PING_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

}
