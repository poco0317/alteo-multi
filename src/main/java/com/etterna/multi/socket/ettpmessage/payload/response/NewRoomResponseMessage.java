package com.etterna.multi.socket.ettpmessage.payload.response;

import com.etterna.multi.data.state.Lobby;

public class NewRoomResponseMessage {
	
	private RoomDTO room;
	
	public NewRoomResponseMessage() {}
	public NewRoomResponseMessage(Lobby lobby) {
		room = new RoomDTO(lobby);
	}
	public RoomDTO getRoom() {
		return room;
	}
	public void setRoom(RoomDTO room) {
		this.room = room;
	}

}
