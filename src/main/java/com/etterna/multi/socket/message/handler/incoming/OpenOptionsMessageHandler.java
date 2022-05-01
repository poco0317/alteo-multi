package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.IncomingMessageHandler;
import com.etterna.multi.socket.payload.incoming.OpenOptionsMessage;

@Component
public class OpenOptionsMessageHandler extends IncomingMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		OpenOptionsMessage msg = readPayload(message, OpenOptionsMessage.class);
	}

}
