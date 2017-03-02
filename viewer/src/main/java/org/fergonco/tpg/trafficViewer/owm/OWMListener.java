package org.fergonco.tpg.trafficViewer.owm;

import java.io.IOException;

import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public interface OWMListener {

	void newConditions(WeatherConditions currentConditions);

	void error(IOException e);

}
