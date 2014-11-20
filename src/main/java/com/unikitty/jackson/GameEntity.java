package com.unikitty.jackson;

public class GameEntity {
	protected int id;
	protected int xPos;
	protected int yPos;
	protected int direction;
	protected int speed;
	
	public GameEntity() {}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int degree) {
		assert(direction > 0 && direction < 361);
		direction = degree;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int s) {
		// check values later once range decided
		speed = s;
	}
	
	public int getxPos() {
        return xPos;
    }
    
    public void setxPos(int xPos) {
        this.xPos = xPos;
    }
    
    public int getyPos() {
        return yPos;
    }
    
    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
