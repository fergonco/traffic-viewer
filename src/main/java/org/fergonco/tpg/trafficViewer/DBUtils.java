package org.fergonco.tpg.trafficViewer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DBUtils {

	public static EntityManager getEntityManager() {
		// EntityManagerFactory emf =
		// Persistence.createEntityManagerFactory(JPA_CONF_NAME, Collections
		// .singletonMap(PersistenceUnitProperties.SESSION_CUSTOMIZER,
		// SchemaSessionCustomizer.class.getName()));
		// emf.getCache().evictAll();
		// EntityManager em = emf.createEntityManager();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("local-pg");
		EntityManager em = emf.createEntityManager();
		return em;
	}

}
