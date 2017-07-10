package org.fergonco.tpg.trafficViewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

public class DBUtils {

	private static final String TRAFFIC_VIEWER_DB_URL = "TRAFFIC_VIEWER_DB_URL";
	private static final String TRAFFIC_VIEWER_DB_USER = "TRAFFIC_VIEWER_DB_USER";
	private static final String TRAFFIC_VIEWER_DB_PASSWORD = "TRAFFIC_VIEWER_DB_PASSWORD";
	private static final String TRAFFIC_VIEWER_JPA_LOG_LEVEL = "TRAFFIC_VIEWER_JPA_LOG_LEVEL";
	private static String persistenceUnit = "local-pg";
	private static String schemaName = "app";

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
		if (System.getenv(TRAFFIC_VIEWER_JPA_LOG_LEVEL) != null) {
			configurationMap.put("eclipselink.logging.level", System.getenv(TRAFFIC_VIEWER_JPA_LOG_LEVEL));
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

	public static <T> void paginatedProcessing(TypedQuery<T> query, int fetchSize, PageProcessor<T> pageProcessor)
			throws AbortPaginationException {
		query.setMaxResults(fetchSize);
		int offset = 0;
		List<T> resultList;
		while ((resultList = query.setFirstResult(offset).getResultList()).size() > 0) {
			pageProcessor.processPage(resultList);
			offset += resultList.size();
		}

	}

	public static interface PageProcessor<T> {
		void processPage(List<T> pageContents) throws AbortPaginationException;
	}

	public static class AbortPaginationException extends Exception {
		private static final long serialVersionUID = 1L;

		public AbortPaginationException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}
}
