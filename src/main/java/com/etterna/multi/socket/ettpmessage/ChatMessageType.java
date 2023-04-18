package com.etterna.multi.socket.ettpmessage;

public enum ChatMessageType {

	LOBBY(0),
	ROOM(1),
	PRIVATE(2);
	
	private int num;
	
	public static ChatMessageType fromInt(int i) {
		switch (i) {
			case 0:
				return LOBBY;
			case 1:
				return ROOM;
			case 2:
				return PRIVATE;
			default:
				return LOBBY;
		}
	}
	
	private ChatMessageType(int num) {
		this.num = num;
	}
	
	public int num() {
		return num;
	}
}
