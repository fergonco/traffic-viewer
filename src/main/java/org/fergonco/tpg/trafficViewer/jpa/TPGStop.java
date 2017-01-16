package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

@Entity
public class TPGStop {

	@Id
	private String code;

	@Column(columnDefinition = "geometry('POINT', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry geom;

	@Override
	public String toString() {
		Coordinate coordinate = geom.getCentroid().getCoordinate();
		return code + "(" + coordinate.y + ", " + coordinate.x + ")";
	}

	public Coordinate getCoordinate() {
		return geom.getCentroid().getCoordinate();
	}
}
