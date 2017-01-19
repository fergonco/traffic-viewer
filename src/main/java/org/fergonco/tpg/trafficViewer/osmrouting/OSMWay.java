package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.HashMap;

public class OSMWay {

	private String id;
	private ArrayList<OSMNode> nodes = new ArrayList<>();
	private HashMap<String, String> tags = new HashMap<>();

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

	// public void removeNonGraphNodes() {
	// ArrayList<OSMNode> toRemove = new ArrayList<>();
	// for (int i = 1; i < nodes.size() - 1; i++) {
	// OSMNode osmNode = nodes.get(i);
	// if (osmNode.getWayCount() == 1) {
	// toRemove.add(osmNode);
	// }
	// }
	//
	// for (OSMNode osmNode : toRemove) {
	// nodes.remove(osmNode);
	// }
	// }

	public OSMNode[] getNodes() {
		return nodes.toArray(new OSMNode[nodes.size()]);
	}

	public void setTag(String key, String value) {
		tags.put(key, value);
	}

	public String getTag(String key) {
		return tags.get(key);
	}

}
