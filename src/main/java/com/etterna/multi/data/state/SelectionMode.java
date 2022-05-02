package com.etterna.multi.data.state;

import com.etterna.multi.socket.ettpmessage.payload.response.ChartDTO;

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
	
	public ChartDTO f(Chart chart) {
		ChartDTO c = new ChartDTO();
		switch (this) {
		case CHARTKEY:
			c.setChartkey(chart.getChartkey());
			break;
		case DIFFHASH:
			c.setTitle(chart.getTitle());
			c.setSubtitle(chart.getSubtitle());
			c.setArtist(chart.getArtist());
			c.setDifficulty(chart.getDifficulty());
			c.setMeter(chart.getMeter());
			c.setFilehash(chart.getFilehash());
			break;
		case HASH:
			c.setTitle(chart.getTitle());
			c.setSubtitle(chart.getSubtitle());
			c.setArtist(chart.getArtist());
			c.setFilehash(chart.getFilehash());
			break;
		default:
			break;
		}
		return c;
	}

}
