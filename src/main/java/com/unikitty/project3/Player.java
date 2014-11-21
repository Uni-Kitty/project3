package com.unikitty.project3;

import java.net.InetAddress;

public class Player {
	private InetAddress IPAddress;
	private int port;
	private PlayerEntity myGameState;
	
	public Player(InetAddress IPAddress, int port, int id) {
		this.IPAddress = IPAddress;
		this.port = port;
		myGameState = new PlayerEntity(5, 5, id);
	}
	
	public int getPort() {
		return port;
	}
	
	public InetAddress getIPAddress() {
		return IPAddress;
	}

	public PlayerEntity getGameState() {
		return myGameState;
	}
}
