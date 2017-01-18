package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPG2OSM;

//@WebListener
public class TPG2OSMLineY implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();

		TPG2OSM t = new TPG2OSM();
		t.setKey(new TPG2OSM.Key("Y", "FEMA", "THGA"));
		t.setOsmids(new long[] {});
		em.persist(t);
		em.getTransaction().commit();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
