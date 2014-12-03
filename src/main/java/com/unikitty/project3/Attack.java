package com.unikitty.project3;

public class Attack extends GameEntity {
	private int ownerID;
	
	public Attack(int owner, int attackID, float x, float y, float xVel, float yVel) {
		ownerID = owner;
		id = attackID;
		xPos = x;
		yPos = y;
		xVelocity = xVel;
		yVelocity = yVel;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}	
}
