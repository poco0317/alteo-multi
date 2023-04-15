package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;

public class PacklistResponseMessage {
	
	private List<String> commonpacks;
	
	public PacklistResponseMessage() {}
	public PacklistResponseMessage(Lobby lobby) {
		commonpacks = new ArrayList<>(lobby.getCommonpacks());
	}

	public List<String> getCommonpacks() {
		return commonpacks;
	}

	public void setCommonpacks(List<String> commonpacks) {
		this.commonpacks = commonpacks;
	}

}
