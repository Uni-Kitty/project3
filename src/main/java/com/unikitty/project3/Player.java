package com.unikitty.project3;

public class Player extends GameEntity {
	
  private int maxHP;
  private int currHP;
  private int atkDmg;
  private int ammo;
  private int kills;
  private int hitCount;
  private String type;
  private long lastUpdate;
  
  public static final String WIZARD = "wizard";
  public static final String RANGER = "ranger";

    public Player() {}
    
	// takes initial x and y and player ID
	public Player(float x, float y, int identifier, float xVel, float yVel) {
		xPos = x;
		yPos = y;
		id = identifier;
		xVelocity = xVel;
		yVelocity = yVel;
	}

  public int getmaxHP() {
    return maxHP;
  }

  public void setmaxHP(int maxHP) {
    this.maxHP = maxHP;
  }

  public int getcurrHP() {
    return currHP;
  }

  public void setcurrHP(int currHP) {
    this.currHP = currHP;
  }
  
  public void incHP(int amt) {
	  this.currHP += amt;
  }
  
  public void decHP(int amt) {
	  this.currHP -= amt;
  }

  public int getatkDmg() {
    return atkDmg;
  }

  public void setatkDmg(int atkDmg) {
    this.atkDmg = atkDmg;
  }

  public int getammo() {
    return ammo;
  }

  public void setammo(int ammo) {
    this.ammo = ammo;
  }
  
  public void incAmmo(int amt) {
	this.ammo += amt;
  }

  public void decAmmo(int amt) {
	this.ammo -= amt;
  }
  
  public int getkills() {
    return kills;
  }

  public void setkills(int kills) {
    this.kills = kills;
  }
  
  public void incKills() {	
	this.kills += 1;  
  }

  public int gethitCount() {
    return hitCount;
  }

  public void sethitCount(int hitCount) {
    this.hitCount = hitCount;
  }
  
  public void incHitCount() {
	this.hitCount += 1;
  }

  public String gettype() {
    return type;
  }

  public void settype(String type) {
    this.type = type;
  }

	public long getLastUpdate() {
		return lastUpdate;
	}
	
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
