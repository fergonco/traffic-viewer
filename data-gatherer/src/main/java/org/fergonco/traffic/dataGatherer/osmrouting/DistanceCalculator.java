package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop2;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRoute;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRouteSegment;
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
		List<TPGStopRoute> allRoutes = em.createQuery("SELECT r FROM TPGStopRoute r", TPGStopRoute.class)
				.getResultList();
		for (TPGStopRoute tpgStopRoute : allRoutes) {
			List<TPGStopRouteSegment> segments = tpgStopRoute.getSegments();
			for (TPGStopRouteSegment tpgStopRouteSegment : segments) {
				em.remove(tpgStopRouteSegment);
			}
			em.remove(tpgStopRoute);
		}
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
		TPGStopRoute route = new TPGStopRoute();
		route.setStartTPGCode(start.getCode());
		route.setEndTPGCode(end.getCode());
		route.setLine(line.lineName);
		LineString routeGeometry = result.getLineString();
		routeGeometry.setSRID(4326);
		route.setRoute(routeGeometry);
		Geometry flatLineString = null;
		CoordinateReferenceSystem crs4326 = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem crs3857 = CRS.decode("EPSG:3857");
		MathTransform transform = CRS.findMathTransform(crs4326, crs3857);
		flatLineString = JTS.transform(routeGeometry, transform);
		double km = flatLineString.getLength() / 1000;
		route.setDistance(km);

		em.getTransaction().begin();
		OSMNode[] nodes = result.getNodes();
		for (int i = 1; i < nodes.length; i++) {
			TPGStopRouteSegment segment = new TPGStopRouteSegment();
			segment.setStartNode(Long.parseLong(nodes[i - 1].getId()));
			segment.setEndNode(Long.parseLong(nodes[i].getId()));
			segment.setGeometry(OSMUtils.buildLineString(nodes[i - 1], nodes[i], 4326));
			segment.setRoute(route);
			em.persist(segment);
			route.getSegments().add(segment);
		}
		em.persist(route);
		em.getTransaction().commit();
	}
}
