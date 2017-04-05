package org.fergonco.traffic.dataGatherer;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.traffic.dataGatherer.osmrouting.OSMRouting;
import org.fergonco.traffic.dataGatherer.osmrouting.OSMRoutingResult;
import org.fergonco.traffic.dataGatherer.osmrouting.OSMStep;
import org.fergonco.traffic.dataGatherer.osmrouting.OSMUtils;
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

	private static final String TRAFFIC_VIEWER_OSM_NETWORK = "TRAFFIC_VIEWER_OSM_NETWORK";
	private OSMRouting osmRouting = new OSMRouting();

	private EntityManager em;

	public DBThermometerListener(EntityManager em, File osmxml)
			throws ParserConfigurationException, SAXException, IOException {
		this.em = em;
		osmRouting.init(osmxml);
		logger.info("initialized");
	}

	public DBThermometerListener(EntityManager em) throws ParserConfigurationException, SAXException, IOException {
		this.em = em;
		String osmNetworkPath = System.getenv(TRAFFIC_VIEWER_OSM_NETWORK);
		if (osmNetworkPath == null) {
			throw new IllegalStateException(
					TRAFFIC_VIEWER_OSM_NETWORK + " must be defined when using empty parameters constructor");
		}
		osmRouting.init(new File(osmNetworkPath));

		logger.info("initialized");
	}

	@Override
	public void stepActualTimestampChanged(Step previousStep, Step currentStep, String destination) {
		logger.info("Previous: " + previousStep);
		logger.info("Current: " + currentStep);
		if (previousStep != null) {
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

			// Remove existing shift
			em.getTransaction().begin();
			String sourceShiftId = getDateId(currentStep) + "+" + currentStep.getDepartureCode();
			try {
				TypedQuery<Shift> query = em.createQuery("SELECT s FROM Shift s WHERE s.sourceShiftId=:sourceShiftId",
						Shift.class);
				query.setParameter("sourceShiftId", sourceShiftId);
				Shift existingShift = query.getSingleResult();
				long shiftId = existingShift.getId();
				em.createQuery("DELETE FROM OSMShift s WHERE s.shift=:shift").setParameter("shift", existingShift)
						.executeUpdate();
				em.createQuery("DELETE FROM Shift s WHERE s.id=:id").setParameter("id", shiftId).executeUpdate();
			} catch (NoResultException e) {
			}

			// Insert shift
			Shift shift = new Shift();
			shift.setSourceShiftId(sourceShiftId);
			shift.setSourceStartPoint(previousStep.getStopCode());
			shift.setSourceEndPoint(currentStep.getStopCode());
			shift.setSourceType("TPG");
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

			OSMStep[] wayIdsAndSenses = result.getWayIdsAndSenses();
			for (OSMStep osmStep : wayIdsAndSenses) {
				OSMShift osmShift = new OSMShift();
				osmShift.setShift(shift);
				osmShift.setStartNode(Long.parseLong(osmStep.getStartNode().getId()));
				osmShift.setEndNode(Long.parseLong(osmStep.getEndNode().getId()));
				osmShift.setGeom(OSMUtils.buildLineString(osmStep.getStartNode(), osmStep.getEndNode(), 4326));
				em.persist(osmShift);
			}
			em.getTransaction().commit();
		}
	}

	private String getDateId(Step currentStep) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(currentStep.getActualTimestamp());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		if (c.get(Calendar.HOUR_OF_DAY) < 2) {
			dayOfMonth--;
		}
		return dayOfMonth + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
	}

}
