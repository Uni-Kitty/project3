package com.unikitty.project3;

public class Attack extends GameEntity {
    private int ownerID;
    private int atkDmg;

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
    
    public int getAtkPwr() {
        return atkDmg;
    }

    public void setAtkPwr(int atkDmg) {
        this.atkDmg = atkDmg;
    }
}