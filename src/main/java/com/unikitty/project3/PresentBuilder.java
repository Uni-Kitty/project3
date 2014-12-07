package com.unikitty.project3;

import java.util.Random;

public class PresentBuilder implements Runnable {
    
	private static String[] POSSIBLE_PRESENTS;
	private static final Random randy = new Random();
	
	private Game game;
	private int arenaWidth;
	private int arenaHeight;
	private static final int PRESENT_DELAY = 5000;
	
	public PresentBuilder(Game g, int width, int height, String[] presents) {
		game = g;
		arenaWidth = width;
		arenaHeight = height;
		POSSIBLE_PRESENTS = presents;
	}
	
	public void run() {
		while(true) {
			try {
				// generate x and y in the middle of the arena
				float x = randy.nextInt(arenaWidth / 3) + arenaWidth / 3;
				float y = randy.nextInt(arenaHeight / 2) + arenaHeight / 4;
				addPresent(x, y);
				long delay = randy.nextInt(PRESENT_DELAY) + 5000;
				Thread.sleep(delay);
			} catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
	
	public void addPresent(float x, float y) {
		int presentIndex = randy.nextInt(POSSIBLE_PRESENTS.length);
		int imageNum = randy.nextInt(4);
		Present gamePresent = new Present(x, y, POSSIBLE_PRESENTS[presentIndex],
							System.currentTimeMillis(), imageNum);
		gamePresent.setId(Main.getNextId());
		synchronized (game) {
			game.addPresent(gamePresent);
		}
	}
}
