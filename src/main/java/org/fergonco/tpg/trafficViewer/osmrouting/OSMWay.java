package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;

public class OSMWay {

	private String id;
	private ArrayList<OSMNode> nodes = new ArrayList<>();

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

	public OSMNode getFirstNode() {
		return nodes.get(0);
	}

	public OSMNode getLastNode() {
		return nodes.get(nodes.size() - 1);
	}

}
