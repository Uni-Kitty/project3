package com.unikitty.project3;

public class DeadPlayer {
	private float xPos;
	private float yPos;
	private long deathTime;
	
	public DeadPlayer() {}
	
	public DeadPlayer(Player p) {
		xPos = p.getxPos();
		yPos = p.getyPos();
		deathTime = System.currentTimeMillis();
	}

	public float getxPos() {
		return xPos;
	}

	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	public float getyPos() {
		return yPos;
	}

	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	public long getDeathTime() {
		return deathTime;
	}

	public void setDeathTime(long deathTime) {
		this.deathTime = deathTime;
	}
}
