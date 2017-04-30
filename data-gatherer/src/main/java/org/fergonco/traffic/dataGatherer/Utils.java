package org.fergonco.traffic.dataGatherer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.fergonco.tpg.trafficViewer.jpa.TPGStop2;

public class Utils {

	public static TPGStop2 getTPGStop(EntityManager em, String tpgCode, String line, String destination) {
		TypedQuery<TPGStop2> query = em.createQuery(
				"SELECT s FROM TPGStop2 s WHERE s.tpgCode=:tpgCode AND s.line=:line AND s.destination=:destination",
				TPGStop2.class);
		query.setParameter("tpgCode", tpgCode);
		query.setParameter("line", line);
		query.setParameter("destination", destination);
		return query.getSingleResult();
	}
}
