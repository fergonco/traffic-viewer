package org.fergonco.tpg.trafficViewer;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;

import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMRouting;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMRoutingResult;
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

	private static final String TRAFFIC_VIEWER_OSM_OVERRIDES = "TRAFFIC_VIEWER_OSM_OVERRIDES";
	private static final String TRAFFIC_VIEWER_OSM_NETWORK = "TRAFFIC_VIEWER_OSM_NETWORK";
	private OSMRouting osmRouting = new OSMRouting();

	public DBThermometerListener(File osmxml, File overrides)
			throws ParserConfigurationException, SAXException, IOException {
		osmRouting.init(osmxml, overrides);
	}

	public DBThermometerListener() throws ParserConfigurationException, SAXException, IOException {
		String osmNetworkPath = System.getenv(TRAFFIC_VIEWER_OSM_NETWORK);
		String osmOverridesPath = System.getenv(TRAFFIC_VIEWER_OSM_OVERRIDES);
		if (osmNetworkPath == null || osmOverridesPath == null) {
			throw new IllegalStateException(TRAFFIC_VIEWER_OSM_NETWORK + " and " + TRAFFIC_VIEWER_OSM_OVERRIDES
					+ " must be defined when using empty parameters constructor");
		}
		osmRouting.init(new File(osmNetworkPath), new File(osmOverridesPath));
	}

	@Override
	public void stepActualTimestampChanged(Step previousStep, Step currentStep) {
		if (previousStep != null) {
			EntityManager em = DBUtils.getEntityManager();
			TPGStop start = em.find(TPGStop.class, previousStep.getStopCode());
			TPGStop end = em.find(TPGStop.class, currentStep.getStopCode());

			OSMRoutingResult result = osmRouting.getPath(start.getCoordinate(), end.getCoordinate());
			LineString path = result.getLineString();
			Coordinate startCoordinate = path.getCoordinateN(0);
			Coordinate endCoordinate = path.getCoordinateN(path.getNumPoints() - 1);

			em.getTransaction().begin();

			Shift shift = new Shift();
			shift.setEndLat((float) endCoordinate.y);
			shift.setEndLon((float) endCoordinate.x);
			shift.setStartLat((float) startCoordinate.y);
			shift.setStartLon((float) startCoordinate.x);
			Geometry flatLineString = null;
			try {
				CoordinateReferenceSystem crs4326 = CRS.decode("EPSG:4326");
				CoordinateReferenceSystem crs3857 = CRS.decode("EPSG:3857");
				MathTransform transform = CRS.findMathTransform(crs4326, crs3857);
				flatLineString = JTS.transform(path, transform);
			} catch (FactoryException | MismatchedDimensionException | TransformException e) {
				e.printStackTrace();
				// TODO should never happen and if it does, we should log
			}
			double km = flatLineString.getLength() / 1000;
			double h = (currentStep.getActualTimestamp() - previousStep.getActualTimestamp()) / (1000.0 * 60 * 60);
			shift.setSpeed((int) Math.round(km / h));
			shift.setVehicleId(currentStep.getDepartureCode());
			em.persist(shift);

			String[] wayIds = result.getWayIds();
			for (String wayId : wayIds) {
				OSMShift osmShift = new OSMShift();
				osmShift.setShift(shift);
				osmShift.setOsmId(Long.parseLong(wayId));
				em.persist(osmShift);
			}

			em.getTransaction().commit();
			System.out.println("/t/t NEW SHIFT!!!");
		}
	}

}
