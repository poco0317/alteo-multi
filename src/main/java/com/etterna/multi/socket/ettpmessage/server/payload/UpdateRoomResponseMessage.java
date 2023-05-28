package com.etterna.multi.socket.ettpmessage.server.payload;


import com.etterna.multi.data.state.Lobby;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateRoomResponseMessage {
	
	private RoomDTO room;
	
	public UpdateRoomResponseMessage() {}
	public UpdateRoomResponseMessage(Lobby lobby) {
		room = new RoomDTO(lobby);
	}

}
