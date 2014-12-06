package com.unikitty.project3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Game {
	private Set<Player>  players  = Collections.synchronizedSet(new HashSet<Player>());
	private Set<Attack>  attacks  = Collections.synchronizedSet(new HashSet<Attack>()); 
	private Set<Present> presents = Collections.synchronizedSet(new HashSet<Present>());
	private Set<DeadPlayer> graveYard = Collections.synchronizedSet(new HashSet<DeadPlayer>());
	
	public Game() {}
	
	public void buildGrave(DeadPlayer p) {
		graveYard.add(p);
	}
	
	public Set<DeadPlayer> getGraveYard() {
		return graveYard;
	}

	public void setGraveYard(Set<DeadPlayer> graveYard) {
		this.graveYard = graveYard;
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

	public Set<Present> getPresents() {
		return presents;
	}

	public void setPresents(Set<Present> presents) {
		this.presents = presents;
	}
	
	public void addPresent(Present p) {
		presents.add(p);
	}
	
	public void removePresent(Present p) {
		presents.remove(p);
	}
	
}
