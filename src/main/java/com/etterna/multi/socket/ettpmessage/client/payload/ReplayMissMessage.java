package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReplayMissMessage {
	
	private Integer col;
	private Integer row;
	private Integer tapnote_type;
	private Integer tapnote_subtype;

}
