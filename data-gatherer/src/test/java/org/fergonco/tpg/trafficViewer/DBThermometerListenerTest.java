package org.fergonco.tpg.trafficViewer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.ParserConfigurationException;

import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.traffic.dataGatherer.DBThermometerListener;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;

public class DBThermometerListenerTest {

	@Test
	public void testStep() throws ParserConfigurationException, SAXException, IOException {
		String stop1 = "STOP1";
		String stop2 = "STOP2";
		String destination = "DESTINATION EVERYWHERE";
		// mock stops
		TPGStop tpgStop1 = mock(TPGStop.class);
		when(tpgStop1.getNodeId(destination)).thenReturn("1161861705");
		TPGStop tpgStop2 = mock(TPGStop.class);
		when(tpgStop2.getNodeId(destination)).thenReturn("1186330722");
		// Mock transaction
		EntityTransaction mockTransaction = mock(EntityTransaction.class);
		// Mock EntityManager
		EntityManager mockEntityManager = mock(EntityManager.class);
		when(mockEntityManager.find(TPGStop.class, stop1)).thenReturn(tpgStop1);
		when(mockEntityManager.find(TPGStop.class, stop2)).thenReturn(tpgStop2);
		when(mockEntityManager.getTransaction()).thenReturn(mockTransaction);

		DBThermometerListener listener = new DBThermometerListener(mockEntityManager, new File("ligne-y.osm.xml"));
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

		verify(mockEntityManager, times(1)).persist(argThat(new ArgumentMatcher<Shift>() {

			@Override
			public boolean matches(Object argument) {
				if (argument instanceof Shift) {
					Shift shift = (Shift) argument;
					return shift.getTimestamp() == now;
				}
				return false;
			}
		}));
		verify(mockEntityManager, atLeast(2)).persist(any(OSMShift.class));
		verify(mockTransaction, times(1)).begin();
		verify(mockTransaction, times(1)).commit();
	}
}
