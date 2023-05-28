package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoomlistResponseMessage {
	
	List<RoomDTO> rooms;
	
	public RoomlistResponseMessage() {}
	public RoomlistResponseMessage(List<RoomDTO> rms) {
		rooms = rms;
	}

}
