package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMNode {

	private String id;
	private Coordinate coordinate;
	private ArrayList<OSMWay> ways = new ArrayList<>();

	public OSMNode(String id, Coordinate coordinate) {
		super();
		this.id = id;
		this.coordinate = coordinate;
	}

	public void addWay(OSMWay way) {
		ways.add(way);
	}

	@Override
	public String toString() {
		return id + "(" + coordinate + ")";
	}

	public int getWayCount() {
		return ways.size();
	}
}
