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
	private int speed;
	private float startLat;
	private float startLon;
	private float endLat;
	private float endLon;

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

	public float getStartLat() {
		return startLat;
	}

	public void setStartLat(float startLat) {
		this.startLat = startLat;
	}

	public float getStartLon() {
		return startLon;
	}

	public void setStartLon(float startLon) {
		this.startLon = startLon;
	}

	public float getEndLat() {
		return endLat;
	}

	public void setEndLat(float endLat) {
		this.endLat = endLat;
	}

	public float getEndLon() {
		return endLon;
	}

	public void setEndLon(float endLon) {
		this.endLon = endLon;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

}
