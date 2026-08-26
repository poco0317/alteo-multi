package com.etterna.multi.socket.ettpmessage.server.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GameplayReplayUpdateMessage {
	
	private String subType;
	private Object data;
	private String userid;
	private Long seq;

}
