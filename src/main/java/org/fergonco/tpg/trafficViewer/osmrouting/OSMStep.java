package org.fergonco.tpg.trafficViewer.osmrouting;

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

	private OSMWay way;

	public OSMStep(OSMWay way) {
		super();
		this.way = way;
	}

	public OSMWay getWay() {
		return way;
	}
}
