package org.fergonco.traffic.dataGatherer.owm;

import java.io.IOException;
import java.util.regex.Pattern;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class OWM {
	private static APIClient client;
	private static OWMParser parser;

	static {
		client = new APIClient("/openweathermapkey", "http://api.openweathermap.org/data/2.5/");
		parser = new OWMParser();
	}

	private String[] getParameters(double lon, double lat) {
		String paramString = "lon=" + lon + ",lat=" + lat + ",units=metric";
		return paramString.split(Pattern.quote(","));
	}

	public WeatherConditions currentConditions(double lon, double lat) throws IOException {
		String owmResponse = client.get("weather", getParameters(lon, lat));
		return parser.parseWeather(owmResponse);
	}

	public WeatherForecast forecastedConditions(double lon, double lat) throws IOException {
		String owmResponse = client.get("forecast", getParameters(lon, lat));
		return parser.parseForecast(owmResponse);
	}

}
