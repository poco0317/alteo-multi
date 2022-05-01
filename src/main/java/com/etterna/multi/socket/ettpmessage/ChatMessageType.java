package com.etterna.multi.socket.ettpmessage;

public enum ChatMessageType {

	LOBBY(0),
	ROOM(1),
	PRIVATE(2);
	
	private int num;
	
	private ChatMessageType(int num) {
		this.num = num;
	}
	
	public int num() {
		return num;
	}
}
