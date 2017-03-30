package org.fergonco.traffic.analyzer;

import java.text.ParseException;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class WeatherFieldSet implements OutputFieldSet {

	@Override
	public String[] getNames() {
		return new String[] { "humidity", "pressure", "rain3h", "snow3h", "temperature", "weather" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) throws ParseException {
		WeatherConditions weatherConditions = outputContext.getWeatherConditions();
		String humidity = "";
		String pressure = "";
		String rain3h = "";
		String snow3h = "";
		String temperature = "";
		String weather = "";
		if (weatherConditions != null) {
			humidity = weatherConditions.getHumidity() + "";
			pressure = weatherConditions.getPressure() + "";
			rain3h = (weatherConditions.getRain3h() != null) ? weatherConditions.getRain3h() + "" : "";
			snow3h = (weatherConditions.getSnow3h() != null) ? weatherConditions.getSnow3h() + "" : "";
			temperature = weatherConditions.getTemperature() + "";
			weather = weatherConditions.getWeather() + "";
		}
		return new Object[] { humidity, pressure, rain3h, snow3h, temperature, weather };
	}
}
