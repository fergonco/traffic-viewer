package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
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

	public boolean pathFound() {
		return result != null;
	}

	public LineString getLineString() {
		List<OSMNode> path = result.getVertexList();
		return OSMUtils.buildLineString(path);
	}

	public OSMWayIdAndSense[] getWayIdsAndSenses() {
		ArrayList<OSMWayIdAndSense> ret = new ArrayList<>();
		List<OSMStep> edges = result.getEdgeList();
		for (OSMStep step : edges) {
			OSMNode startNode = step.getStartNode();
			OSMNode endNode = step.getEndNode();
			OSMWay way = step.getWay();
			OSMWayIdAndSense wayIdSense = new OSMWayIdAndSense(way.getId(), way.isSenseForward(startNode, endNode));
			if (!ret.contains(wayIdSense)) {
				ret.add(wayIdSense);
			}
		}

		return ret.toArray(new OSMWayIdAndSense[ret.size()]);
	}

}
