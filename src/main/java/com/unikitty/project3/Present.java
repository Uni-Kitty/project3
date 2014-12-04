package com.unikitty.project3;

public class Present extends GameEntity {
	private String type;
	
	public Present() {}
	
	public Present(float x, float y, String type) {
		xPos = x;
		yPos = y;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
