package org.fergonco.traffic.dataGatherer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
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
			TPGStopRoute route = distanceQuery.getSingleResult();

			// Remove existing shift
			em.getTransaction().begin();
			String sourceShiftId = getDateId(currentStep) + "+" + currentStep.getDepartureCode();
			em.createQuery("DELETE FROM Shift s WHERE s.sourceShiftId=:sourceShiftId")
					.setParameter("sourceShiftId", sourceShiftId).executeUpdate();

			// Insert shift
			Shift shift = new Shift();
			shift.setSourceShiftId(sourceShiftId);
			shift.setRoute(route);
			long seconds = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / 1000;
			shift.setSeconds((int) seconds);
			shift.setVehicleId(currentStep.getDepartureCode());
			shift.setTimestamp(currentStep.getActualTimestamp());
			logger.debug("New Shift to be inserted from " + previousStep.getStopCode() + "("
					+ previousStep.getActualTimestamp() + ") to " + currentStep.getStopCode() + "("
					+ currentStep.getActualTimestamp() + ")");
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
