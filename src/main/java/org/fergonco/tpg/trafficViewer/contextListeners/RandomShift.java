package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class RandomShift implements ServletContextListener {

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		// Timer timer = new Timer(false);
		// timer.schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		// Shift shift = new Shift();
		// shift.setSpeed((int) (Math.random() * 100));
		// shift.setStartLon(672843);
		// shift.setStartLat(5818381);
		// shift.setEndLon(671714);
		// shift.setEndLat(5818810);
		// shift.setTimestamp(new Date().getTime());
		// shift.setVehicleId("Y1");
		//
		// OSMShift osmShift = new OSMShift();
		// osmShift.setOsmId(4848097);
		// osmShift.setShift(shift);
		//
		// EntityManager em = DBUtils.getEntityManager();
		// em.getTransaction().begin();
		// em.persist(shift);
		// em.persist(osmShift);
		// em.getTransaction().commit();
		//
		// }
		// }, 500, 5000);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
