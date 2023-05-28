package com.etterna.multi.socket.ettpmessage.server.payload;

import com.etterna.multi.data.state.Lobby;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteRoomResponseMessage {
	
	private RoomDTO room;
	
	public DeleteRoomResponseMessage() {}
	public DeleteRoomResponseMessage(Lobby lobby) {
		room = new RoomDTO(lobby);
	}

}
