package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegmentModel;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TimestampedPredictedOSMShift;
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
		Shift shift = new Shift();
		OSMShift osmShift = new OSMShift();
		osmShift.setStartNode(10);
		osmShift.setEndNode(11);
		LineString geometry = new GeometryFactory()
				.createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(10, 10) });
		geometry.setSRID(4326);
		osmShift.setGeom(geometry);
		osmShift.setShift(shift);
		em.getTransaction().begin();
		em.persist(shift);
		em.persist(osmShift);
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
		predictor.updatePredictions();

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

}
