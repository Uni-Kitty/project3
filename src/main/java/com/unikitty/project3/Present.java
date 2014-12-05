package com.unikitty.project3;

public class Present extends GameEntity {
	private String type;
	private long startTime;
	
	public Present() {}
	
	public Present(float x, float y, String type, long sTime) {
		xPos = x;
		yPos = y;
		this.type = type;
		setStartTime(sTime);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
