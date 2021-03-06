package org.fergonco.traffic.dataGatherer;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
import org.fergonco.traffic.dataGatherer.owm.OWM;
import org.fergonco.traffic.dataGatherer.owm.OWMListener;
import org.fergonco.traffic.dataGatherer.owm.OWMTimer;

@WebListener
public class OWMLogger implements ServletContextListener {

	private final static Logger logger = LogManager.getLogger(OWMLogger.class);

	private OWMTimer timer;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		OWM owm = new OWM();
		int fourHours = 4 * 60 * 60 * 1000;
		timer = new OWMTimer(owm, fourHours, new OWMListener() {

			@Override
			public void newConditions(WeatherConditions currentConditions) {
				EntityManager em = DBUtils.getEntityManager();
				em.getTransaction().begin();
				em.persist(currentConditions);
				em.getTransaction().commit();
			}

			@Override
			public void error(IOException e) {
				logger.debug("Cannot get weather conditions", e);
			}
		});
		timer.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		timer.stop();
	}

}
