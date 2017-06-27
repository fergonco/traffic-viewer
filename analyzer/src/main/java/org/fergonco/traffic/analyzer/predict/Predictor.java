package org.fergonco.traffic.analyzer.predict;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegmentModel;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.TimestampedPredictedOSMShift;

public class Predictor {
	private static final int QUARTER_OF_HOUR = 15 * 60 * 1000;
	private static final long PREDICTION_LIMIT = 24 * 60 * 60 * 1000;

	public void updatePredictions() throws IOException, PredictionException {
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();

		// Remove existing predictions
		em.createQuery("DELETE FROM " + TimestampedPredictedOSMShift.class.getName() + " o").executeUpdate();

		// Add new predictions

		ModelBuilder modelBuilder = new ModelBuilder();
		ArrayList<OSMShift> osmShifts = modelBuilder.getUniqueOSMShifts();

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

			for (OSMShift osmShift : osmShifts) {
				// Recover model from database and save it on a file
				TypedQuery<OSMSegmentModel> modelQuery = em
						.createQuery(
								"select m from " + OSMSegmentModel.class.getName()
										+ " m where m.startNode=:startNode and m.endNode=:endNode",
								OSMSegmentModel.class);
				modelQuery.setParameter("startNode", osmShift.getStartNode());
				modelQuery.setParameter("endNode", osmShift.getEndNode());
				OSMSegmentModel model = modelQuery.getSingleResult();
				File modelFile = File.createTempFile("rmodel", ".rds");
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(modelFile));
				IOUtils.write(model.getModel(), outputStream);
				outputStream.close();

				// TODO Build prediction dataset
				File datasetFile = File.createTempFile("predictiondataset", ".csv");
				StringBuilder datasetContent = new StringBuilder();

				// Run RScript reading the model from the file and get
				// prediction
				double[] prediction = getCenterAndPredictedInterval(modelFile, datasetFile);
				modelFile.delete();
				datasetFile.delete();

				// Insert the prediction in the table
				TimestampedPredictedOSMShift predictedShift = new TimestampedPredictedOSMShift();
				predictedShift.setMillis(predictionTimestamp);
				predictedShift.setSpeed((int) prediction[0]);
				predictedShift.setPredictionerror((float) prediction[1]);
				predictedShift.setGeom(osmShift.getGeom());

				em.persist(predictedShift);
			}
			predictionTimestamp += QUARTER_OF_HOUR;
		}
		em.getTransaction().commit();
	}

	public double[] getCenterAndPredictedInterval(File modelFile, File datasetFile)
			throws IOException, PredictionException {
		String command = "Rscript analyse/predictor.r " + modelFile.getAbsolutePath() + " "
				+ datasetFile.getAbsolutePath();
		ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s"));
		// processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		String predictionOutput = IOUtils.toString(process.getInputStream(), "utf-8");
		Pattern pattern = Pattern.compile("1\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)\\s+(\\d+\\.\\d+)");
		Matcher matcher = pattern.matcher(predictionOutput);
		if (matcher.find()) {
			double prediction = Double.parseDouble(matcher.group(1));
			double lowerEnd = Double.parseDouble(matcher.group(2));
			double upperEnd = Double.parseDouble(matcher.group(3));
			return new double[] { prediction, lowerEnd, upperEnd };
		} else {
			throw new PredictionException("Could not understand Rscript output: \"" + predictionOutput + "\"");
		}
	}
}
