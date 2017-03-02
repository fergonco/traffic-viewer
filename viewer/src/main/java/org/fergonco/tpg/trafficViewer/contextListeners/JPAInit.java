package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fergonco.tpg.trafficViewer.DBUtils;

@WebListener
public class JPAInit implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		DBUtils.getEntityManager();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
