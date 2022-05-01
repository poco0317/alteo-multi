package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.IncomingMessageHandler;
import com.etterna.multi.socket.payload.incoming.CreateRoomMessage;

@Component
public class CreateRoomMessageHandler extends IncomingMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		CreateRoomMessage msg = readPayload(message, CreateRoomMessage.class);
	}

}
