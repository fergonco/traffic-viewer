package org.fergonco.tpg.trafficViewer.contextListeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.fergonco.tpg.trafficViewer.DBUtils;

public class SetSchema implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		DBUtils.setSchemaName("app");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}
