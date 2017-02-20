package org.fergonco.tpg.trafficViewer.osmrouting;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class OSMUtils {

	private static GeometryFactory gf = new GeometryFactory();

	public static LineString buildLineString(List<OSMNode> path) {
		Coordinate[] coordinates = new Coordinate[path.size()];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = path.get(i).getCoordinate();
		}
		LineString ret = gf.createLineString(coordinates);
		return ret;
	}

	public static Geometry buildPoint(Coordinate coordinate) {
		return gf.createPoint(coordinate);
	}

	public static Geometry buildPoint(Coordinate coordinate, int srid) {
		Geometry ret = buildPoint(coordinate);
		ret.setSRID(srid);
		return ret;
	}

	public static Geometry buildLineString(OSMNode startNode, OSMNode endNode, int srid) {
		ArrayList<OSMNode> nodes = new ArrayList<>();
		nodes.add(startNode);
		nodes.add(endNode);
		LineString ret = buildLineString(nodes);
		ret.setSRID(srid);
		return ret;
	}

}
