package org.fergonco.traffic.dataGatherer.osmrouting;

import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class OSMWay {

	private String id;
	private ArrayList<OSMNode> nodes = new ArrayList<>();
	private HashMap<String, String> tags = new HashMap<>();
	private LineString lineString = null;
	private String noRoute = null;

	public OSMWay(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void addNode(OSMNode osmNode) {
		nodes.add(osmNode);
	}

	public OSMNode[] getNodes() {
		return nodes.toArray(new OSMNode[nodes.size()]);
	}

	public void setTag(String key, String value) {
		tags.put(key, value);
	}

	public String getTag(String key) {
		return tags.get(key);
	}

	public double getDistance(Coordinate coordinate) {
		return getLineString().distance(OSMUtils.buildPoint(coordinate));
	}

	public LineString getLineString() {
		if (lineString == null) {
			lineString = OSMUtils.buildLineString(nodes);

		}

		return lineString;
	}

	public OSMNode getClosestNode(Coordinate coordinate) {
		double min = Double.MAX_VALUE;
		OSMNode argMin = null;
		for (OSMNode osmNode : nodes) {
			double distance = osmNode.getCoordinate().distance(coordinate);
			if (distance < min) {
				min = distance;
				argMin = osmNode;
			}
		}

		return argMin;
	}

	public boolean isSenseForward(OSMNode startNode, OSMNode endNode) {
		OSMNode previousNode = null;
		for (OSMNode osmNode : nodes) {
			if (osmNode == startNode) {
				return endNode != previousNode;
			}
			previousNode = osmNode;
		}

		throw new IllegalArgumentException("Start node does not belong to the way: " + startNode);
	}

	public void setNoRoute(String routeDirection) {
		this.noRoute = routeDirection;
	}

	public boolean isNoRouteForwards() {
		return "forwards".equals(noRoute);
	}

	public boolean isNoRouteBackwards() {
		return "backwards".equals(noRoute);
	}

	public void removeNode(String id) {
		for (OSMNode node : nodes) {
			if (node.getId().equals(id)) {
				nodes.remove(node);
				break;
			}
		}
	}

}
