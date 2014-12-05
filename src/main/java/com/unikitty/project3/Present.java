package com.unikitty.project3;

public class Present extends GameEntity {
	private String type;
	private long startTime;
	private int imageNum;
	
	public Present() {}
	
	public Present(float x, float y, String type, long sTime, int image) {
		xPos = x;
		yPos = y;
		this.type = type;
		setStartTime(sTime);
		setImageNum(image);
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

	public int getImageNum() {
		return imageNum;
	}

	public void setImageNum(int imageNum) {
		this.imageNum = imageNum;
	}
}
