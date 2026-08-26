package com.etterna.multi.socket.ettpmessage.server.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SpectatingUpdateResponseMessage {
	
	private String who;
	private String spectatingWho;
	private Boolean state;

}
