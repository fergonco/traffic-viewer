package org.fergonco.tpg.trafficViewer;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;

import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.junit.Before;
import org.junit.BeforeClass;
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
		em.createQuery("DELETE FROM OSMShift s").executeUpdate();
		em.createQuery("DELETE FROM Shift s").executeUpdate();
		em.getTransaction().commit();
	}

	@Test
	public void testAddStep() throws ParserConfigurationException, SAXException, IOException {
		DBThermometerListener listener = new DBThermometerListener();
		Step previousStep = new Step();
		long now = new Date().getTime();
		previousStep.setActualTimestamp(now - 4 * 60 * 1000);
		previousStep.setDepartureCode("12345");
		previousStep.setReliable(true);
		previousStep.setStopCode("SHUM");
		Step currentStep = new Step();
		currentStep.setActualTimestamp(now);
		currentStep.setDepartureCode("12346");
		currentStep.setReliable(true);
		currentStep.setStopCode("CERN");
		listener.stepActualTimestampChanged(previousStep, currentStep);

		EntityManager em = DBUtils.getEntityManager();
		List<Shift> shifts = em.createQuery("SELECT s FROM Shift s ", Shift.class).getResultList();
		assertEquals(1, shifts.size());
		assertEquals(now, shifts.get(0).getTimestamp());
		System.out.println(shifts);
	}
}
