package org.fergonco.traffic.analyzer.predict;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.PredictedShift;
import org.fergonco.traffic.analyzer.Dataset;
import org.fergonco.traffic.analyzer.OutputContext;
import org.fergonco.traffic.analyzer.OutputContext.ShiftEntry;
import org.fergonco.traffic.dataGatherer.owm.OWM;
import org.fergonco.traffic.dataGatherer.owm.WeatherForecast;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Predictor {

	private static final Logger logger = LogManager.getLogger(Predictor.class.getName());

	private static final int HOUR = 60 * 60 * 1000;
	private static final int FORECAST_STEP = 15 * 60 * 1000;
	private static final long FORECAST_LIMIT = 24 * 60 * 60 * 1000;

	public void updatePredictions() throws IOException, RException {
		updatePredictions(new OWM());
	}

	public void updatePredictions(OWM owm) throws IOException, RException {
		// Get osmshift center and get a weather prediction
		logger.debug("Get forecast for centroid");
		EntityManager em = DBUtils.getEntityManager();
		List<OSMSegment> osmSegments = em
				.createQuery("select s from " + OSMSegment.class.getSimpleName() + " s", OSMSegment.class)
				.getResultList();
		GeometryFactory gf = new GeometryFactory();
		ArrayList<Geometry> geometries = new ArrayList<>();
		HashMap<Long, OSMSegment> segmentById = new HashMap<>();
		for (OSMSegment osmSegment : osmSegments) {
			segmentById.put(osmSegment.getId(), osmSegment);
			geometries.add(osmSegment.getGeom());
		}
		GeometryCollection gc = gf.createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
		Point centroid = gc.getCentroid();
		WeatherForecast forecast = owm.forecastedConditions(centroid.getX(), centroid.getY());

		// Remove existing predictions
		logger.debug("Remove existing predictions");
		em.getTransaction().begin();
		try {
			em.createQuery("DELETE FROM " + PredictedShift.class.getName() + " o").executeUpdate();
			em.getTransaction().commit();
		} catch (RuntimeException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
		// Generate forecast folder
		File forecastFolder = Files.createTempDirectory("traffic-forecast").toFile();

		// Build prediction dataset
		Date now = new Date();
		long forecastTimestamp = now.getTime();
		logger.debug("Predicting for next " + (FORECAST_LIMIT / HOUR) + " hours each " + (FORECAST_STEP / (double) HOUR)
				+ " hours");
		PrintStream datasetStream = new PrintStream(new FileOutputStream(new File(forecastFolder, "dataset.csv")));
		Dataset dataset = new Dataset(datasetStream);
		dataset.writeHeader();
		while (forecastTimestamp - FORECAST_LIMIT < now.getTime()) {
			logger.debug(forecastTimestamp);
			OutputContext outputContext = new OutputContext(new ForecastShiftEntry(forecastTimestamp),
					forecast.getForecast(forecastTimestamp));
			dataset.writeEntry(outputContext);
			forecastTimestamp += FORECAST_STEP;
		}
		datasetStream.close();

		// Write model files
		logger.debug("Writting model files");
		for (OSMSegment osmSegment : osmSegments) {
			if (osmSegment.getModel() == null) {
				continue;
			}
			File modelFile = new File(forecastFolder, osmSegment.getId() + ".rds");
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(modelFile));
			IOUtils.write(osmSegment.getModel(), outputStream);
			outputStream.close();
		}

		// Run RScript processing folder and get a map from predictions
		logger.debug("Getting forecasts");
		try {
			Rscript rscript = new Rscript();
			InputStream predictionOutput = rscript.executeResource("predictor.r", forecastFolder.getAbsolutePath());
			BufferedReader reader = new BufferedReader(new InputStreamReader(predictionOutput));
			Pattern pattern = Pattern
					.compile("Result\\|(\\d+)\\|(\\d+)\\|(\\d+(?:\\.\\d+)?)\\|(\\d+(?:\\.\\d+)?)\\|(\\d+(?:\\.\\d+)?)");
			String line = null;
			int persistCounter = 0;
			int batchSize = 100;
			em.getTransaction().begin();
			while ((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					try {
						Long id = new Long(matcher.group(1));
						long timestamp = Long.parseLong(matcher.group(2));
						double prediction = Double.parseDouble(matcher.group(3));
						double lowerEnd = Double.parseDouble(matcher.group(4));
						double upperEnd = Double.parseDouble(matcher.group(5));

						PredictedShift predictedShift = new PredictedShift();
						predictedShift.setMillis(timestamp);
						predictedShift.setSpeed((int) prediction);
						predictedShift.setPredictionerror((float) ((upperEnd - lowerEnd) / 2));
						predictedShift.setSegment(segmentById.get(id));

						em.persist(predictedShift);

						persistCounter++;
						if (persistCounter % batchSize == 0) {
							em.flush();
							em.clear();
						}

					} catch (NumberFormatException e) {
						logger.error("Bad R output: \"" + line + "\"");
					}
				} else {
					logger.info("Ignoring Rscript output: \"" + line + "\"");
				}
			}
			em.getTransaction().commit();
			if (rscript.getExitCode() != 0) {
				logger.error("Script exited with non zero code");
			}
		} catch (IOException e) {
			logger.error("Cannot get predictions", e);
		} finally {
			FileUtils.deleteDirectory(forecastFolder);
		}
		logger.debug("predictions done");
	}

	private class ForecastShiftEntry implements ShiftEntry {

		private long forecastTimestamp;

		public ForecastShiftEntry(long forecastTimestamp) {
			this.forecastTimestamp = forecastTimestamp;
		}

		@Override
		public long getTimestamp() {
			return forecastTimestamp;
		}

		@Override
		public int getSpeed() {
			// Whatever, the linear model will ignore this variable
			return -1;
		}

		@Override
		public String getId() {
			// Whatever, we have only one entry
			return "0";
		}
	}
}
