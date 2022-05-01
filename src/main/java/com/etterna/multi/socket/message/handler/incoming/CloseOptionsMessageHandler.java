package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.EttpMessageHandler;
import com.etterna.multi.socket.payload.incoming.CloseOptionsMessage;

@Component
public class CloseOptionsMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		CloseOptionsMessage msg = readPayload(message, CloseOptionsMessage.class);
	}

}