package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.IncomingMessageHandler;
import com.etterna.multi.socket.payload.incoming.CloseEvalMessage;

@Component
public class CloseEvalMessageHandler extends IncomingMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		CloseEvalMessage msg = readPayload(message, CloseEvalMessage.class);
	}

}