package com.unikitty.project3;

import java.util.Random;

public class PresentBuilder implements Runnable {
	private static final String[] POSSIBLE_PRESENTS = {"food", "health"};
	private static final Random randy = new Random();
	
	private Game game;
	private int arenaWidth;
	private int arenaHeight;
	private int presentDelay;
	
	public PresentBuilder(Game g, int width, int height, int delay) {
		game = g;
		arenaWidth = width;
		arenaHeight = height;
		presentDelay = delay;
	}
	
	public void run() {
		while(true) {
			try {
				int presentIndex = randy.nextInt(POSSIBLE_PRESENTS.length);
				// generate x and y in the middle of the arena
				float x = randy.nextInt(arenaWidth / 2) + arenaWidth / 4;
				float y = randy.nextInt(arenaHeight / 2) + arenaHeight / 4;
				Present gamePresent = new Present(x, y, POSSIBLE_PRESENTS[presentIndex]);
				game.addPresent(gamePresent);
				Thread.sleep(presentDelay);
			} catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
}
