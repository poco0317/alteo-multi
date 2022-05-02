package com.etterna.multi.socket.ettpmessage.payload.response;

public class EnterRoomResponseMessage {
	private boolean entered;
	public EnterRoomResponseMessage() {}
	public EnterRoomResponseMessage(boolean b) {
		entered = b;
	}
	public boolean isEntered() {
		return entered;
	}
	public void setEntered(boolean entered) {
		this.entered = entered;
	}
}