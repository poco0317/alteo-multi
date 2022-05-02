package com.etterna.multi.data.state;

public enum PlayerState {
	
	READY(0),
	PLAYING(1),
	EVAL(2),
	OPTIONS(3),
	NOTREADY(4);
	
	private int num;
	
	private PlayerState(int n) {
		num = n;
	}
	
	public int num() {
		return num;
	}

}
