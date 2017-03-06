package org.fergonco.traffic.dataGatherer.osmrouting;

import org.jgrapht.DirectedGraph;

/**
 * This class represents a step between two OSMNodes in the graph. All steps
 * instances should be different (do not override equals & hashcode) so that
 * every call to {@link DirectedGraph#addEdge(Object, Object, Object)} take
 * effect.
 * 
 * @author fergonco
 */
public class OSMStep {

	private OSMNode startNode;
	private OSMNode endNode;
	private OSMWay way;

	public OSMStep(OSMNode startNode, OSMNode endNode, OSMWay way) {
		super();
		this.startNode = startNode;
		this.endNode = endNode;
		this.way = way;
	}

	public OSMWay getWay() {
		return way;
	}

	public OSMNode getStartNode() {
		return startNode;
	}

	public OSMNode getEndNode() {
		return endNode;
	}
}
