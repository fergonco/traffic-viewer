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
public class OSMSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long startNode;
	private long endNode;
	@Column(columnDefinition = "geometry('LINESTRING', 4326)")
	@Convert(converter = JTSConverter.class)
	private Geometry geom;
	@OneToMany
	private List<Shift> shifts = new ArrayList<Shift>();

	public List<Shift> getShifts() {
		return shifts;
	}
}
