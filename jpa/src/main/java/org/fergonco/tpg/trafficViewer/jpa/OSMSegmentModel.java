package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class OSMSegmentModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long startNode;
	private long endNode;

	private byte[] model;

	public void setStartNode(long startNode) {
		this.startNode = startNode;
	}

	public void setEndNode(long endNode) {
		this.endNode = endNode;
	}

	public void setModel(byte[] model) {
		this.model = model;
	}

	public byte[] getModel() {
		return model;
	}

	public long getStartNode() {
		return startNode;
	}

	public long getEndNode() {
		return endNode;
	}
}
