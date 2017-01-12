package org.fergonco.tpg.trafficViewer.contextListeners;

import java.util.Date;

import co.geomati.tpg.Step;
import co.geomati.tpg.ThermometerListener;

public class DBThermometerListener implements ThermometerListener {

	@Override
	public void stepActualTimestampChanged(Step plannedStep) {
		System.out.println("\t(" + plannedStep.getDepartureCode() + ") stopped at " + plannedStep.getStopCode() + " at "
				+ new Date(plannedStep.getActualTimestamp()));
	}

}
