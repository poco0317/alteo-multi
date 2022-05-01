package com.etterna.multi.data.state;

import java.util.HashSet;
import java.util.Set;

public class Lobby {

	private String name;
	private String description;
	private String hashedPassword;
	
	private boolean freerate;
	private boolean freepick;
	private boolean playing;
	private Chart chart;
	private Set<String> commonpacks = new HashSet<>();
	private SelectionMode selectionmode;
	private boolean countdown;
	private boolean inCountdown;
	private int timer;
	
	private UserSession owner;
	private Set<UserSession> operators = new HashSet<>();
	private Set<UserSession> players = new HashSet<>();
	
	public boolean canSelect(UserSession user) {
		return freepick || isOwner(user) || isOperator(user); 
	}
	
	public boolean isOperator(UserSession user) {
		return operators.contains(user);
	}
	
	public boolean isOwner(UserSession user) {
		return owner.equals(user);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	public boolean isFreerate() {
		return freerate;
	}
	public void setFreerate(boolean freerate) {
		this.freerate = freerate;
	}
	public boolean isFreepick() {
		return freepick;
	}
	public void setFreepick(boolean freepick) {
		this.freepick = freepick;
	}
	public boolean isPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public SelectionMode getSelectionmode() {
		return selectionmode;
	}
	public void setSelectionmode(SelectionMode selectionmode) {
		this.selectionmode = selectionmode;
	}
	public boolean isCountdown() {
		return countdown;
	}
	public void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}
	public boolean isInCountdown() {
		return inCountdown;
	}
	public void setInCountdown(boolean inCountdown) {
		this.inCountdown = inCountdown;
	}
	public int getTimer() {
		return timer;
	}
	public void setTimer(int timer) {
		this.timer = timer;
	}
	public UserSession getOwner() {
		return owner;
	}
	public void setOwner(UserSession owner) {
		this.owner = owner;
	}

	public Set<String> getCommonpacks() {
		return commonpacks;
	}

	public void setCommonpacks(Set<String> commonpacks) {
		this.commonpacks = commonpacks;
	}

	public Set<UserSession> getOperators() {
		return operators;
	}

	public void setOperators(Set<UserSession> operators) {
		this.operators = operators;
	}

	public Set<UserSession> getPlayers() {
		return players;
	}

	public void setPlayers(Set<UserSession> players) {
		this.players = players;
	}
	
	
}
