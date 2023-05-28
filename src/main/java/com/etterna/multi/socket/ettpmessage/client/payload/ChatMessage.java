package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessage {
	
	private String msg;
	private int msgtype;
	private String tab;
	
}
