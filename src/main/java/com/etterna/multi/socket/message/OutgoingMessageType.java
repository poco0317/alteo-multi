package com.etterna.multi.socket.message;

/**
 * Messages sent by the server to the client
 */
public enum OutgoingMessageType {
	
	HELLO,
	ROOMLIST,
	LOBBYUSERLIST,
	LOBBYUSERLISTUPDATE,
	PING,
	CHAT,
	LOGIN,
	SCORE,
	LEADERBOARD,
	CREATEROON,
	ENTERROON,
	SELECTCHART,
	DELETEROON,
	NEWROOM,
	UPDATEROOM,
	USERLIST,
	CHARTREQUEST,
	PACKLIST;
	
	private OutgoingMessageType() {
		linkedClass = null;
	}
	
	private OutgoingMessageType(Class<?> cla) {
		linkedClass = cla;
	}
	
	private Class<?> linkedClass;
	
	public Class<?> getLinkedClass() {
		return linkedClass;
	}

}
