package org.fergonco.traffic.dataGatherer.owm;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class WeatherForecast {

	private TreeSet<WeatherConditions> forecasts;

	public WeatherForecast() {
		forecasts = new TreeSet<>(new Comparator<WeatherConditions>() {

			@Override
			public int compare(WeatherConditions o1, WeatherConditions o2) {
				return (int) (o1.getTimestamp() - o2.getTimestamp());
			}
		});
	}

	public void addPrediction(WeatherConditions weatherConditions) {
		forecasts.add(weatherConditions);
	}

	public WeatherConditions getForecast(long forecastTimestamp) {
		Iterator<WeatherConditions> forecastIterator = forecasts.iterator();

		WeatherConditions lastForecast = forecastIterator.next();
		while (forecastIterator.hasNext()) {
			WeatherConditions currentForecast = (WeatherConditions) forecastIterator.next();
			if (currentForecast.getTimestamp() >= forecastTimestamp) {
				return lastForecast;
			}

			lastForecast = currentForecast;
		}

		throw new IllegalStateException("All forecasts were calculated for a period prior to the requested timestamp");
	}

}
