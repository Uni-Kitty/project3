package com.unikitty.project3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Game {
    
	private Set<Player> players = Collections.synchronizedSet(new HashSet<Player>());
	private Set<Attack> attacks = Collections.synchronizedSet(new HashSet<Attack>()); 
	
	public Game() {}

	public void updatePlayer(Player oldInfo, Player newInfo) {
		players.remove(oldInfo);
		players.add(newInfo);
	}
	
	public void addAttack(Attack a) {
		attacks.add(a);
	}
	
	public void removeAttack(Attack a) {
		attacks.remove(a);
	}
	
	public void addPlayer(Player p) {
		players.add(p);
	}
	
	public void removePlayer(Player p) {
		players.remove(p);
	}
	
	public Set<Player> getPlayers() {
		return players;
	}

	public void setPlayers(Set<Player> players) {
		this.players = players;
	}

	public Set<Attack> getAttacks() {
		return attacks;
	}

	public void setAttacks(Set<Attack> attacks) {
		this.attacks = attacks;
	}
	
}
