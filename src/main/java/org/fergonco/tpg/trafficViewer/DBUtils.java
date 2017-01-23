package org.fergonco.tpg.trafficViewer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBUtils {

	private static String persistenceUnit = "local-pg";

	public static EntityManager getEntityManager() {
		// EntityManagerFactory emf =
		// Persistence.createEntityManagerFactory(JPA_CONF_NAME, Collections
		// .singletonMap(PersistenceUnitProperties.SESSION_CUSTOMIZER,
		// SchemaSessionCustomizer.class.getName()));
		// emf.getCache().evictAll();
		// EntityManager em = emf.createEntityManager();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
		EntityManager em = emf.createEntityManager();
		return em;
	}

	public static void setPersistenceUnit(String persistenceUnit) {
		DBUtils.persistenceUnit = persistenceUnit;
	}

}
