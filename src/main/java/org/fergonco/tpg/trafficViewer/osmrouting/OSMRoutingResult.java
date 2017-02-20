package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.List;

import org.jgrapht.GraphPath;

import com.vividsolutions.jts.geom.LineString;

public class OSMRoutingResult {

	private GraphPath<OSMNode, OSMStep> result;

	public OSMRoutingResult(GraphPath<OSMNode, OSMStep> result) {
		if (result == null) {
			throw new NullPointerException();
		}
		this.result = result;
	}

	public LineString getLineString() {
		List<OSMNode> path = result.getVertexList();
		return OSMUtils.buildLineString(path);
	}

	public OSMStep[] getWayIdsAndSenses() {
		return result.getEdgeList().toArray(new OSMStep[0]);
	}

}
