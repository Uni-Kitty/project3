package com.unikitty.project3;

public class Player extends GameEntity {
	
    public Player() {}
    
	// takes initial x and y and player ID
	public Player(float x, float y, int identifier, float xVel, float yVel) {
		xPos = x;
		yPos = y;
		id = identifier;
		xVelocity = xVel;
		yVelocity = yVel;
	}
}
