package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class OSMRoutingResult {

	private static GeometryFactory gf = new GeometryFactory();

	private GraphPath<OSMNode, OSMStep> result;

	public OSMRoutingResult(GraphPath<OSMNode, OSMStep> result) {
		this.result = result;
	}

	public LineString getLineString() {
		List<OSMNode> path = result.getVertexList();
		Coordinate[] coordinates = new Coordinate[path.size()];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = path.get(i).getCoordinate();
		}
		return gf.createLineString(coordinates);
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
