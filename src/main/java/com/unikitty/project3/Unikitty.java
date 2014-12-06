package com.unikitty.project3;

import java.util.Random;

public class Unikitty implements Runnable {
	private Game game;
	private Random randy = new Random();
	
	public Unikitty(Game g) {
		game = g;
	}
	
	public void run() {
		while(true) {
			try {
				unikittyGo();
				int sleep = randy.nextInt(10000);
				Thread.sleep(sleep + 7000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void unikittyGo() {
		Player kitty = new Player("polly", "unikitty", 42);
		game.addPlayer(kitty);
	}
}
