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
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRoute;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRouteSegment;

import com.vividsolutions.jts.geom.Geometry;

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
				long shiftId = existingShift.getId();
				em.createQuery("DELETE FROM OSMShift s WHERE s.shift=:shift").setParameter("shift", existingShift)
						.executeUpdate();
				em.createQuery("DELETE FROM Shift s WHERE s.id=:id").setParameter("id", shiftId).executeUpdate();
			} catch (NoResultException e) {
			}

			// Insert shift
			Shift shift = new Shift();
			shift.setSourceShiftId(sourceShiftId);
			shift.setSourceStartPoint(previousStep.getStopCode());
			shift.setSourceEndPoint(currentStep.getStopCode());
			shift.setSourceType("TPG");
			shift.setEndPoint(null);
			shift.setStartPoint(null);
			double km = distance.getDistance();
			double h = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / (1000.0 * 60 * 60);
			shift.setSpeed((int) Math.round(km / h));
			shift.setVehicleId(currentStep.getDepartureCode());
			shift.setTimestamp(currentStep.getActualTimestamp());
			logger.debug("New Shift to be inserted from " + previousStep.getStopCode() + "("
					+ previousStep.getActualTimestamp() + ") to " + currentStep.getStopCode() + "("
					+ currentStep.getActualTimestamp() + "). Path length: " + km + ". Speed:" + shift.getSpeed());
			em.persist(shift);

			List<TPGStopRouteSegment> segments = distance.getSegments();
			for (TPGStopRouteSegment segment : segments) {
				OSMShift osmShift = new OSMShift();
				osmShift.setShift(shift);
				osmShift.setStartNode(segment.getStartNode());
				osmShift.setEndNode(segment.getEndNode());
				Geometry segmentGeometry = segment.getGeometry();
				segmentGeometry.setSRID(4326);
				osmShift.setGeom(segmentGeometry);
				em.persist(osmShift);
			}
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
