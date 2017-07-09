package org.fergonco.tpg.trafficViewer.owm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
import org.fergonco.traffic.dataGatherer.owm.OWM;
import org.fergonco.traffic.dataGatherer.owm.OWMListener;
import org.fergonco.traffic.dataGatherer.owm.OWMParser;
import org.fergonco.traffic.dataGatherer.owm.OWMTimer;
import org.fergonco.traffic.dataGatherer.owm.WeatherForecast;
import org.junit.Test;

public class OpenWeatherMapClientTest {

	@Test
	public void testParseCurrentConditions() throws IOException {

		InputStream input = this.getClass().getResourceAsStream("owmresponse.json");
		String owmResponse = IOUtils.toString(input, Charset.forName("utf8"));
		input.close();
		OWMParser parser = new OWMParser();

		WeatherConditions weather = parser.parseWeather(owmResponse);

		assertEquals(1486762200000L, weather.getTimestamp());
		assertEquals(93.0, weather.getHumidity());
		assertEquals(1018.0, weather.getPressure());
		assertNull(weather.getRain3h());
		assertNull(weather.getSnow3h());
		assertEquals(3.0, weather.getTemperature());
		assertEquals(701, (int) weather.getWeather());
	}

	@Test
	public void testParseForecast() throws IOException {

		InputStream input = this.getClass().getResourceAsStream("owmForecastResponse.json");
		String owmResponse = IOUtils.toString(input, Charset.forName("utf8"));
		input.close();
		OWMParser parser = new OWMParser();

		WeatherForecast forecast = parser.parseForecast(owmResponse);
		// Before first forecast
		WeatherConditions weather = forecast.getForecast(1498672700000L);
		checkFirstForecast(weather);
		// Between first and second forecast
		weather = forecast.getForecast(1498672900000L);
		checkFirstForecast(weather);
		// Between second and third forecast
		weather = forecast.getForecast(1498683700000L);
		assertEquals(100.0, weather.getHumidity());
		assertEquals(932.39, weather.getPressure());
		assertEquals(0.295, weather.getRain3h());
		assertNull(weather.getSnow3h());
		assertEquals(14.27, weather.getTemperature());
		assertEquals(500, (int) weather.getWeather());
	}

	private void checkFirstForecast(WeatherConditions weather) {
		assertEquals(100.0, weather.getHumidity());
		assertEquals(932.11, weather.getPressure());
		assertEquals(7.26, weather.getRain3h());
		assertNull(weather.getSnow3h());
		assertEquals(15.14, weather.getTemperature());
		assertEquals(501, (int) weather.getWeather());
	}

	@Test
	public void testOWM() throws IOException {
		OWM owm = new OWM();
		WeatherConditions weather = owm.currentConditions(6, 46.25);
		assertNotNull(weather);
	}

	@Test
	public void testTimer() throws IOException, InterruptedException {
		OWM owm = mock(OWM.class);
		OWMListener listener = mock(OWMListener.class);
		OWMTimer timer = new OWMTimer(owm, 500, listener);
		timer.start();
		verify(owm, never()).currentConditions(anyDouble(), anyDouble());
		synchronized (this) {
			wait(700);
		}
		verify(owm, times(1)).currentConditions(anyDouble(), anyDouble());
	}

	@Test
	public void testTimerStop() throws IOException, InterruptedException {
		OWM owm = mock(OWM.class);
		OWMListener listener = mock(OWMListener.class);
		OWMTimer timer = new OWMTimer(owm, 500, listener);
		timer.start();
		timer.stop();
		synchronized (this) {
			wait(700);
		}
		verify(owm, never()).currentConditions(anyDouble(), anyDouble());
	}
}
