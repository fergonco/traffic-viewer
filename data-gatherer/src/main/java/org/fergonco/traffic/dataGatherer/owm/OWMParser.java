package org.fergonco.traffic.dataGatherer.owm;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OWMParser {

	public WeatherConditions parseWeather(String weatherResponse) {
		JsonObject currentConditions = (JsonObject) new JsonParser().parse(weatherResponse);
		long timestamp = currentConditions.get("dt").getAsLong() * 1000;
		WeatherConditions ret = new WeatherConditions();
		ret.setTimestamp(timestamp);
		JsonObject main = (JsonObject) currentConditions.get("main");
		ret.setTemperature(main.get("temp").getAsDouble());
		ret.setPressure(main.get("pressure").getAsDouble());
		ret.setHumidity(main.get("humidity").getAsDouble());
		JsonArray weatherArray = (JsonArray) currentConditions.get("weather");
		if (weatherArray.size() > 0) {
			JsonObject weather = (JsonObject) weatherArray.get(0);
			ret.setWeather(weather.get("id").getAsInt());
		}
		JsonObject rain = (JsonObject) currentConditions.get("rain");
		if (rain != null && rain.has("3h")) {
			ret.setRain3h(rain.get("3h").getAsDouble());
		}
		JsonObject snow = (JsonObject) currentConditions.get("snow");
		if (snow != null && snow.has("3h")) {
			ret.setSnow3h(snow.get("3h").getAsDouble());
		}
		return ret;
	}

}
