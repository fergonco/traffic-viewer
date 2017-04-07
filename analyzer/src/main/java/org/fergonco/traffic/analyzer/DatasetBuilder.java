package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

		/*
		 * We iterate all the shifts. We keep the latest when there are
		 * duplicates unless any of the duplicates has weird measures.
		 */
		HashMap<String, ArrayList<Shift>> idShiftDuplicates = new HashMap<>();
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			String shiftId = IdFieldSet.getShiftId(shift);
			ArrayList<Shift> shiftDuplicates = idShiftDuplicates.get(shiftId);
			if (shiftDuplicates == null) {
				shiftDuplicates = new ArrayList<>();
				idShiftDuplicates.put(shiftId, shiftDuplicates);
			}
			shiftDuplicates.add(shift);
		}
		HashMap<String, Shift> idRightShift = new HashMap<>();
		Set<String> shiftIds = idShiftDuplicates.keySet();
		for (String shiftId : shiftIds) {
			ArrayList<Shift> duplicates = idShiftDuplicates.get(shiftId);
			Shift lastShift = null;
			for (Shift shift : duplicates) {
				if (lastShift == null || shift.getTimestamp() > lastShift.getTimestamp()) {
					lastShift = shift;
				}

				if (shift.getSpeed() < 0 || shift.getSpeed() > 80) {
					lastShift = null;
					break;
				}
			}

			if (lastShift != null) {
				idRightShift.put(shiftId, lastShift);
			}
		}

		OutputFieldSet[] outputFieldSets = new OutputFieldSet[] { new IdFieldSet(), new ShiftFieldSet(),
				new CalendarFieldSet(), new WeatherFieldSet() };
		ArrayList<Object> outputLine = new ArrayList<>();
		for (OutputFieldSet outputFieldSet : outputFieldSets) {
			Collections.addAll(outputLine, outputFieldSet.getNames());
		}
		System.out.println(StringUtils.join(outputLine, ","));
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();

			// Check if this is the right duplicate
			String shiftId = IdFieldSet.getShiftId(shift);
			Shift rightShift = idRightShift.get(shiftId);
			if (rightShift == null || rightShift.getId() != shift.getId()) {
				// wrong duplicate
				continue;
			}

			outputLine.clear();

			WeatherConditions weatherConditions = null;
			try {
				Query weatherConditionsQuery = em.createNativeQuery("select * from app.WeatherConditions w "
						+ "where w.timestamp=("
						+ "select max(w2.timestamp) from app.WeatherConditions w2 where w2.timestamp < "
						+ shift.getTimestamp() + " and w2.timestamp + 6*60*60*1000 > " + shift.getTimestamp() + ");",
						WeatherConditions.class);
				weatherConditions = (WeatherConditions) weatherConditionsQuery.getSingleResult();

			} catch (NoResultException e) {
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
