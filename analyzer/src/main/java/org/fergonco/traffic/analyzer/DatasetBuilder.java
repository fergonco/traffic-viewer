package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class DatasetBuilder {

	public void build() throws IOException, ParseException {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMShift> query = em.createQuery(
				"SELECT s FROM OSMShift s WHERE s.startNode=:startNode and s.endNode=:endNode", OSMShift.class);
		query.setParameter("startNode", 907579280);
		query.setParameter("endNode", 906227763);
		List<OSMShift> osmShifts = query.getResultList();
		OutputFieldSet[] outputFieldSets = new OutputFieldSet[] { new ShiftFieldSet(), new CalendarFieldSet(),
				new WeatherFieldSet() };
		ArrayList<Object> outputLine = new ArrayList<>();
		for (OutputFieldSet outputFieldSet : outputFieldSets) {
			Collections.addAll(outputLine, outputFieldSet.getNames());
		}
		System.out.println(StringUtils.join(outputLine, ","));
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			outputLine.clear();

			WeatherConditions weatherConditions = null;
			try {
				Query weatherConditionsQuery = em
						.createNativeQuery("select * from app.WeatherConditions w " + "where w.timestamp=("
								+ "select max(w2.timestamp) from app.WeatherConditions w2 where w2.timestamp < "
								+ shift.getTimestamp() + ");", WeatherConditions.class);
				weatherConditions = (WeatherConditions) weatherConditionsQuery.getSingleResult();

			} catch (NoResultException e) {
				e.printStackTrace();
			}

			OutputContext outputContext = new OutputContext(shift, weatherConditions);
			for (OutputFieldSet outputFieldSet : outputFieldSets) {
				Collections.addAll(outputLine, outputFieldSet.getValues(outputContext));
			}

			System.out.println(StringUtils.join(outputLine, ","));
		}
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build();
	}
}
