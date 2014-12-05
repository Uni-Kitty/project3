package com.unikitty.project3;

import java.util.Random;

public class PresentBuilder implements Runnable {
	private static final String[] POSSIBLE_PRESENTS = {"ammo", "health"};
	private static final Random randy = new Random();
	
	private Game game;
	private int arenaWidth;
	private int arenaHeight;
	private static final int PRESENT_DELAY = 5000;
	
	public PresentBuilder(Game g, int width, int height) {
		game = g;
		arenaWidth = width;
		arenaHeight = height;
	}
	
	public void run() {
		while(true) {
			try {
				int presentIndex = randy.nextInt(POSSIBLE_PRESENTS.length);
				// generate x and y in the middle of the arena
				float x = randy.nextInt(arenaWidth / 2) + arenaWidth / 4;
				float y = randy.nextInt(arenaHeight / 2) + arenaHeight / 4;
				int imageNum = randy.nextInt(4);
				Present gamePresent = new Present(x, y, POSSIBLE_PRESENTS[presentIndex],
									System.currentTimeMillis(), imageNum);
				gamePresent.setId(Main.getNextId());
				game.addPresent(gamePresent);
				long delay = randy.nextInt(PRESENT_DELAY) + 5000;
				Thread.sleep(delay);
			} catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
}
