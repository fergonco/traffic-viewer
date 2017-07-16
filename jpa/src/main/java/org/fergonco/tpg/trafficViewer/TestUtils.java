package org.fergonco.tpg.trafficViewer;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class TestUtils {

	public static void configureDBUtilsAndClearDatabase() {
		DBUtils.setPersistenceUnit("test");
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		String weatherconditions = WeatherConditions.class.getSimpleName();
		String osmsegment = OSMSegment.class.getSimpleName();
		String shift = Shift.class.getSimpleName();
		em.createQuery("DELETE FROM " + weatherconditions).executeUpdate();
		em.createQuery("UPDATE " + osmsegment + " SET model = null").executeUpdate();
		em.createQuery("DELETE FROM " + shift).executeUpdate();
		em.getTransaction().commit();
	}
}
