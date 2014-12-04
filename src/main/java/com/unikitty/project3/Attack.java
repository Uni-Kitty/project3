package com.unikitty.project3;

public class Attack extends GameEntity {
	
    private int ownerID;
    private int atkDmg;
    private String type;
    private float rotation;
    
    public static final String FIREBALL = "fireball";
    public static final String ARROW = "arrow";
    
    public Attack() {}

    public Attack(int owner, int attackID, float x, float y, float xVel, float yVel, int pwr) {
        ownerID = owner;
        id = attackID;
        xPos = x;
        yPos = y;
        xVelocity = xVel;
        yVelocity = yVel;
        atkDmg = pwr;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

	public int getAtkDmg() {
		return atkDmg;
	}

	public void setAtkDmg(int atkDmg) {
		this.atkDmg = atkDmg;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
}