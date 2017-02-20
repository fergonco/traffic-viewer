package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class OSMShift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long startNode;
	private long endNode;
	@Column(columnDefinition = "geometry('LINESTRING', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry geom;

	@ManyToOne
	private Shift shift;

	public long getStartNode() {
		return startNode;
	}

	public void setStartNode(long startNode) {
		this.startNode = startNode;
	}

	public long getEndNode() {
		return endNode;
	}

	public void setEndNode(long endNode) {
		this.endNode = endNode;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public Shift getShift() {
		return shift;
	}
}
