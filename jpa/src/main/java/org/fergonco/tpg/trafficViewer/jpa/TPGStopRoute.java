package org.fergonco.tpg.trafficViewer.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class TPGStopRoute {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String startTPGCode;
	private String endTPGCode;
	private String line;
	private double distance;
	@Column(columnDefinition = "geometry('LINESTRING', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry route;
	@OneToMany
	private List<OSMSegment> segments = new ArrayList<OSMSegment>();

	public long getId() {
		return id;
	}

	public void setStartTPGCode(String startTPGCode) {
		this.startTPGCode = startTPGCode;
	}

	public String getStartTPGCode() {
		return startTPGCode;
	}

	public void setEndTPGCode(String endTPGCode) {
		this.endTPGCode = endTPGCode;
	}

	public String getEndTPGCode() {
		return endTPGCode;
	}

	public void setDistance(double d) {
		this.distance = d;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLine() {
		return line;
	}

	public void setRoute(Geometry route) {
		this.route = route;
	}

	public double getDistance() {
		return distance;
	}

	public List<OSMSegment> getSegments() {
		return segments;
	}

}
