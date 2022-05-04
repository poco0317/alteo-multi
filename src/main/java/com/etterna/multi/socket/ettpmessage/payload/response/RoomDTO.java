package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.List;
import java.util.stream.Collectors;

import com.etterna.multi.data.state.Lobby;

public class RoomDTO {
	
	private String name;
	private String desc;
	private List<String> players;
	private boolean pass;
	private int state;
	
	public RoomDTO() {}
	public RoomDTO(Lobby lobby) {
		name = lobby.getName();
		desc = lobby.getDescription();
		state = lobby.getState().num();
		pass = lobby.getPassword() != null && !lobby.getPassword().isEmpty();
		players = lobby.getPlayers().stream().map(p -> p.getUsername()).collect(Collectors.toList());
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<String> getPlayers() {
		return players;
	}
	public void setPlayers(List<String> players) {
		this.players = players;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public boolean isPass() {
		return pass;
	}
	public void setPass(boolean pass) {
		this.pass = pass;
	}

}
