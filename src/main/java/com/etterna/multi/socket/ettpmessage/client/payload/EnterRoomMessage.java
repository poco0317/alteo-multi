package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EnterRoomMessage {

	private String name;
	private String pass;
	private String desc;
	
}
