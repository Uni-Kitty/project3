package com.unikitty.jackson;

public class Attack extends GameEntity {
	private int ownerID;
	
	public Attack(int owner, int attackID) {
		id = attackID;
		ownerID = owner;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}	
}
