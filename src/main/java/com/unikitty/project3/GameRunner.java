package com.unikitty.project3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.jackson.map.ObjectMapper;

// updates the state of the game by moving attacks, registering hits, and removing inactive players
public class GameRunner implements Runnable {
    
    private Game game;
    private ConcurrentMap<Integer, Attack> attacksInGame;
    private ConcurrentMap<Integer, Player> playersInGame;
    private ConcurrentMap<Integer, ACK> outstandingACKWelcome;
    private ConcurrentMap<Integer, ACK> outstandingACKDied;
    private static int broadcastDelay;
	private static int arenaWidth;
	private static int arenaHeight;
	private static int hitDistance;
	private static Player happyKitty = new Player("UNIKITTY", Player.HAPPY_KITTY, Main.getNextId());
	private static Player angryKitty = new Player("UNIKITTY", Player.ANGRY_KITTY, Main.getNextId());
	private static ObjectMapper mapper = new ObjectMapper();
	private static Random randy = new Random();
	private static float[] ANGRY_ATTACK = {-10, 0, 10};
	
	private static final long PRES_TIMEOUT = 30000;
	private static final float WALL_COL_DIST = 10;
	private static final long GHOST_DELAY = 5000;
	private static final String WELCOME = "welcome";
    public static final long ACK_TIMEOUT = 1000;
	
	public static final String YOU_HAVE_DIED = "you_have_died";
    
    public GameRunner(Game g, ConcurrentMap<Integer, Attack> attacks, 
    		           ConcurrentMap<Integer, Player> players, ConcurrentMap<Integer, ACK> outAW, ConcurrentMap<Integer, ACK> outAD,
    		           long pTime, int bDel, int aW, int aH, int hD) {
        game = g;
        attacksInGame = attacks;
        playersInGame = players;
        broadcastDelay = bDel;
        arenaWidth = aW;
        arenaHeight = aH;
        hitDistance = hD;
        game.addPlayer(happyKitty);
        game.addPlayer(angryKitty);
        outstandingACKWelcome = outAW;
        outstandingACKDied = outAD;
    }
    
    public void run() {
        happyKitty.setxPos(-100);
        happyKitty.setyPos(Main.ARENA_HEIGHT / 2);
        angryKitty.setyPos(-100);
        angryKitty.setxPos(Main.ARENA_WIDTH / 2);
        int step = 0;
        Message<String> angryKittyMessage = new Message<String>();
        Message<String> happyKittyMessage = new Message<String>();
        angryKittyMessage.setType("chat");
        happyKittyMessage.setType("chat");
        angryKittyMessage.setData("Here comes ANGRY KITTY!!");
        happyKittyMessage.setData("Here comes HAPPY KITTY!!");
        String angryKittyMsg = "";
        String happyKittyMsg = "";
        try {
            angryKittyMsg = mapper.writeValueAsString(angryKittyMessage);
            happyKittyMsg = mapper.writeValueAsString(happyKittyMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                step++;
                long timeSum = 0; // track average loop time
                long time = System.nanoTime();
            	synchronized (game) {
            		checkAcks();
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
                	if ((step > 510 && step < 590) || (step > 1510 && step < 1590)) {// happy kitty is running across screen
                		if (randy.nextInt(9) == 0) {  //chance of dropping present each step
                			dropPresents(happyKitty.getxPos() + randy.nextInt(80) - 40, happyKitty.getyPos());
                		}
            		}
                	if ((step > 1000 && step < 1100) || (step > 2000 && step < 2100)) {
                		if (randy.nextInt(4) == 0) {
                			angryKittyShoot();
                		}
                	}
                	if (step > 500 && step <= 600) {
                	    happyKitty.setxPos(happyKitty.getxPos() + 10);
                	}
                    else if (step > 1000 && step <= 1100) {
                        angryKitty.setyPos(angryKitty.getyPos() + 10);
                        angryCollision();
                	}
                	else if (step > 1500 && step <= 1600) {
                        happyKitty.setxPos(happyKitty.getxPos() - 10);
                	}
                    else if (step > 2000 && step <= 2100) {
                        angryKitty.setyPos(angryKitty.getyPos() - 10);
                        angryCollision();
                    }
                	if (step == 2100) {
                	    step = 0;
                	}
                	else if (step == 450 || step == 1450) {
                	    Main.broadcastMessage(happyKittyMsg);
                	}
                	else if (step == 1950 || step == 950) {
                	    Main.broadcastMessage(angryKittyMsg);
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
            	timeSum += System.nanoTime() - time;
            	if (step == 0) { // periodically log some performance info
            	    long avgTime = timeSum / 2100;
            	    System.out.println("Game loop avg time: " + avgTime + " ns with " + game.getPlayers().size() + " active players");
            	    timeSum = 0;
            	    for (Player p : playersInGame.values()) {
            	        System.out.println("Player " + p.getUsername() + ", rtt: " + p.getRtt());
            	    }
            	}
                Thread.sleep(broadcastDelay);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void checkAcks() {
    	synchronized(outstandingACKWelcome) {
    		Iterator<Integer> welcomeIT = outstandingACKWelcome.keySet().iterator();
	    	while (welcomeIT.hasNext()) {
	    		int id = welcomeIT.next();
	    		ACK a = outstandingACKWelcome.get(id);
	    		if (System.currentTimeMillis() - a.getLastACKStart() > ACK_TIMEOUT) {
	    			a.setLastACKStart(System.currentTimeMillis());
	    			a.incCount();
	    			if (a.getACKcount() > 10) {
	    				welcomeIT.remove();
	    			}
	    			try {
		    			Message<Object> m = new Message<Object>();
		                m.setId(id);
		                m.setType(WELCOME);
		                String message = mapper.writeValueAsString(m);
		                System.out.println("WELCOME RESENDING");
		    			Main.sendMessageToPlayer(id, message);
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	}
    	}
    	synchronized(outstandingACKDied) {
    		Iterator<Integer> diedIT = outstandingACKDied.keySet().iterator();
	    	while (diedIT.hasNext()) {
	    		int id = diedIT.next();
	    		// Player p = playersInGame.get(id);
	    		ACK a = outstandingACKDied.get(id);
	    		if (System.currentTimeMillis() - a.getLastACKStart() > 100) {
	    			a.setLastACKStart(System.currentTimeMillis());
	    			a.incCount();
	    			if (a.getACKcount() > 10) {
	    				diedIT.remove();
	    			}
	    			try {
		    			Message<Object> m = new Message<Object>();
		                m.setId(id);
		                m.setType(YOU_HAVE_DIED);
		                String message = mapper.writeValueAsString(m);
		                System.out.println("You_HAVE_DIED resending");
		    			Main.sendMessageToPlayer(id, message);
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	}
    	}
    }
    
    private void angryCollision() {
    	synchronized(game) {
            // detect collisions
    	    Set<Player> deadPlayers = new HashSet<Player>();
        	for (Player p : game.getPlayers()) {
        	    if (p.getId() != angryKitty.getId()) {
            		float xDelta = Math.abs(angryKitty.getxPos() - p.getxPos());
            		float yDelta = Math.abs(angryKitty.getyPos() - p.getyPos());
            		if (hit(xDelta, yDelta)) {
            			p.decHP(angryKitty.getatkDmg());
            			if (p.getcurrHP() <= 0) {
            				deadPlayers.add(p);
            			}
            		}
        	    }
            }
        	for (Player p : deadPlayers) {
        	    killPlayer(p, angryKitty);
        	}
    	}
    }
    
    private void angryKittyShoot() {
        synchronized(game) {
            // shoot randomly
	        int xIndex = randy.nextInt(ANGRY_ATTACK.length);
	        int yIndex = randy.nextInt(ANGRY_ATTACK.length);
	        float xVel = ANGRY_ATTACK[xIndex];
	        float yVel = ANGRY_ATTACK[yIndex];
	        Attack a = new Attack(angryKitty.getId(), Main.getNextId(), angryKitty.getxPos(), angryKitty.getyPos(), xVel, yVel, 100);
	        a.setType(Attack.LASER_BALL);
	        if (xVel != 0 || yVel != 0) {
	        	game.addAttack(a);
	        	attacksInGame.put(a.getId(), a);
	        } 
        }
    }
    
    private void dropPresents(float x, float y) {
		int imageNum = randy.nextInt(4);
		Present gamePresent = new Present(x, y, PresentBuilder.getRandomPresent(),
							System.currentTimeMillis(), imageNum);
		gamePresent.setId(Main.getNextId());
		game.addPresent(gamePresent);
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
    
    private static void sendDeadMessage(int id, String message) {
    	Message<String> msg = new Message<String>();
    	msg.setType(YOU_HAVE_DIED);
    	msg.setData(message);
    	msg.setId(id);
    	try {
    	    Main.sendMessageToPlayer(id, mapper.writeValueAsString(msg));
    	}
    	catch (Exception e) {
    	    
    	}
    }

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
    		if (hit(xDelta, yDelta) && (play.gettype() != "happy_kitty")) {
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
        synchronized(winner) {
        	String presType = pres.getType();
        	int amount = 0;
        	switch (presType) {
        	case ("ammo"):
        	    amount = randy.nextInt(20) - randy.nextInt(3);
        	    winner.incAmmo(amount);
        	    break;
        	case ("health"):
        	    amount = randy.nextInt(8) - randy.nextInt(3);
        	    winner.incHP(amount);
        	    break;
        	case ("max hp"):
        	    amount = randy.nextInt(8) - randy.nextInt(3);
        	    winner.incMaxHP(amount);
        	    break;
        	case ("atk"):
        	    amount = randy.nextInt(4) - randy.nextInt(2);
        	    winner.incDmg(amount);
        	    break;
        	}
        	String color = Color.GREEN;
        	String text = "";
        	if (amount >= 0)
        	    text += "+";
        	else
        	    color = Color.RED;
        	text += amount;
        	text += " " + presType;
        	Message<TextDisplay> msg = new Message<TextDisplay>();
        	msg.setType("text_display");
        	msg.setId(pres.getId());
        	msg.setData(new TextDisplay(text, 2000, Math.round(winner.getxPos()), Math.round(winner.getyPos())));
        	msg.getData().setColor(color);
        	try {
        	    Main.sendMessageToPlayer(winner.getId(), mapper.writeValueAsString(msg));        	    
        	}
        	catch (Exception e) {
        	    e.printStackTrace();
        	}
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
        if (a.getOwnerID() == p.getId()) // can't shoot yourself, keeps angry kitty from killing herself?
            return;
    	Player attackOwner;
    	if (a.getOwnerID() == angryKitty.getId())
    	    attackOwner = angryKitty;
    	else if (playersInGame.containsKey(a.getOwnerID())) {
    	    attackOwner = playersInGame.get(a.getOwnerID());
        	// System.out.println(attackOwner);
        	p.decHP(attackOwner.getatkDmg());
        	attackOwner.incHitCount();
        	if (p.getcurrHP() <= 0) {
        	    killPlayer(p, attackOwner);
        		attackOwner.incKills();
        	}
    	}
    }
    
    private void killPlayer(Player killed, Player killer) {
        sendDeadMessage(killed.getId(), "YOU WERE KILLED BY " + killer.getUsername());
        Message<String> msg = new Message<String>();
        msg.setType("chat");
        msg.setData(killer.getUsername() + " killed " + killed.getUsername());
        try {
        	outstandingACKDied.put(killed.getId(), new ACK("died", System.currentTimeMillis()));
            Main.broadcastMessage(mapper.writeValueAsString(msg));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        playersInGame.remove(killed.getId());
        game.removePlayer(killed);
        game.buildGrave(new DeadPlayer(killed));
    }
}

