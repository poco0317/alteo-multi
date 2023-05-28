package com.etterna.multi.socket.ettpmessage.client.payload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HelloMessage {
	
	private String version;
	private String client;
	private List<String> packs;

}
