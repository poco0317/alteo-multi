package com.etterna.multi.data.state;

public enum LobbyState {
	
	SELECTING(0),
	INGAME(1);

	private int num;
	
	private LobbyState(int n) {
		num = n;
	}
	
	public int num() {
		return num;
	}

}
