package org.fergonco.tpg.trafficViewer;

import java.io.IOException;

import org.fergonco.tpg.trafficViewer.jpa.WeatherForecast;
import org.junit.Test;

public class OpenWeatherMapClientTest {

	@Test
	public void test() throws IOException {
		OpenWeatherMapClient client = new OpenWeatherMapClient();
		WeatherForecast[] forecasts = client.getForecasts(5);
		for (WeatherForecast weatherForecast : forecasts) {
			System.out.println(weatherForecast);
		}
	}
}
