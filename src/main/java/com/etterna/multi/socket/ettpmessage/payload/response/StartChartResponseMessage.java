package com.etterna.multi.socket.ettpmessage.payload.response;

import com.etterna.multi.data.state.Chart;

public class StartChartResponseMessage {
	private ChartDTO chart;
	
	public StartChartResponseMessage() {}
	public StartChartResponseMessage(ChartDTO c) {
		chart = c;
	}
	public StartChartResponseMessage(Chart chart) {
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
