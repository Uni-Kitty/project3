package com.unikitty.project3;

public class Player extends GameEntity {
	
    public Player() {}
    
	// takes initial x and y and player ID
	public Player(int x, int y, int identifier) {
		xPos = x;
		yPos = y;
		id = identifier;
	}
}
