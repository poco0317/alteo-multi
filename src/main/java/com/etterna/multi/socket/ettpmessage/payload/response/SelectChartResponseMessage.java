package com.etterna.multi.socket.ettpmessage.payload.response;

import com.etterna.multi.data.state.Chart;

public class SelectChartResponseMessage {
	
	private ChartDTO chart;
	
	public SelectChartResponseMessage() {}
	public SelectChartResponseMessage(Chart chart) {
		
	}
	
	
	public ChartDTO getChart() {
		return chart;
	}
	public void setChart(ChartDTO chart) {
		this.chart = chart;
	}

}
