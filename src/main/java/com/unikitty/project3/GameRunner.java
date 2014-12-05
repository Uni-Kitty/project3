package com.unikitty.project3;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

// updates the state of the game by moving attacks, registering hits, and removing inactive players
public class GameRunner implements Runnable {
    
    private Player player;
    private Game game;
    private ConcurrentMap<Integer, Attack> attacksInGame;
    private ConcurrentMap<Integer, Player> playersInGame;
    private static long playerTimeout;;
    private static int broadcastDelay;
	private static int arenaWidth;
	private static int arenaHeight;
	private static int hitDistance;
	
	private static final long PRES_TIMEOUT = 30000;
    
    
    public GameRunner(Player p, Game g, ConcurrentMap<Integer, Attack> attacks, 
    		           ConcurrentMap<Integer, Player> players, long pTime, int bDel, int aW, int aH, int hD) {
        this.player = p;
        game = g;
        attacksInGame = attacks;
        playersInGame = players;
        playerTimeout = pTime;
        broadcastDelay = bDel;
        arenaWidth = aW;
        arenaHeight = aH;
        hitDistance = hD;
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
            		// update attacks
                	Iterator<Attack> it = game.getAttacks().iterator();
                	while(it.hasNext()) {
                		Attack a = it.next();
                		a.xPos += a.getxVelocity();
                		a.yPos += a.getyVelocity();
                		if (!inArena(a) || /* isHit(a, playerGrid) */ hitPlayer(a, game.getPlayers())) {
                			attacksInGame.remove(a.getId());
                			it.remove();
                		}
                	}
                	// update presents
                	Iterator<Present> iterPresents = game.getPresents().iterator();
                	while(iterPresents.hasNext()) {
                		Present pres = iterPresents.next();
                		if (playerOnPresent(pres, game.getPlayers()) || presentExpired(pres)) {
                			iterPresents.remove();
                		}
                	}
                	// delete inactive players
                	long currentTime = System.currentTimeMillis();
                	Iterator<Player> iter = game.getPlayers().iterator();
                	while (iter.hasNext()) {
                		Player p = iter.next();
                		if (currentTime - p.getLastUpdate() > playerTimeout) {
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
                Thread.sleep(broadcastDelay);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void sendDisconnectedMessage(Player p) {
    	// TODO
    }

    // Simple O(P) isHit
    // returns if it hit a player
    // updates player status based on hit
    private boolean hitPlayer(Attack a, Set<Player> players) {
    	for (Player p : players) {
    		if (a.getOwnerID() != p.getId()) {
        		float xDelta = Math.abs(a.getxPos() - p.getxPos());
        		float yDelta = Math.abs(a.getyPos() - p.getyPos());
        		if (hit(xDelta, yDelta)) {
        			registerAttack(a, p);
        			return true;
        		}
    		}
    	}
    	return false;
    }

    // returns if player received present
    private boolean playerOnPresent(Present pres, Set<Player> players) {
    	for (Player play : players) {
    		float xDelta = Math.abs(pres.getxPos() - play.getxPos());
    		float yDelta = Math.abs(pres.getyPos() - play.getyPos());
    		if (hit(xDelta, yDelta)) {
    			givePresent(pres, play);
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean presentExpired(Present pres) {
    	return System.currentTimeMillis() - pres.getStartTime() > PRES_TIMEOUT;
    }
    
    private void givePresent(Present pres, Player winner) {
    	String presType = pres.getType();
    	if (presType == "ammo") {
    		winner.incAmmo(10);
    	} else  if (presType == "health") {
    		winner.incHP(6);
    	}
    }

    private boolean hit(float xDelta, float yDelta) {
    	return xDelta < hitDistance && yDelta < hitDistance;
    }

    private boolean inArena(Attack a) {
    	float x = a.getxPos();
    	float y = a.getyPos();
    	return x > 0 && x < arenaWidth && y > 0 && y < arenaHeight;
    }

    // handle attack a hitting player p
    private void registerAttack(Attack a, Player p) {
    	Player attackOwner = playersInGame.get(a.getOwnerID());
    	p.decHP(attackOwner.getatkDmg());
    	attackOwner.incHitCount();
    	if (p.getcurrHP() <= 0) {
    		playersInGame.remove(p.getId());
    		game.removePlayer(p);
    		sendDisconnectedMessage(p);
    		attackOwner.incKills();
    	}
    }
}

