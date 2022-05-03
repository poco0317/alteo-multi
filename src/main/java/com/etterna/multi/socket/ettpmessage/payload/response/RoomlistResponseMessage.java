package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.List;

public class RoomlistResponseMessage {
	
	List<RoomDTO> rooms;
	
	public RoomlistResponseMessage() {}
	public RoomlistResponseMessage(List<RoomDTO> rms) {
		rooms = rms;
	}
	public List<RoomDTO> getRooms() {
		return rooms;
	}
	public void setRooms(List<RoomDTO> rooms) {
		this.rooms = rooms;
	}

}
