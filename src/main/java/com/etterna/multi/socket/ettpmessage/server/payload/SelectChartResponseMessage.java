package com.etterna.multi.socket.ettpmessage.server.payload;

import com.etterna.multi.data.state.Chart;

public class SelectChartResponseMessage {
	
	private ChartDTO chart;
	
	public SelectChartResponseMessage() {}
	public SelectChartResponseMessage(ChartDTO c) {
		chart = c;
	}
	/**
	 * Not used in preference of Lobby.serializeChart(Chart)
	 */
	public SelectChartResponseMessage(Chart chart) {
		this.chart = new ChartDTO();
		this.chart.setArtist(chart.getArtist());
		this.chart.setChartkey(chart.getChartkey());
		this.chart.setDifficulty(chart.getDifficulty());
		this.chart.setFilehash(chart.getFilehash());
		this.chart.setMeter(chart.getMeter());
		this.chart.setRate(chart.getRate());
		this.chart.setSubtitle(chart.getSubtitle());
		this.chart.setTitle(chart.getTitle());
	}
	
	
	public ChartDTO getChart() {
		return chart;
	}
	public void setChart(ChartDTO chart) {
		this.chart = chart;
	}

}
