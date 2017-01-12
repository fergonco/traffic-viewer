package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OSMShift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long osmId;

	@ManyToOne
	private Shift shift;

	public void setOsmId(long osmId) {
		this.osmId = osmId;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

}
