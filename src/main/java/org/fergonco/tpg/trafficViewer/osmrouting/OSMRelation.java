package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMRelation {
	private ArrayList<OSMWay> ways = new ArrayList<>();
	private ArrayList<OSMNode> nodes = new ArrayList<>();
	private HashMap<String, String> tags = new HashMap<>();

	public Iterable<OSMWay> getWays() {
		return ways;
	}

	public Iterable<OSMNode> getNodes() {
		return nodes;
	}

	public void addWay(OSMWay way) {
		ways.add(way);
	}

	public void addNode(OSMNode node) {
		nodes.add(node);
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

	public OSMNode[] getNClosestNodes(Coordinate coordinate, int n) {
		TreeSet<OSMNode> sortedNodes = new TreeSet<>(new Comparator<OSMNode>() {

			@Override
			public int compare(OSMNode o1, OSMNode o2) {
				return (int) Math
						.signum(o1.getCoordinate().distance(coordinate) - o2.getCoordinate().distance(coordinate));
			}
		});
		for (OSMNode osmNode : nodes) {
			sortedNodes.add(osmNode);
		}
		ArrayList<OSMNode> ret = new ArrayList<>();
		for (OSMNode osmNode : sortedNodes) {
			ret.add(osmNode);
			if (ret.size() == n) {
				break;
			}
		}
		return ret.toArray(new OSMNode[n]);
	}

}
