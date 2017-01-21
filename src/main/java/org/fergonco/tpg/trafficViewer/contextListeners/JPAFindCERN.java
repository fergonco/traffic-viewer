package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;

@WebListener
public class JPAFindCERN implements ServletContextListener {

	@Override
	public void contextInitialized(final ServletContextEvent sce) {

		EntityManager em = DBUtils.getEntityManager();
		TPGStop cernStop = em.find(TPGStop.class, "CERN");
		System.out.println(cernStop);

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
