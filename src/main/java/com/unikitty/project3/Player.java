package com.unikitty.project3;

public class Player extends GameEntity {
	
  private int maxHP;
  private int currHP;
  private int atkDmg;
  private int ammo;
  private int kills;
  private int hitCount;
  private String type;
  private String username;
  private String color;
  private long lastUpdate;
  private long rtt;
  
  public static final String WIZARD = "wizard";
  public static final String RANGER = "ranger";
  public static final String HAPPY_KITTY = "happy_kitty";
  public static final String ANGRY_KITTY = "angry_kitty";

	public Player() {}
	
	// takes initial x and y and player ID
	public Player(float x, float y, int identifier, float xVel, float yVel) {
		xPos = x;
		yPos = y;
		id = identifier;
		xVelocity = xVel;
		yVelocity = yVel;
	}
	
	public Player(String name, String type, int id) {
	    this.id = id;
	    this.username = name;
	    this.type = type;
	    this.color = Color.COLORS[id % 7];
	    switch (type) {
	    case (WIZARD):
	        maxHP = 10;
	        currHP = 10;
	        atkDmg = 5;
	        ammo = 20;
	        break;
	    case (RANGER):
	        maxHP = 15;
	        currHP = 15;
	        atkDmg = 3;
	        ammo = 30;
	        break;
	    case (HAPPY_KITTY):
	    case (ANGRY_KITTY):
	    	//TODO: set x and y position
	    	maxHP = 99999;
	    	currHP = 99999;
	    	atkDmg = 7;
	    	ammo = 99999;
	    }
	    lastUpdate = System.currentTimeMillis();
	}
	
	public void incMaxHP(int amount) {
	    maxHP = Math.max(1, maxHP + amount);
	    currHP = Math.min(currHP, maxHP);
	}
	
	public void incDmg(int amount) {
	    atkDmg += amount;
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
	  this.currHP = Math.min(this.currHP + amt, this.maxHP);
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

	public long getRtt() {
		return rtt;
	}

	public void setRtt(long rtt) {
		this.rtt = rtt;
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
