package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMessage {

	private String user;
	private String pass;

}
