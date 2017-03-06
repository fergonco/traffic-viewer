package org.fergonco.tpg.trafficViewer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.xml.parsers.ParserConfigurationException;

import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;

public class DBThermometerListenerTest {

	@BeforeClass
	public static void setup() {
		DBUtils.setPersistenceUnit("test");
	}

	@Before
	public void clean() {
		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		try {
			em.createQuery("DELETE FROM OSMShift s").executeUpdate();
			em.createQuery("DELETE FROM Shift s").executeUpdate();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
		}
	}

	@Test
	@Ignore
	public void testAddStep() throws ParserConfigurationException, SAXException, IOException {
		testStep("VATH", "THGA", "FERNEY-VOLTAIRE");
		clean();
		testStep("SGGA", "JMON", "FERNEY-VOLTAIRE");
		clean();
		testStep("SHUM", "CERN", "FERNEY-VOLTAIRE");
	}

	private void testStep(String stop1, String stop2, String destination)
			throws ParserConfigurationException, SAXException, IOException {
		DBThermometerListener listener = new DBThermometerListener(new File("ligne-y.osm.xml"));
		Step previousStep = new Step();
		long now = new Date().getTime();
		previousStep.setActualTimestamp(now - 4 * 60 * 1000);
		previousStep.setDepartureCode("12345");
		previousStep.setReliable(true);
		previousStep.setStopCode(stop1);
		Step currentStep = new Step();
		currentStep.setActualTimestamp(now);
		currentStep.setDepartureCode("12346");
		currentStep.setReliable(true);
		currentStep.setStopCode(stop2);
		listener.stepActualTimestampChanged(previousStep, currentStep, destination);

		EntityManager em = DBUtils.getEntityManager();
		List<Shift> shifts = em.createQuery("SELECT s FROM Shift s ", Shift.class).getResultList();
		assertEquals(1, shifts.size());
		assertEquals(now, shifts.get(0).getTimestamp());
		List<OSMShift> osmShifts = em.createQuery("SELECT s FROM OSMShift s ", OSMShift.class).getResultList();
		assertTrue(1 < osmShifts.size());
		for (OSMShift osmShift : osmShifts) {
			assertEquals(shifts.get(0), osmShift.getShift());
		}
		assertEquals(now, shifts.get(0).getTimestamp());
	}
}
