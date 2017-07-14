package org.fergonco.traffic.dataGatherer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRoute;

import co.geomati.tpg.Step;
import co.geomati.tpg.ThermometerListener;

public class DBThermometerListener implements ThermometerListener {

	private static Logger logger = LogManager.getLogger(DBThermometerListener.class);

	@Override
	public void stepActualTimestampChanged(Step previousStep, Step currentStep, String line, String destination) {
		EntityManager em = DBUtils.getEntityManager();
		logger.info("Previous: " + previousStep);
		logger.info("Current: " + currentStep);
		if (previousStep != null) {
			TypedQuery<TPGStopRoute> distanceQuery = em.createQuery("SELECT d " + "FROM TPGStopRoute d "
					+ "WHERE d.startTPGCode=:starttpgcode " + "AND d.endTPGCode=:endtpgcode " + "AND d.line=:line",
					TPGStopRoute.class);
			distanceQuery.setParameter("starttpgcode", previousStep.getStopCode());
			distanceQuery.setParameter("endtpgcode", currentStep.getStopCode());
			distanceQuery.setParameter("line", line);
			TPGStopRoute distance = distanceQuery.getSingleResult();

			// Remove existing shift
			em.getTransaction().begin();
			String sourceShiftId = getDateId(currentStep) + "+" + currentStep.getDepartureCode();
			try {
				TypedQuery<Shift> query = em.createQuery("SELECT s FROM Shift s WHERE s.sourceShiftId=:sourceShiftId",
						Shift.class);
				query.setParameter("sourceShiftId", sourceShiftId);
				Shift existingShift = query.getSingleResult();
				List<OSMSegment> segments = existingShift.getSegments();
				for (OSMSegment osmSegment : segments) {
					osmSegment.getShifts().remove(existingShift);
					em.persist(osmSegment);
				}
				segments.clear();
				em.persist(existingShift);
				em.remove(existingShift);
			} catch (NoResultException e) {
			}

			// Insert shift
			Shift shift = new Shift();
			shift.setSourceShiftId(sourceShiftId);
			shift.setSourceStartPoint(previousStep.getStopCode());
			shift.setSourceEndPoint(currentStep.getStopCode());
			shift.setSourceLineCode(line);
			shift.setSourceType("TPG");
			long seconds = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / 1000;
			shift.setSeconds((int) seconds);
			shift.setVehicleId(currentStep.getDepartureCode());
			shift.setTimestamp(currentStep.getActualTimestamp());
			logger.debug("New Shift to be inserted from " + previousStep.getStopCode() + "("
					+ previousStep.getActualTimestamp() + ") to " + currentStep.getStopCode() + "("
					+ currentStep.getActualTimestamp() + ")");
			shift.setSegments(distance.getSegments());
			for (OSMSegment segment : distance.getSegments()) {
				segment.getShifts().add(shift);
				em.persist(segment);
			}
			em.persist(shift);
			em.getTransaction().commit();
		}
	}

	private String getDateId(Step currentStep) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(currentStep.getActualTimestamp());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		if (c.get(Calendar.HOUR_OF_DAY) < 2) {
			dayOfMonth--;
		}
		return dayOfMonth + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
	}

}
