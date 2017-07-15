package org.fergonco.tpg.trafficViewer.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long timestamp;
	private String vehicleId;
	private int seconds;
	private String sourceType;
	private String sourceStartPoint;
	private String sourceEndPoint;
	private String sourceLineCode;
	private String sourceShiftId;
	@ManyToMany
	@JoinTable(name = "shift_osmsegment", joinColumns = @JoinColumn(name = "shift_id"), inverseJoinColumns = @JoinColumn(name = "segment_id"))
	private List<OSMSegment> segments = new ArrayList<OSMSegment>();

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

	public String getSourceLineCode() {
		return sourceLineCode;
	}

	public void setSourceLineCode(String sourceLineCode) {
		this.sourceLineCode = sourceLineCode;
	}

	public void setSegments(List<OSMSegment> segments) {
		this.segments = segments;
	}

	public List<OSMSegment> getSegments() {
		return segments;
	}
}
