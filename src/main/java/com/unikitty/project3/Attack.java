package com.unikitty.project3;

public class Attack extends GameEntity {
	private int ownerID;
	
	public Attack(int owner, int attackID, int x, int y, int dir, int speed) {
		id = attackID;
		ownerID = owner;
		xPos = x;
		yPos = y;
		direction = dir;
		this.speed = speed;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}	
}
