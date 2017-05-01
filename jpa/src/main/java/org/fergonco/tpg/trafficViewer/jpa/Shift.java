package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long timestamp;
	private String vehicleId;
	private int speed;
	private String sourceType;
	private String sourceStartPoint;
	private String sourceEndPoint;
	private String sourceShiftId;
	@Column(columnDefinition = "geometry('POINT', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry startPoint;
	@Column(columnDefinition = "geometry('POINT', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry endPoint;

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

	public Geometry getStartPoint() {
		return startPoint;
	}

	/**
	 * Must be kept because it's the only data we have about the stops of the
	 * shifts for the data that was gathered at the beginning
	 * 
	 * @param startPoint
	 */
	@Deprecated
	public void setStartPoint(Geometry startPoint) {
		this.startPoint = startPoint;
	}

	public Geometry getEndPoint() {
		return endPoint;
	}

	/**
	 * Must be kept because it's the only data we have about the stops of the
	 * shifts for the data that was gathered at the beginning
	 * 
	 * @param startPoint
	 */
	@Deprecated
	public void setEndPoint(Geometry endPoint) {
		this.endPoint = endPoint;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceStartPoint() {
		return sourceStartPoint;
	}

	public void setSourceStartPoint(String sourceStartPoint) {
		this.sourceStartPoint = sourceStartPoint;
	}

	public String getSourceEndPoint() {
		return sourceEndPoint;
	}

	public void setSourceEndPoint(String sourceEndPoint) {
		this.sourceEndPoint = sourceEndPoint;
	}

	public String getSourceShiftId() {
		return sourceShiftId;
	}

	public void setSourceShiftId(String sourceShiftId) {
		this.sourceShiftId = sourceShiftId;
	}

}
