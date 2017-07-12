package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class DatasetBuilder {

	private void build(PrintStream stream, long startNode, long endNode) throws IOException, ParseException {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMSegment> query = em.createQuery("SELECT s FROM " + OSMSegment.class.getSimpleName()
				+ " s WHERE s.startNode=:startNode and s.endNode=:endNode", OSMSegment.class);
		query.setParameter("startNode", startNode);
		query.setParameter("endNode", endNode);
		OSMSegment osmSegment = query.getSingleResult();

		build(stream, osmSegment);
	}

	public void build(PrintStream stream, OSMSegment osmSegment) {
		EntityManager em = DBUtils.getEntityManager();
		List<Shift> shifts = osmSegment.getShifts();

		/*
		 * We iterate all the shifts. We keep the latest when there are
		 * duplicates unless any of the duplicates has weird measures.
		 */
		HashMap<String, Shift> idShift = new HashMap<>();
		for (Shift shift : shifts) {
			if (shift.getSpeed() < 0 || shift.getSpeed() > 80) {
				// Remove weird measures
				continue;
			}
			ShiftEntryImpl shiftEntry = new ShiftEntryImpl(shift);
			String shiftId = shiftEntry.getId();
			Shift duplicatedShift = idShift.get(shiftId);
			if (duplicatedShift == null) {
				idShift.put(shiftId, shift);
			} else {
				if (shift.getTimestamp() > duplicatedShift.getTimestamp()) {
					idShift.put(shiftId, shift);
				}
			}
		}

		Dataset dataset = new Dataset(stream);
		dataset.writeHeader();
		Collection<Shift> cleanShifts = idShift.values();
		for (Shift shift : cleanShifts) {
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
