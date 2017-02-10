package org.fergonco.tpg.trafficViewer.owm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
import org.junit.Test;

public class OpenWeatherMapClientTest {

	@Test
	public void testParser() throws IOException {

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
	public void testOWM() throws IOException {
		OWM owm = new OWM(6, 46.25);
		WeatherConditions weather = owm.currentConditions();
		assertNotNull(weather);
	}

	@Test
	public void testTimer() throws IOException, InterruptedException {
		OWM owm = mock(OWM.class);
		OWMListener listener = mock(OWMListener.class);
		OWMTimer timer = new OWMTimer(owm, 500, listener);
		timer.start();
		verify(owm, never()).currentConditions();
		synchronized (this) {
			wait(700);
		}
		verify(owm, times(1)).currentConditions();
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
		verify(owm, never()).currentConditions();
	}
}
