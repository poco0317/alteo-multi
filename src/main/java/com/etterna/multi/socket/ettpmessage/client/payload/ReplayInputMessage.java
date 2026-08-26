package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReplayInputMessage {
	
	private Boolean is_press;
	private Integer col;
	private Integer row;
	private Float music_seconds;
	private Float offset;
	private Integer tapnote_type;
	private Integer tapnote_subtype;

}
