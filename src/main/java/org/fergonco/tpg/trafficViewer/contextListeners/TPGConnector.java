package org.fergonco.tpg.trafficViewer.contextListeners;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import co.geomati.tpg.DayFrame;
import co.geomati.tpg.HumanReadableLog;
import co.geomati.tpg.HumanReadableLogImpl;
import co.geomati.tpg.Thermometer;
import co.geomati.tpg.ThermometerArchiver;
import co.geomati.tpg.ThermometerComparator;
import co.geomati.tpg.ThermometerMonitor;
import co.geomati.tpg.WeatherArchiver;
import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.TPGCachedParser;

@WebListener
public class TPGConnector implements ServletContextListener {

	private ThermometerMonitor monitor;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		int start4am = 4 * 60 * 60 * 1000;
		int end3amNextDay = 27 * 60 * 60 * 1000;
		DayFrame dayFrame = new DayFrame(start4am, end3amNextDay);

		TPGCachedParser tpg = new TPGCachedParser(new TPG());

		ThermometerComparator[] comparators = new ThermometerComparator[] {
				// new ThermometerComparator(dayFrame, tpg, new
				// NullThermometerArchiver(), "Y", "FEMA", "VAL-THOIRY"),
				new ThermometerComparator(dayFrame, tpg, new DBThermometerListener(), new NullThermometerArchiver(),
						"18", "BLAN", "CERN")

		};
		monitor = new ThermometerMonitor(dayFrame, comparators, new NullWeatherArchiver(),
				new StdoutHumanReadableLog());

		monitor.monitor();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		monitor.stop();
	}

	private static class NullThermometerArchiver implements ThermometerArchiver {

		@Override
		public void archive(String line, String firstStop, String destination, Thermometer thermometer)
				throws IOException {
			// TODO no op
		}

	}

	private static class NullWeatherArchiver implements WeatherArchiver {

		@Override
		public void archive() {
			// no op
		}

	}

	private static class StdoutHumanReadableLog implements HumanReadableLog {

		@Override
		public void log(List<Thermometer> thermometers) throws IOException {
			System.out.println(HumanReadableLogImpl.format(thermometers).toString());
		}

	}

}
