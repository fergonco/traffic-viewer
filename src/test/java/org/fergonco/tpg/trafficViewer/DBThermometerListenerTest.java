package org.fergonco.tpg.trafficViewer;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;

public class DBThermometerListenerTest {

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
	}
}
