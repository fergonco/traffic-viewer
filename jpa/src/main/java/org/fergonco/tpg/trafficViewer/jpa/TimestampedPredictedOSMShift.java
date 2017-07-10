package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class TimestampedPredictedOSMShift {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	@Column(columnDefinition = "geometry('LINESTRING', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry geom;
	private long millis;
	private int speed;
	private float predictionerror;

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

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
}
