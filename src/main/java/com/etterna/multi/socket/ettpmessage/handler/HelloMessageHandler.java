package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.HelloMessage;

@Component
public class HelloMessageHandler extends EttpMessageHandler {
	
	private static final String SERVER_NAME = "AltEOMulti";
	private static final int SERVER_VERSION = 5;
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		HelloMessage msg = readPayload(message, HelloMessage.class);
		
		sessions.clientHello(session, msg);
	}
	
	/**
	 * Sent on connect by a client
	 */
	public void hello(WebSocketSession session) {
		respond(session, "hello", new HelloResponseMessage());
	}
	
	public class HelloResponseMessage {
		private int version = SERVER_VERSION;
		private String name = SERVER_NAME;
		public int getVersion() {
			return version;
		}
		public String getName() {
			return name;
		}
	}

}
