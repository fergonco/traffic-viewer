package org.fergonco.tpg.trafficViewer;

import java.util.Date;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;

import com.vividsolutions.jts.geom.Coordinate;

import co.geomati.tpg.Step;
import co.geomati.tpg.ThermometerListener;

public class DBThermometerListener implements ThermometerListener {

	@Override
	public void stepActualTimestampChanged(Step previousStep, Step currentStep) {
		if (previousStep != null) {
			EntityManager em = DBUtils.getEntityManager();
			TPGStop start = em.find(TPGStop.class, previousStep.getStopCode());
			TPGStop end = em.find(TPGStop.class, currentStep.getStopCode());
			Shift shift = new Shift();
			Coordinate endCoordinate = end.getCoordinate();
			shift.setEndLat((float) endCoordinate.y);
			shift.setEndLon((float) endCoordinate.x);
			Coordinate startCoordinate = start.getCoordinate();
			shift.setStartLat((float) startCoordinate.y);
			shift.setStartLon((float) startCoordinate.x);
			double km = endCoordinate.distance(startCoordinate) / 1000;
			long h = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / (1000 * 60 * 60);
			shift.setSpeed((int) Math.round(km / h));
			shift.setVehicleId(currentStep.getDepartureCode());
			System.out.println(shift.getSpeed());
			System.out.println("\t(" + currentStep.getDepartureCode() + ") stopped at " + currentStep.getStopCode()
					+ " at " + new Date(currentStep.getActualTimestamp())
					+ (previousStep != null ? " comming from " + previousStep.getStopCode() : ""));
		}
	}

}
