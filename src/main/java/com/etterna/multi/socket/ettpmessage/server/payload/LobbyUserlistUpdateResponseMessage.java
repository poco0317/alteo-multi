package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

public class LobbyUserlistUpdateResponseMessage {
	
	private List<String> on = new ArrayList<>();
	private List<String> off = new ArrayList<>();
	
	public void addOn(String u) {
		on.add(u);
	}
	public void addOff(String u) {
		off.add(u);
	}
	
	public List<String> getOn() {
		return on;
	}
	public void setOn(List<String> on) {
		this.on = on;
	}
	public List<String> getOff() {
		return off;
	}
	public void setOff(List<String> off) {
		this.off = off;
	}
	


}
