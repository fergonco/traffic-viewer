package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.TestUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.PredictedShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRoute;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
import org.fergonco.traffic.dataGatherer.owm.OWM;
import org.fergonco.traffic.dataGatherer.owm.WeatherForecast;
import org.junit.Before;
import org.junit.Test;

public class ModelBuilderTest {

	@Before
	public void prepareDatabase() throws Exception {
		TestUtils.configureDBUtilsAndClearDatabase();
	}

	@Test
	public void testGenerateModelAndPredict() throws Exception {
		// Populate database
		EntityManager em = DBUtils.getEntityManager();
		TPGStopRoute route = em
				.createQuery(
						"SELECT r FROM " + TPGStopRoute.class.getSimpleName()
								+ " r WHERE r.startTPGCode='VATH' AND r.endTPGCode='THGA' AND r.line='Y'",
						TPGStopRoute.class)
				.getSingleResult();
		em.getTransaction().begin();
		long now = new Date().getTime();
		int FIVE_HOURS = 5 * 60 * 60 * 1000;
		for (int i = 0; i < 30; i++) {
			persistShift(em, i, now - i * FIVE_HOURS, route);
			persistWeatherConditions(em, i, now - i * FIVE_HOURS - 1000);
		}
		em.getTransaction().commit();

		// Generate models
		ModelBuilder builder = new ModelBuilder();
		List<OSMSegment> segments = route.getSegments();
		ArrayList<Long> segmentIds = new ArrayList<>(segments.size());
		for (OSMSegment osmSegment : segments) {
			segmentIds.add(osmSegment.getId());
		}
		builder.generateModels(segmentIds);

		// Check the models are generated
		List<OSMSegment> resultList = em
				.createQuery("select m FROM " + OSMSegment.class.getName() + " m where m.model is not null",
						OSMSegment.class)
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
		List<PredictedShift> predictedOSMShifts = em
				.createQuery("select p FROM " + PredictedShift.class.getName() + " p order by p.millis",
						PredictedShift.class)
				.getResultList();
		assertTrue("there are predictions", predictedOSMShifts.size() > 0);
		assertTrue("speed predictions are not 0", predictedOSMShifts.get(0).getSpeed() > 0);
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

	private void persistShift(EntityManager em, int index, long timestamp, TPGStopRoute route) {
		Shift shift = new Shift();
		shift.setTimestamp(timestamp);
		shift.setVehicleId(Integer.toString(index));
		shift.setRoute(route);
		shift.setSeconds(300);
		em.persist(shift);
	}

}
