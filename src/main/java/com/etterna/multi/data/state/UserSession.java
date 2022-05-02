package com.etterna.multi.data.state;

import java.util.Set;

import org.springframework.web.socket.WebSocketSession;

public class UserSession {
	
	private String username;
	private WebSocketSession session;
	private long lastPing = System.currentTimeMillis();
	private int ettpcVersion;
	private String client;
	private Set<String> packs;
	private PlayerState state = PlayerState.READY;
	private boolean isReady = false;
	private Lobby lobby;
	
	private double gameplayWife;
	private String gameplayJudgments;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public WebSocketSession getSession() {
		return session;
	}
	public void setSession(WebSocketSession session) {
		this.session = session;
	}
	public int getEttpcVersion() {
		return ettpcVersion;
	}
	public void setEttpcVersion(int ettpcVersion) {
		this.ettpcVersion = ettpcVersion;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public Set<String> getPacks() {
		return packs;
	}
	public void setPacks(Set<String> packs) {
		this.packs = packs;
	}
	public boolean isReady() {
		return isReady;
	}
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	public Lobby getLobby() {
		return lobby;
	}
	public void setLobby(Lobby lobby) {
		this.lobby = lobby;
	}
	public double getGameplayWife() {
		return gameplayWife;
	}
	public void setGameplayWife(double gameplayWife) {
		this.gameplayWife = gameplayWife;
	}
	public String getGameplayJudgments() {
		return gameplayJudgments;
	}
	public void setGameplayJudgments(String gameplayJudgments) {
		this.gameplayJudgments = gameplayJudgments;
	}
	public long getLastPing() {
		return lastPing;
	}
	public void setLastPing(long lastPing) {
		this.lastPing = lastPing;
	}
	public PlayerState getState() {
		return state;
	}
	public void setState(PlayerState state) {
		this.state = state;
	}

}
