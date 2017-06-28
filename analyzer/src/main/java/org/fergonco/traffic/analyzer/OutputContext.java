package org.fergonco.traffic.analyzer;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class OutputContext {

	private ShiftEntry shift;
	private WeatherConditions weatherConditions;

	public OutputContext(ShiftEntry shift, WeatherConditions weatherConditions) {
		this.shift = shift;
		this.weatherConditions = weatherConditions;
	}

	public ShiftEntry getShift() {
		return shift;
	}

	public WeatherConditions getWeatherConditions() {
		return weatherConditions;
	}

	public interface ShiftEntry {
		long getTimestamp();

		String getId();

		int getSpeed();
	}
}
