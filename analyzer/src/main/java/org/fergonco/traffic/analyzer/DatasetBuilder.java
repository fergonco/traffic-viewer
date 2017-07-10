package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class DatasetBuilder {

	public void build(PrintStream stream, long startNode, long endNode) throws IOException, ParseException {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMShift> query = em.createQuery(
				"SELECT s FROM OSMShift s WHERE s.startNode=:startNode and s.endNode=:endNode", OSMShift.class);
		query.setParameter("startNode", startNode);
		query.setParameter("endNode", endNode);
		List<OSMShift> osmShifts = query.getResultList();

		/*
		 * We iterate all the shifts. We keep the latest when there are
		 * duplicates unless any of the duplicates has weird measures.
		 */
		HashMap<String, ArrayList<Shift>> idShiftDuplicates = new HashMap<>();
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			ShiftEntryImpl shiftEntry = new ShiftEntryImpl(shift);
			String shiftId = shiftEntry.getId();
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

		Dataset dataset = new Dataset(stream);
		dataset.writeHeader();
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			ShiftEntryImpl shiftEntry = new ShiftEntryImpl(shift);

			// Check if this is the right duplicate
			String shiftId = shiftEntry.getId();
			Shift rightShift = idRightShift.get(shiftId);
			if (rightShift == null || rightShift.getId() != shift.getId()) {
				// wrong duplicate
				continue;
			}

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

			OutputContext outputContext = new OutputContext(new ShiftEntryImpl(shift), weatherConditions);
			dataset.writeEntry(outputContext);
		}
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build(System.out, 907579280, 906227763);
	}
}
