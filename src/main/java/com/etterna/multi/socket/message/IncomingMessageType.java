package com.etterna.multi.socket.message;

import com.etterna.multi.socket.message.handler.incoming.*;

/**
 * Messages sent by the client to the server
 */
public enum IncomingMessageType {
	
	LOGIN(LoginMessageHandler.class),
	PING,
	CHAT,
	SCORE,
	GAMEPLAYUPDATE,
	CREATEROOM,
	ENTERROOM,
	SELECTCHART,
	STARTCHART,
	LEAVEROOM,
	GAMEOVER,
	HASCHART,
	MISSINGCHART,
	STARTINGCHART,
	NOTSTARTINGCHART,
	OPENOPTIONS,
	CLOSEOPTIONS,
	OPENEVAL,
	CLOSEEVAL,
	LOGOUT,
	HELLO(HelloMessageHandler.class);
	
	private IncomingMessageType() {
		linkedClass = null;
	}
	
	private IncomingMessageType(Class<? extends IncomingMessageHandler> cla) {
		linkedClass = cla;
	}
	
	private Class<? extends IncomingMessageHandler> linkedClass;
	
	public Class<? extends IncomingMessageHandler> getLinkedClass() {
		return linkedClass;
	}

}
