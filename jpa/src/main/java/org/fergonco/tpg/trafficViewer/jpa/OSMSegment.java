package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.vividsolutions.jts.geom.Geometry;

@Entity
public class OSMSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long startNode;
	private long endNode;

	@Column(columnDefinition = "geometry('LINESTRING', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry geom;

	private byte[] model;

	public void setModel(byte[] model) {
		this.model = model;
	}

	public byte[] getModel() {
		return model;
	}

	public Geometry getGeom() {
		return geom;
	}

	public long getId() {
		return id;
	}

	public void setStartNode(long startNode) {
		this.startNode = startNode;
	}

	public void setEndNode(long endNode) {
		this.endNode = endNode;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
}
