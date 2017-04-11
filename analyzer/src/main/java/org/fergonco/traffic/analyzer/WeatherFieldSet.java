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
			weather = factorWeather(weatherConditions.getWeather());
		}
		return new Object[] { humidity, pressure, rain3h, snow3h, temperature, weather };
	}

	private String factorWeather(Integer weather) {
		if (weather == null) {
			return "";
		} else {
			switch (weather) {
			case 200:
			case 201:
			case 202:
			case 210:
			case 211:
			case 212:
			case 221:
			case 230:
			case 231:
			case 232:
				return "thunderstorm";
			case 300:
			case 301:
			case 302:
			case 310:
			case 311:
			case 312:
			case 313:
			case 314:
			case 321:
				return "drizzle";
			case 600:
			case 601:
			case 602:
			case 611:
			case 612:
			case 615:
			case 616:
			case 620:
			case 621:
			case 622:
				return "snow";
			case 500:
			case 501:
			case 502:
			case 503:
			case 504:
			case 511:
			case 520:
			case 521:
			case 522:
			case 531:
				return "rain";
			case 701:
				return "mist";
			case 711:
				return "smoke";
			case 721:
				return "haze";
			case 731:
				return "dust-whirls";
			case 741:
				return "fog";
			case 751:
				return "sand";
			case 761:
				return "dust";
			case 762:
				return "volcanic ash";
			case 771:
				return "squalls";
			case 781:
				return "tornado";
			case 800:
			case 801:
			case 802:
			case 803:
			case 804:
				return "clearorclouds";
			case 900:
			case 901:
			case 902:
			case 903:
			case 904:
			case 905:
			case 906:
				return "extreme";
			case 951:
			case 952:
			case 953:
			case 954:
			case 955:
				return "light wind";
			case 956:
			case 957:
			case 958:
			case 959:
			case 960:
			case 961:
			case 962:
				return "wind";
			default:
				throw new RuntimeException("" + weather);
			}
		}
	}
}
