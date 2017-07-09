package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegmentModel;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TimestampedPredictedOSMShift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
import org.fergonco.traffic.dataGatherer.owm.OWM;
import org.fergonco.traffic.dataGatherer.owm.WeatherForecast;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class ModelBuilderTest {

	@Before
	public void prepareDatabase() throws Exception {
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:54322/tpgtest", "tpg", "tpg");
		Statement statement = connection.createStatement();
		statement.execute("DROP SCHEMA IF EXISTS app CASCADE;");
		statement.execute("CREATE SCHEMA app;");
		connection.close();

		DBUtils.setPersistenceUnit("test");
	}

	@Test
	public void testGenerateModelAndPredict() throws Exception {
		// Populate database
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		LineString geometry = new GeometryFactory()
				.createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(10, 10) });
		geometry.setSRID(4326);
		long now = new Date().getTime();
		int FIVE_HOURS = 5 * 60 * 60 * 1000;
		for (int i = 0; i < 30; i++) {
			long timestamp = now - i * FIVE_HOURS;
			persistShift(em, i, 10, 11, geometry, timestamp);
			long weatherTimestamp = timestamp - 1000;
			persistWeatherConditions(em, i, weatherTimestamp);
		}
		em.getTransaction().commit();

		// Generate models
		ModelBuilder builder = new ModelBuilder();
		builder.generateModels();

		// Check the models are generated
		List<OSMSegmentModel> resultList = em
				.createQuery("select m FROM " + OSMSegmentModel.class.getName() + " m", OSMSegmentModel.class)
				.getResultList();
		assertTrue("Some model in database", resultList.size() > 0);

		// Update predictions
		Predictor predictor = new Predictor();
		OWM mock = mock(OWM.class);
		WeatherForecast weatherForecast = new WeatherForecast();
		weatherForecast.addPrediction(newTestWeatherConditions(0, now));
		when(mock.forecastedConditions(anyDouble(), anyDouble())).thenReturn(weatherForecast);
		predictor.updatePredictions(mock);

		// Check the predictions are there
		List<TimestampedPredictedOSMShift> predictedOSMShifts = em
				.createQuery("select p FROM " + TimestampedPredictedOSMShift.class.getName() + " p order by p.millis",
						TimestampedPredictedOSMShift.class)
				.getResultList();
		assertTrue("there are predictions", predictedOSMShifts.size() > 0);
		assertTrue("predictions are not 0", predictedOSMShifts.get(0).getSpeed() > 0);
		long closestPrediction = predictedOSMShifts.get(0).getMillis();
		long furtherstPrediction = predictedOSMShifts.get(predictedOSMShifts.size() - 1).getMillis();
		long range = furtherstPrediction - closestPrediction;
		assertTrue("predictions cover 24 hours", Math.abs(range - 24 * 60 * 60 * 1000) < 23 * 60 * 60 * 1000);
	}

	private void persistWeatherConditions(EntityManager em, int i, long weatherTimestamp) {
		WeatherConditions weatherConditions = newTestWeatherConditions(i, weatherTimestamp);
		em.persist(weatherConditions);
	}

	private WeatherConditions newTestWeatherConditions(int i, long weatherTimestamp) {
		WeatherConditions weatherConditions = new WeatherConditions();
		weatherConditions.setHumidity(50 + i);
		weatherConditions.setWeather((i % 2 == 0) ? 500 : 200);
		weatherConditions.setTimestamp(weatherTimestamp);
		return weatherConditions;
	}

	private void persistShift(EntityManager em, int index, int startNode, int endNode, LineString geometry,
			long timestamp) {
		Shift shift = new Shift();
		shift.setTimestamp(timestamp);
		shift.setVehicleId(Integer.toString(index));
		shift.setSpeed(20);
		OSMShift osmShift = new OSMShift();
		osmShift.setStartNode(startNode);
		osmShift.setEndNode(endNode);
		osmShift.setGeom(geometry);
		osmShift.setShift(shift);
		em.persist(shift);
		em.persist(osmShift);
	}

}
