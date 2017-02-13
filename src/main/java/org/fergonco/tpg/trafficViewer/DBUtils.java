package org.fergonco.tpg.trafficViewer;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

public class DBUtils {

	private static final String TRAFFIC_VIEWER_DB_URL = "TRAFFIC_VIEWER_DB_URL";
	private static final String TRAFFIC_VIEWER_DB_USER = "TRAFFIC_VIEWER_DB_USER";
	private static final String TRAFFIC_VIEWER_DB_PASSWORD = "TRAFFIC_VIEWER_DB_PASSWORD";
	private static String persistenceUnit = "local-pg";
	private static String schemaName = null;

	public static EntityManager getEntityManager() {
		Map<String, String> configurationMap = new HashMap<>();
		configurationMap.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, SchemaSessionCustomizer.class.getName());
		if (System.getenv(TRAFFIC_VIEWER_DB_URL) != null) {
			configurationMap.put("javax.persistence.jdbc.url", System.getenv(TRAFFIC_VIEWER_DB_URL));
		}
		if (System.getenv(TRAFFIC_VIEWER_DB_USER) != null) {
			configurationMap.put("javax.persistence.jdbc.user", System.getenv(TRAFFIC_VIEWER_DB_USER));
		}
		if (System.getenv(TRAFFIC_VIEWER_DB_PASSWORD) != null) {
			configurationMap.put("javax.persistence.jdbc.password", System.getenv(TRAFFIC_VIEWER_DB_PASSWORD));
		}
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, configurationMap);
		EntityManager em = emf.createEntityManager();
		return em;
	}

	public static void setPersistenceUnit(String persistenceUnit) {
		DBUtils.persistenceUnit = persistenceUnit;
	}

	public static void setSchemaName(String schemaName) {
		DBUtils.schemaName = schemaName;
	}

	public static class SchemaSessionCustomizer implements SessionCustomizer {

		@Override
		public void customize(Session session) throws Exception {
			if (schemaName != null) {
				session.getLogin().setTableQualifier(schemaName);
			}
		}

	}

}
