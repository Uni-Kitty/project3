package com.unikitty.project3;

public class ACK {
	String type;
	Long lastACKStart;
	int ACKcount;
	
	public ACK(String type, Long last) {
		this.type = type;
		this.lastACKStart = last;
		this.ACKcount = 1;
	}
	
	public void incCount() {
		ACKcount++;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getLastACKStart() {
		return lastACKStart;
	}

	public void setLastACKStart(Long lastACKStart) {
		this.lastACKStart = lastACKStart;
	}

	public int getACKcount() {
		return ACKcount;
	}

	public void setACKcount(int aCKcount) {
		ACKcount = aCKcount;
	}
}
