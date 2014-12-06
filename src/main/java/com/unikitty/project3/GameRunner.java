package com.unikitty.project3;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

// updates the state of the game by moving attacks, registering hits, and removing inactive players
public class GameRunner implements Runnable {
    private Game game;
    private ConcurrentMap<Integer, Attack> attacksInGame;
    private ConcurrentMap<Integer, Player> playersInGame;
    private static long playerTimeout;
    private static int broadcastDelay;
	private static int arenaWidth;
	private static int arenaHeight;
	private static int hitDistance;
	
	private static final long PRES_TIMEOUT = 30000;
	private static final float WALL_COL_DIST = 10;
	private static final long GHOST_DELAY = 5000;
    
    
    public GameRunner(Game g, ConcurrentMap<Integer, Attack> attacks, 
    		           ConcurrentMap<Integer, Player> players, long pTime, int bDel, int aW, int aH, int hD) {
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
        while (true) {
            try {
            	synchronized (game) {
            		// update attacks
                	Iterator<Attack> it = game.getAttacks().iterator();
                	while(it.hasNext()) {
                		Attack a = it.next();
                		a.xPos += a.getxVelocity();
                		a.yPos += a.getyVelocity();
                		if (!inArena(a) || hitWall(a) || hitPlayer(a, game.getPlayers()) || hitPresent(a, game.getPresents())) {
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
                	// delete inactive players and move unikitties
                	long currentTime = System.currentTimeMillis();
                	Iterator<Player> iter = game.getPlayers().iterator();
                	while (iter.hasNext()) {
                		Player p = iter.next();
                		if (currentTime - p.getLastUpdate() > playerTimeout) {
                			iter.remove();
                			playersInGame.remove(p.getId());
                			sendDisconnectedMessage(p);
                		} else if (p.gettype() == "unikitty") {
                			// TODO: move and fire attacks from unikitty
                			// figure out when to remove them.. 
                			//p.setxPos(p.getxPos() + 1);
                			//Attack a = new Attack(0, 0, p.getxPos, p.getyPos(), )
                			//game.addAttack(a);
		                	//attacksInGame.put(a.getId(), a);
                		}
                	}
                	// delete deadPlayers after Timeout
                	Iterator<DeadPlayer> itGraves = game.getGraveYard().iterator();
                	while(itGraves.hasNext()) {
                		DeadPlayer nextGhost = itGraves.next();
                		if (System.currentTimeMillis() - nextGhost.getDeathTime() > GHOST_DELAY) {
                			itGraves.remove();
                		}
                	}
                	
            	}
                Thread.sleep(broadcastDelay);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean hitPresent(Attack a, Set<Present> presents) {
    	Iterator<Present> it = presents.iterator();
    	while(it.hasNext()) {
    		Present p = it.next();
    		float xDelta = Math.abs(p.getxPos() - a.getxPos());
    		float yDelta = Math.abs(p.getyPos() - a.getyPos());
    		if (hit(xDelta, yDelta)) {
    			it.remove();
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean hitWall(Attack a) {
    	return checkLineNE(190, 160, a) || checkLineNE(610, 440, a) || checkLineNW(610, 160, a) || checkLineNW(190, 440, a);
    }
    
    private boolean checkLineNE(int x, int y, Attack a) {
    	float x1 = (float) (x - 4 * 14.142);
    	float y1 = (float) (y + 4 * 14.142);
    	for(int i = 0; i < 9; i++) {
    		float xDelta = Math.abs(a.getxPos() - x1);
    		float yDelta = Math.abs(a.getyPos() - y1);
    		if (isWallCollision(xDelta, yDelta)) {
    			return true;
    		}
    		x1 += 14.142;
    		y1 -= 14.142;
    	}
    	return false;
    }
    
    private boolean checkLineNW(int x, int y, Attack a) {
    	float x1 = (float) (x + 4 * 14.142);
    	float y1 = (float) (y + 4 * 14.142);
    	for(int i = 0; i < 9; i++) {
    		float xDelta = Math.abs(a.getxPos() - x1);
    		float yDelta = Math.abs(a.getyPos() - y1);
    		if (isWallCollision(xDelta, yDelta)) {
    			return true;
    		}
    		x1 -= 14.142;
    		y1 -= 14.142;
    	}
    	return false;
    }
    
    private boolean isWallCollision(float xDelta, float yDelta) {
    	return xDelta < WALL_COL_DIST && yDelta < WALL_COL_DIST;
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
    		game.buildGrave(new DeadPlayer(p));
    		sendDisconnectedMessage(p);
    		attackOwner.incKills();
    	}
    }
}

