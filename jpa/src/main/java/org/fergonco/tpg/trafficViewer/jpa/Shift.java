package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long timestamp;
	private String vehicleId;
	private int seconds;
	private String sourceShiftId;

	private TPGStopRoute route;

	public long getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public String getSourceShiftId() {
		return sourceShiftId;
	}

	public void setSourceShiftId(String sourceShiftId) {
		this.sourceShiftId = sourceShiftId;
	}

	public void setRoute(TPGStopRoute route) {
		this.route = route;
	}

	public TPGStopRoute getRoute() {
		return route;
	}
}
