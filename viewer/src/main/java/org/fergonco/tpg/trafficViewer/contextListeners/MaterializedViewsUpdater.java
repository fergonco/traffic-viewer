package org.fergonco.tpg.trafficViewer.contextListeners;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.traffic.analyzer.predict.Predictor;

@WebListener
public class MaterializedViewsUpdater implements ServletContextListener {
	private static final Logger logger = LogManager.getLogger(MaterializedViewsUpdater.class.getName());

	private Predictor predictor = new Predictor();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Timer timer = new Timer(true);
		int _15minutes = 15 * 60 * 1000;
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				try {
					predictor.updatePredictions();
				} catch (IOException e) {
					logger.error("Error while updating predictions", e);
				}

				refresh("app.osmshiftinfo");
				refresh("app.timestamps");
				refresh("app.timestamped_measured_osmshifts");
				refresh("app.timestamped_osmshiftinfo");

			}
		}, _15minutes, _15minutes);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

	private void refresh(String viewName) {
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		Query q = em.createNativeQuery("refresh materialized view " + viewName + ";");
		q.executeUpdate();
		em.getTransaction().commit();
	}

}
