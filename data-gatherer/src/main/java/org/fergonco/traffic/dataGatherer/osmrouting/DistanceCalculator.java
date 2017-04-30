package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop2;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopDistance;
import org.fergonco.traffic.dataGatherer.Utils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class DistanceCalculator {

	public static void main(String[] args) throws Exception {
		ArrayList<Line> lines = new ArrayList<>();
		lines.add(Line.read("line-y.txt"));
		lines.add(Line.read("line-o.txt"));
		ArrayList<String> lineNames = new ArrayList<>();
		for (Line line : lines) {
			lineNames.add(line.lineName);
		}
		OSMRouter osmRouter = new OSMRouter(new File("ligne-y.osm.xml"),
				lineNames.toArray(new String[lineNames.size()]));

		EntityManager em = DBUtils.getEntityManager();
		em.getTransaction().begin();
		em.createQuery("DELETE FROM TPGStopDistance distance").executeUpdate();
		em.getTransaction().commit();
		for (Line line : lines) {
			String[] stopSequence = line.stopSequence;
			for (int i = 0; i < stopSequence.length - 1; i++) {
				TPGStop2 start = Utils.getTPGStop(em, stopSequence[i], line.lineName, line.forwardDestination);
				TPGStop2 end = Utils.getTPGStop(em, stopSequence[i + 1], line.lineName, line.forwardDestination);
				persist(osmRouter, em, line, start, end);
				start = Utils.getTPGStop(em, stopSequence[i + 1], line.lineName, line.backwardDestination);
				end = Utils.getTPGStop(em, stopSequence[i], line.lineName, line.backwardDestination);
				persist(osmRouter, em, line, start, end);
			}
		}
	}

	private static void persist(OSMRouter osmRouter, EntityManager em, Line line, TPGStop2 start, TPGStop2 end)
			throws MismatchedDimensionException, TransformException, NoSuchAuthorityCodeException, FactoryException {
		String startNodeId = start.getNodeId();
		String endNodeId = end.getNodeId();
		OSMRoutingResult result = osmRouter.getPathFromNodeOutsideGraph(line.lineName, startNodeId, endNodeId);
		TPGStopDistance distance = new TPGStopDistance();
		distance.setStartTPGCode(start.getCode());
		distance.setEndTPGCode(end.getCode());
		distance.setLine(line.lineName);
		LineString route = result.getLineString();
		route.setSRID(4326);
		distance.setRoute(route);
		Geometry flatLineString = null;
		CoordinateReferenceSystem crs4326 = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem crs3857 = CRS.decode("EPSG:3857");
		MathTransform transform = CRS.findMathTransform(crs4326, crs3857);
		flatLineString = JTS.transform(route, transform);
		double km = flatLineString.getLength() / 1000;
		distance.setDistance(km);
		em.getTransaction().begin();
		em.persist(distance);
		em.getTransaction().commit();
	}
}
