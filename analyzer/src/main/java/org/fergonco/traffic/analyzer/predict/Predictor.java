package org.fergonco.traffic.analyzer.predict;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TimestampedPredictedOSMShift;

public class Predictor {
	private static final int QUARTER_OF_HOUR = 15 * 60 * 1000;
	private static final long PREDICTION_LIMIT = 24 * 60 * 60 * 1000;

	public void updatePredictions() {
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();

		// Remove existing predictions
		em.createQuery("DELETE FROM " + TimestampedPredictedOSMShift.class.getName() + " o").executeUpdate();

		// Add new predictions

		// TODO get geometry as well
		ModelBuilder modelBuilder = new ModelBuilder();
		ArrayList<OSMNodePair> nodePairs = modelBuilder.buildNodePairList();

		// Variable: day of week
		Calendar c = Calendar.getInstance();
		Date now = new Date();
		c.setTime(now);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

		long predictionTimestamp = now.getTime();
		while (predictionTimestamp - PREDICTION_LIMIT < now.getTime()) {

			// Variable: distorted minutes
			c.setTimeInMillis(predictionTimestamp);
			int hours = c.get(Calendar.HOUR_OF_DAY);
			int minutes = c.get(Calendar.MINUTE);
			int minutesSinceMidnight = hours * 60 + minutes;
			double distortedMinutes = Math.sqrt(Math.abs(450 - minutesSinceMidnight));

			for (OSMNodePair osmNodePair : nodePairs) {
				// TODO
				// Recover model from database and save it on a file
				// Run RScript reading the model from the file
				// Parser output to get speed and prediction interval

				// Insert the prediction in the table
				TimestampedPredictedOSMShift predictedShift = new TimestampedPredictedOSMShift();
				predictedShift.setMillis(predictionTimestamp);
				// TODO
				// predictedShift.setSpeed(speed);
				// predictedShift.setPredictionerror(predictionerror);
				// predictedShift.setGeom(geometry);

				em.persist(predictedShift);
			}
			predictionTimestamp += QUARTER_OF_HOUR;
		}
		em.getTransaction().commit();
	}
}
