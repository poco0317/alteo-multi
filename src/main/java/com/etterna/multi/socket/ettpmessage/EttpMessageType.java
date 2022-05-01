package com.etterna.multi.socket.ettpmessage;

import com.etterna.multi.socket.ettpmessage.handler.*;

/**
 * Messages sent by the client to the server.
 * Helps map input type strings to message handlers
 */
public enum EttpMessageType {
	
	LOGIN(LoginMessageHandler.class),
	PING(PingMessageHandler.class),
	CHAT(ChatMessageHandler.class),
	SCORE(ScoreMessageHandler.class),
	GAMEPLAYUPDATE(GameplayUpdateMessageHandler.class),
	CREATEROOM(CreateRoomMessageHandler.class),
	ENTERROOM(EnterRoomMessageHandler.class),
	SELECTCHART(SelectChartMessageHandler.class),
	STARTCHART(StartChartMessageHandler.class),
	LEAVEROOM(LeaveRoomMessageHandler.class),
	GAMEOVER(GameoverMessageHandler.class),
	HASCHART(HasChartMessageHandler.class),
	MISSINGCHART(MissingChartMessageHandler.class),
	STARTINGCHART(StartingChartMessageHandler.class),
	NOTSTARTINGCHART(NotStartingChartMessageHandler.class),
	OPENOPTIONS(OpenOptionsMessageHandler.class),
	CLOSEOPTIONS(CloseOptionsMessageHandler.class),
	OPENEVAL(OpenEvalMessageHandler.class),
	CLOSEEVAL(CloseEvalMessageHandler.class),
	LOGOUT(LogoutMessageHandler.class),
	HELLO(HelloMessageHandler.class);
	
	private EttpMessageType() {
		linkedClass = null;
	}
	
	private EttpMessageType(Class<? extends EttpMessageHandler> cla) {
		linkedClass = cla;
	}
	
	private Class<? extends EttpMessageHandler> linkedClass;
	
	public Class<? extends EttpMessageHandler> getLinkedClass() {
		return linkedClass;
	}

}
