package com.unikitty.project3;

public class Attack extends GameEntity {
    
    public static final String FIREBALL = "fireball";
    public static final String ARROW = "arrow";
    
	private int ownerID;
	private String type;
	
	public Attack() {}
	
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }	
}
