package org.fergonco.tpg.trafficViewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.jpa.WeatherForecast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.geomati.tpg.utils.TPG;

public class OpenWeatherMapClient {
	private final static Logger logger = LogManager.getLogger(TPG.class);
	private static final int HOUR = 60 * 60 * 1000;

	private static String key;
	private static final String baseURL = "http://api.openweathermap.org/data/2.5/";

	static {
		InputStream stream = TPG.class.getResourceAsStream("/openweathermapkey");
		try {
			key = IOUtils.toString(stream, Charset.forName("utf8"));
		} catch (IOException e) {
			throw new RuntimeException("Key not found", e);
		}
	}

	private String get(String command, String... params) throws IOException {
		try {
			String url = baseURL + command + "?appid=" + key;
			for (int i = 0; i < params.length; i++) {
				url += "&" + params[i];
			}
			logger.debug(url);
			String ret = IOUtils.toString(new URI(url), Charset.forName("utf-8"));
			logger.debug("ok");
			logger.debug(ret);
			return ret;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Bug: Malformed URI", e);
		}
	}

	public WeatherForecast[] getForecasts(int nextHours) throws IOException {
		ArrayList<WeatherForecast> ret = new ArrayList<>();
		String forecast = get("forecast", "lon=6,lat=46.25,units=metric".split(Pattern.quote(",")));
		JsonObject jsonForecast = (JsonObject) new JsonParser().parse(forecast);
		JsonArray list = jsonForecast.get("list").getAsJsonArray();
		for (int i = 0; i < list.size(); i++) {
			JsonObject hourForecast = (JsonObject) list.get(i);
			long timestamp = hourForecast.get("dt").getAsLong() * 1000;
			long now = new Date().getTime();
			if (now < timestamp && timestamp < now + nextHours * HOUR) {
				WeatherForecast weatherForecast = new WeatherForecast();
				weatherForecast.setTimestamp(timestamp);
				JsonObject main = (JsonObject) hourForecast.get("main");
				weatherForecast.setTemperature(main.get("temp").getAsDouble());
				weatherForecast.setPressure(main.get("pressure").getAsDouble());
				weatherForecast.setHumidity(main.get("humidity").getAsDouble());
				JsonArray weatherArray = (JsonArray) hourForecast.get("weather");
				if (weatherArray.size() > 0) {
					JsonObject weather = (JsonObject) weatherArray.get(0);
					weatherForecast.setWeather(weather.get("id").getAsInt());
				}
				JsonObject rain = (JsonObject) hourForecast.get("rain");
				if (rain.has("3h")) {
					weatherForecast.setRain3h(rain.get("3h").getAsDouble());
				}
				JsonObject snow = (JsonObject) hourForecast.get("snow");
				if (snow.has("3h")) {
					weatherForecast.setSnow3h(snow.get("3h").getAsDouble());
				}
				ret.add(weatherForecast);
			}
		}

		return ret.toArray(new WeatherForecast[ret.size()]);
	}

}
