package org.fergonco.tpg.trafficViewer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;

import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.traffic.dataGatherer.DBThermometerListener;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;

public class DBThermometerListenerTest {

	private static EntityManager em;
	private Step previousStep;
	private Step currentStep;
	private long now;
	private long someMinutesAgo;
	private DBThermometerListener listener;

	@Before
	public void clean() throws ParserConfigurationException, SAXException, IOException {
		TestUtils.configureDBUtilsAndClearDatabase();

		em = DBUtils.getEntityManager();
		now = new Date().getTime();
		someMinutesAgo = now - 10 * 60 * 1000;
		previousStep = new Step();
		previousStep.setStopCode("SHUM");
		previousStep.setDepartureCode("12345");
		previousStep.setTimestamp(someMinutesAgo);
		previousStep.setActualTimestamp(someMinutesAgo + 5);
		currentStep = new Step();
		currentStep.setStopCode("CERN");
		currentStep.setDepartureCode("12346");
		currentStep.setTimestamp(now);
		currentStep.setActualTimestamp(now + 5);
		listener = new DBThermometerListener();
	}

	@Test
	public void insert() {
		listener.stepActualTimestampChanged(previousStep, currentStep, "Y", "FERNEY-VOLTAIRE");

		List<Shift> list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(1, list.size());

		// Check OSMSegments have the Shift associated
		Shift shift = list.get(0);
		assertTrue(shift.getSegments().get(0).getShifts().contains(shift));
	}

	@Test
	public void avoidRepeated() {
		insert();
		int seconds = em.createQuery("SELECT s FROM Shift s", Shift.class).getSingleResult().getSeconds();

		// Correct the current shift by indicating a later arrival
		currentStep.setActualTimestamp(now + 30 * 1000);
		listener.stepActualTimestampChanged(previousStep, currentStep, "Y", "FERNEY-VOLTAIRE");

		// Same shift, therefore same record
		List<Shift> list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(1, list.size());

		// speed is slower
		Shift newShift = em.createQuery("SELECT s FROM Shift s", Shift.class).getSingleResult();
		int newSeconds = newShift.getSeconds();
		assertTrue(newSeconds > seconds);

		// Check the OSMSegments do not have the reference to the old Shift
		List<OSMSegment> segments = newShift.getSegments();
		for (OSMSegment osmSegment : segments) {
			List<Shift> shifts = osmSegment.getShifts();
			assertTrue(shifts.size() == 1);
		}
	}

	@Test
	public void newShiftNextDay() {
		insert();

		Step newPreviousStep = new Step();
		newPreviousStep.setStopCode("SHUM");
		newPreviousStep.setDepartureCode("12347");
		newPreviousStep.setTimestamp(someMinutesAgo + 24 * 60 * 60 * 1000);
		newPreviousStep.setActualTimestamp(someMinutesAgo + 24 * 60 * 60 * 1000 + 5);
		Step newCurrentStep = new Step();
		newCurrentStep.setStopCode("CERN");
		newCurrentStep.setDepartureCode("12348");
		newCurrentStep.setTimestamp(now + 24 * 60 * 60 * 1000);
		newCurrentStep.setActualTimestamp(now + 24 * 60 * 60 * 1000 + 5);
		listener.stepActualTimestampChanged(newPreviousStep, newCurrentStep, "Y", "FERNEY-VOLTAIRE");

		// Same shift, therefore same record
		List<Shift> list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(2, list.size());
	}

	@Test
	public void sameShiftAfterMidnight() {
		// any day at midnight
		Calendar c = new GregorianCalendar();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		long almostMidnight = c.getTimeInMillis();

		Step newPreviousStep = new Step();
		newPreviousStep.setStopCode("SHUM");
		newPreviousStep.setDepartureCode("12347");
		newPreviousStep.setTimestamp(almostMidnight - 5 * 60 * 1000);
		newPreviousStep.setActualTimestamp(almostMidnight - 5 * 60 * 1000);
		Step newCurrentStep = new Step();
		newCurrentStep.setStopCode("CERN");
		newCurrentStep.setDepartureCode("12348");
		newCurrentStep.setTimestamp(almostMidnight);
		newCurrentStep.setActualTimestamp(almostMidnight);
		listener.stepActualTimestampChanged(newPreviousStep, newCurrentStep, "Y", "FERNEY-VOLTAIRE");

		// First shift
		List<Shift> list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(1, list.size());

		// Correct the current shift by indicating a later arrival
		long afterMidnight = almostMidnight + 3 * 1000;
		newCurrentStep.setActualTimestamp(afterMidnight);
		listener.stepActualTimestampChanged(newPreviousStep, newCurrentStep, "Y", "FERNEY-VOLTAIRE");

		// First shift
		list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(1, list.size());
	}

	@Test
	public void newShiftWithDifferentDepartureCode() {
		insert();

		Step newPreviousStep = new Step();
		newPreviousStep.setStopCode("SHUM");
		newPreviousStep.setDepartureCode("12347");
		newPreviousStep.setTimestamp(someMinutesAgo);
		newPreviousStep.setActualTimestamp(someMinutesAgo + 5);
		Step newCurrentStep = new Step();
		newCurrentStep.setStopCode("CERN");
		newCurrentStep.setDepartureCode("12348");
		newCurrentStep.setTimestamp(now);
		newCurrentStep.setActualTimestamp(now + 5);
		listener.stepActualTimestampChanged(newPreviousStep, newCurrentStep, "Y", "FERNEY-VOLTAIRE");

		// Same shift, therefore same record
		List<Shift> list = em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();
		assertEquals(2, list.size());
	}
}
