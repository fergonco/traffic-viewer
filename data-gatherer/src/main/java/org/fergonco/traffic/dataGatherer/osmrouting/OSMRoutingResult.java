package org.fergonco.traffic.dataGatherer.osmrouting;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.LineString;

public class OSMRoutingResult {

	private GraphPath<OSMNode, DefaultWeightedEdge> result;

	public OSMRoutingResult(GraphPath<OSMNode, DefaultWeightedEdge> result) {
		if (result == null) {
			throw new NullPointerException();
		}
		this.result = result;
	}

	public LineString getLineString() {
		List<OSMNode> path = result.getVertexList();
		return OSMUtils.buildLineString(path);
	}

	public OSMNode[] getNodes() {
		return result.getVertexList().toArray(new OSMNode[0]);
	}

}
