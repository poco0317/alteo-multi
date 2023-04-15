package com.etterna.multi.socket.ettpmessage.server.payload;

import com.etterna.multi.data.state.Lobby;

public class DeleteRoomResponseMessage {
	
	private RoomDTO room;
	
	public DeleteRoomResponseMessage() {}
	public DeleteRoomResponseMessage(Lobby lobby) {
		room = new RoomDTO(lobby);
	}

	public RoomDTO getRoom() {
		return room;
	}

	public void setRoom(RoomDTO room) {
		this.room = room;
	}

}
