package com.etterna.multi.socket.ettpmessage.payload;

import java.util.List;

public class HelloMessage {
	
	private String version;
	private String client;
	private List<String> packs;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public List<String> getPacks() {
		return packs;
	}
	public void setPacks(List<String> packs) {
		this.packs = packs;
	}

}
