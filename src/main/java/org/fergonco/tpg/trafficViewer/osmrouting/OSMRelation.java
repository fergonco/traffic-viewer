package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMRelation {
	private ArrayList<OSMWay> ways = new ArrayList<>();
	private HashMap<String, String> tags = new HashMap<>();

	public Iterable<OSMWay> getWays() {
		return ways;
	}

	public void addWay(OSMWay way) {
		ways.add(way);
	}

	public void setTag(String key, String value) {
		tags.put(key, value);
	}

	public String getTag(String key) {
		return tags.get(key);
	}

	public OSMWay getClosestWay(Coordinate coordinate) {
		double min = Double.MAX_VALUE;
		OSMWay argMin = null;
		for (OSMWay osmWay : ways) {
			double distance = osmWay.getDistance(coordinate);
			if (distance < min) {
				min = distance;
				argMin = osmWay;
			}
		}

		return argMin;
	}

	public void removeWay(OSMWay toRemove) {
		ways.remove(toRemove);
	}

}
