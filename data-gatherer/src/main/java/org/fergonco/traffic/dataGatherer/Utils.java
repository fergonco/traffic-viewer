package org.fergonco.traffic.dataGatherer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.fergonco.tpg.trafficViewer.jpa.TPGStop;

public class Utils {

	public static TPGStop getTPGStop(EntityManager em, String tpgCode, String line, String destination) {
		TypedQuery<TPGStop> query = em.createQuery("SELECT s FROM " + TPGStop.class.getSimpleName()
				+ " s WHERE s.tpgCode=:tpgCode AND s.line=:line AND s.destination=:destination", TPGStop.class);
		query.setParameter("tpgCode", tpgCode);
		query.setParameter("line", line);
		query.setParameter("destination", destination);
		return query.getSingleResult();
	}
}
