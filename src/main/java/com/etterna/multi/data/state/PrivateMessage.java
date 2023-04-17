package com.etterna.multi.data.state;

public class PrivateMessage {
	
	private UserSession user1;
	private UserSession user2;
	
	public UserSession getUser1() {
		return user1;
	}
	public void setUser1(UserSession user1) {
		this.user1 = user1;
	}
	public UserSession getUser2() {
		return user2;
	}
	public void setUser2(UserSession user2) {
		this.user2 = user2;
	}

}
