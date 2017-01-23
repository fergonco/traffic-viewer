package org.fergonco.tpg.trafficViewer;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

public class DBUtils {

	private static String persistenceUnit = "local-pg";
	private static String schemaName = null;

	public static EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, Collections
				.singletonMap(PersistenceUnitProperties.SESSION_CUSTOMIZER, SchemaSessionCustomizer.class.getName()));
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
