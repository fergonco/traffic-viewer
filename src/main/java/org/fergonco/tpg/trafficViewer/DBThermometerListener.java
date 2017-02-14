package org.fergonco.tpg.trafficViewer;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMRouting;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMRoutingResult;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMUtils;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMWayIdAndSense;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import co.geomati.tpg.Step;
import co.geomati.tpg.ThermometerListener;

public class DBThermometerListener implements ThermometerListener {

	private static Logger logger = Logger.getLogger(DBThermometerListener.class);

	private static final String TRAFFIC_VIEWER_OSM_OVERRIDES = "TRAFFIC_VIEWER_OSM_OVERRIDES";
	private static final String TRAFFIC_VIEWER_OSM_NETWORK = "TRAFFIC_VIEWER_OSM_NETWORK";
	private OSMRouting osmRouting = new OSMRouting();

	public DBThermometerListener(File osmxml, File overrides)
			throws ParserConfigurationException, SAXException, IOException {
		osmRouting.init(osmxml, overrides);
		logger.info("initialized");
	}

	public DBThermometerListener() throws ParserConfigurationException, SAXException, IOException {
		String osmNetworkPath = System.getenv(TRAFFIC_VIEWER_OSM_NETWORK);
		String osmOverridesPath = System.getenv(TRAFFIC_VIEWER_OSM_OVERRIDES);
		if (osmNetworkPath == null || osmOverridesPath == null) {
			throw new IllegalStateException(TRAFFIC_VIEWER_OSM_NETWORK + " and " + TRAFFIC_VIEWER_OSM_OVERRIDES
					+ " must be defined when using empty parameters constructor");
		}
		osmRouting.init(new File(osmNetworkPath), new File(osmOverridesPath));

		logger.info("initialized");
	}

	@Override
	public void stepActualTimestampChanged(Step previousStep, Step currentStep, String destination) {
		logger.info("Previous: " + previousStep);
		logger.info("Current: " + currentStep);
		if (previousStep != null) {
			EntityManager em = DBUtils.getEntityManager();
			TPGStop start = em.find(TPGStop.class, previousStep.getStopCode());
			TPGStop end = em.find(TPGStop.class, currentStep.getStopCode());

			logger.info("Finding path from " + start.getCode() + " to " + end.getCode());
			String startNodeId = start.getNodeId(destination);
			String endNodeId = end.getNodeId(destination);
			logger.info("startNodeId: " + startNodeId);
			logger.info("endNodeId: " + endNodeId);
			OSMRoutingResult result = osmRouting.getPathFromNodeOutsideGraph(startNodeId, endNodeId);
			LineString path = result.getLineString();
			Coordinate startCoordinate = path.getCoordinateN(0);
			Coordinate endCoordinate = path.getCoordinateN(path.getNumPoints() - 1);

			em.getTransaction().begin();

			Shift shift = new Shift();
			shift.setEndPoint(OSMUtils.buildPoint(endCoordinate, 4326));
			shift.setStartPoint(OSMUtils.buildPoint(startCoordinate, 4326));
			Geometry flatLineString = null;
			try {
				CoordinateReferenceSystem crs4326 = CRS.decode("EPSG:4326");
				CoordinateReferenceSystem crs3857 = CRS.decode("EPSG:3857");
				MathTransform transform = CRS.findMathTransform(crs4326, crs3857);
				flatLineString = JTS.transform(path, transform);
			} catch (FactoryException | MismatchedDimensionException | TransformException e) {
				logger.error("Should never happen", e);
			}
			double km = flatLineString.getLength() / 1000;
			double h = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / (1000.0 * 60 * 60);
			shift.setSpeed((int) Math.round(km / h));
			shift.setVehicleId(currentStep.getDepartureCode());
			shift.setTimestamp(currentStep.getActualTimestamp());
			em.persist(shift);

			OSMWayIdAndSense[] wayIdsAndSenses = result.getWayIdsAndSenses();
			for (OSMWayIdAndSense wayIdAndSense : wayIdsAndSenses) {
				OSMShift osmShift = new OSMShift();
				osmShift.setShift(shift);
				osmShift.setOsmId(Long.parseLong(wayIdAndSense.getOsmId()));
				osmShift.setForward(wayIdAndSense.isForward());
				em.persist(osmShift);
			}

			em.getTransaction().commit();
			System.out.println("/t/t NEW SHIFT!!!");
		}
	}

}
