package com.unikitty.project3;

public class PlayerEntity extends GameEntity {
	
	// takes initial x and y and player ID
	public PlayerEntity(int x, int y, int identifier) {
		xPos = x;
		yPos = y;
		id = identifier;
	}
}
