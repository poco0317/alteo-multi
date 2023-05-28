package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PacklistResponseMessage {
	
	private List<String> commonpacks;
	
	public PacklistResponseMessage() {}
	public PacklistResponseMessage(Lobby lobby) {
		commonpacks = new ArrayList<>(lobby.getCommonpacks());
	}

}
