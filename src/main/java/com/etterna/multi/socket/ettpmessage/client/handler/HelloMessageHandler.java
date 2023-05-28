package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.HelloMessage;

import lombok.Getter;
import lombok.Setter;

@Component
public class HelloMessageHandler extends EttpMessageHandler {
	
	private static final String SERVER_NAME = "AltEOMulti";
	private static final int SERVER_VERSION = 5;
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		HelloMessage msg = readPayload(message, HelloMessage.class);
		
		multiplayer.clientHello(session, msg);
	}
	
	/**
	 * Sent on connect by a client
	 */
	public void hello(WebSocketSession session) {
		responder.respond(session, "hello", new HelloResponseMessage());
	}
	
	@Getter @Setter
	public class HelloResponseMessage {
		private int version = SERVER_VERSION;
		private String name = SERVER_NAME;
	}

}
