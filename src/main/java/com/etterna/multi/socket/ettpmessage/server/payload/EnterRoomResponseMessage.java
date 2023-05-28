package com.etterna.multi.socket.ettpmessage.server.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EnterRoomResponseMessage {
	private boolean entered;
	public EnterRoomResponseMessage() {}
	public EnterRoomResponseMessage(boolean b) {
		entered = b;
	}
}