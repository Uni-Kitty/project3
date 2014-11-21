package com.unikitty.project3;

import java.util.HashSet;
import java.util.Set;

public class Game {
	private Set<PlayerEntity> players = new HashSet<PlayerEntity>();
	private Set<Attack> attacks = new HashSet<Attack>(); 
	
	public Game() {}

	public void addAttack(Attack a) {
		attacks.add(a);
	}
	
	public void removeAttack(Attack a) {
		attacks.remove(a);
	}
	
	public void addPlayer(PlayerEntity p) {
		players.add(p);
	}
	
	public void removePlayer(Player p) {
		players.remove(p);
	}
	
	
	public Set<PlayerEntity> getPlayers() {
		return players;
	}

	public void setPlayers(Set<PlayerEntity> players) {
		this.players = players;
	}

	public Set<Attack> getAttacks() {
		return attacks;
	}

	public void setAttacks(Set<Attack> attacks) {
		this.attacks = attacks;
	}
	
}
