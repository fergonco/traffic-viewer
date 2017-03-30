package org.fergonco.traffic.analyzer;

import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class OutputContext {

	private Shift shift;
	private WeatherConditions weatherConditions;

	public OutputContext(Shift shift, WeatherConditions weatherConditions) {
		this.shift = shift;
		this.weatherConditions = weatherConditions;
	}

	public Shift getShift() {
		return shift;
	}

	public WeatherConditions getWeatherConditions() {
		return weatherConditions;
	}

}
