package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fergonco.tpg.trafficViewer.OpenWeatherMapClient;

@WebListener
public class WeatherLogger implements ServletContextListener {

	private OpenWeatherMapClient client = new OpenWeatherMapClient();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
