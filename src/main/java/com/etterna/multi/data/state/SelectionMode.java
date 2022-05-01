package com.etterna.multi.data.state;

public enum SelectionMode {
	
	CHARTKEY("Require same chartkey"),
	DIFFHASH("Require same difficulty and file"),
	HASH("Require same file, not same difficulty");
	
	private SelectionMode(String s) {
		description = s;
	}
	
	private String description;
	
	public String getDescription() {
		return description;
	}

}
