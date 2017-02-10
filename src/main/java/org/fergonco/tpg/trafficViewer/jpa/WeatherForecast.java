package org.fergonco.tpg.trafficViewer.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@Entity
public class WeatherForecast {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long timestamp;
	private double temperature;
	private double pressure;
	private double humidity;
	private Integer weather;
	private Double rain3h;
	private Double snow3h;

	public void setId(long id) {
		this.id = id;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public void setWeather(int weather) {
		this.weather = weather;
	}

	public void setRain3h(Double rain3h) {
		this.rain3h = rain3h;
	}

	public void setSnow3h(Double snow3h) {
		this.snow3h = snow3h;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(
				new Object[] { new Date(timestamp), temperature, pressure, humidity, weather, rain3h, snow3h });
	}
}
