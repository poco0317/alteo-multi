package com.etterna.multi.socket.ettpmessage.server.payload;

import com.etterna.multi.data.state.Lobby;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NewRoomResponseMessage {
	
	private RoomDTO room;
	
	public NewRoomResponseMessage() {}
	public NewRoomResponseMessage(Lobby lobby) {
		room = new RoomDTO(lobby);
	}

}
