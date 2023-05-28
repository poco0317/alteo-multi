package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LobbyUserlistUpdateResponseMessage {
	
	private List<String> on = new ArrayList<>();
	private List<String> off = new ArrayList<>();
	
	public void addOn(String u) {
		on.add(u);
	}
	public void addOff(String u) {
		off.add(u);
	}


}
