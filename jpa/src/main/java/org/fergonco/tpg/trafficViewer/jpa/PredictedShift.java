package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PredictedShift {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	private long millis;
	private int speed;
	private float predictionerror;
	private OSMSegment segment;

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setPredictionerror(float predictionerror) {
		this.predictionerror = predictionerror;
	}

	public long getMillis() {
		return millis;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSegment(OSMSegment segment) {
		this.segment = segment;
	}
}
