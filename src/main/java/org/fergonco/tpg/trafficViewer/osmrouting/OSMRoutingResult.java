package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;

import com.vividsolutions.jts.geom.LineString;

public class OSMRoutingResult {

	private GraphPath<OSMNode, OSMStep> result;

	public OSMRoutingResult(GraphPath<OSMNode, OSMStep> result) {
		this.result = result;
	}

	public boolean pathFound() {
		return result != null;
	}

	public LineString getLineString() {
		List<OSMNode> path = result.getVertexList();
		return OSMUtils.buildLineString(path);
	}

	public String[] getWayIds() {
		ArrayList<String> ret = new ArrayList<>();
		List<OSMStep> edges = result.getEdgeList();
		for (OSMStep step : edges) {
			OSMWay way = step.getWay();
			if (!ret.contains(way.getId())) {
				ret.add(way.getId());
			}
		}

		return ret.toArray(new String[ret.size()]);
	}

}
