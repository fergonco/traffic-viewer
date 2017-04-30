package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class TPGStopDistance {

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

	public void setStartTPGCode(String startTPGCode) {
		this.startTPGCode = startTPGCode;
	}

	public void setEndTPGCode(String endTPGCode) {
		this.endTPGCode = endTPGCode;
	}

	public void setDistance(double d) {
		this.distance = d;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setRoute(Geometry route) {
		this.route = route;
	}
}
