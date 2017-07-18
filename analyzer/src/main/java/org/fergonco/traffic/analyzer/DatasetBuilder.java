package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.TPGStopRoute;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;

public class DatasetBuilder {

	private Map<String, Double> km = null;
	private Map<Long, ArrayList<Long>> routesContainingSegment = null;
	private TreeSet<WeatherConditions> orderedWeatherConditions = null;

	private void build(PrintStream stream, long startNode, long endNode)
			throws IOException, ParseException, NotEnoughShiftsForSegment {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMSegment> query = em.createQuery("SELECT s FROM " + OSMSegment.class.getSimpleName()
				+ " s WHERE s.startNode=:startNode and s.endNode=:endNode", OSMSegment.class);
		query.setParameter("startNode", startNode);
		query.setParameter("endNode", endNode);
		OSMSegment osmSegment = query.getSingleResult();

		build(stream, osmSegment);
	}

	private String getRouteUID(String line, String startCode, String endCode) {
		return line + ":" + startCode + "-" + endCode;
	}

	public void build(PrintStream stream, OSMSegment osmSegment) throws NotEnoughShiftsForSegment {
		EntityManager em = DBUtils.getEntityManager();

		// Build a distance table to calculate speeds
		if (km == null || routesContainingSegment == null) {
			List<TPGStopRoute> tpgStopRoutes = em
					.createQuery("select r from " + TPGStopRoute.class.getSimpleName() + " r", TPGStopRoute.class)
					.getResultList();
			HashMap<String, Double> km = new HashMap<>();
			Map<Long, ArrayList<Long>> routesContainingSegment = new HashMap<>();
			for (TPGStopRoute tpgStopRoute : tpgStopRoutes) {
				String routeUID = getRouteUID(tpgStopRoute.getLine(), tpgStopRoute.getStartTPGCode(),
						tpgStopRoute.getEndTPGCode());
				km.put(routeUID, tpgStopRoute.getDistance());

				List<OSMSegment> segments = tpgStopRoute.getSegments();
				for (OSMSegment segment : segments) {
					ArrayList<Long> routes = routesContainingSegment.get(segment.getId());
					if (routes == null) {
						routes = new ArrayList<>();
						routesContainingSegment.put(segment.getId(), routes);
					}
					routes.add(tpgStopRoute.getId());
				}
			}
			this.km = km;
			this.routesContainingSegment = routesContainingSegment;
		}
		if (orderedWeatherConditions == null) {
			TreeSet<WeatherConditions> orderedWeatherConditions = new TreeSet<>(new Comparator<WeatherConditions>() {

				@Override
				public int compare(WeatherConditions o1, WeatherConditions o2) {
					return (int) (o1.getTimestamp() - o2.getTimestamp());
				}
			});
			List<WeatherConditions> weatherConditions = DBUtils.getAll(em, WeatherConditions.class);
			for (WeatherConditions weather : weatherConditions) {
				orderedWeatherConditions.add(weather);
			}
			this.orderedWeatherConditions = orderedWeatherConditions;
		}

		ArrayList<Long> routes = routesContainingSegment.get(osmSegment.getId());
		String sql = "SELECT shift FROM $shift shift WHERE shift.route.id in :routes".replace("$shift",
				Shift.class.getSimpleName());
		TypedQuery<Shift> query = em.createQuery(sql, Shift.class);
		query.setParameter("routes", routes);
		List<Shift> shifts = query.getResultList();
		em.clear();

		if (shifts.size() <= 2) {
			throw new NotEnoughShiftsForSegment();
		}
		/*
		 * We iterate all the shifts. We keep the latest when there are
		 * duplicates unless any of the duplicates has weird measures.
		 */
		HashMap<String, Shift> idShift = new HashMap<>();
		for (Shift shift : shifts) {
			String routeUID = getRouteUID(shift.getRoute().getLine(), shift.getRoute().getStartTPGCode(),
					shift.getRoute().getEndTPGCode());
			ShiftEntryImpl shiftEntry = new ShiftEntryImpl(shift, km.get(routeUID));
			if (shiftEntry.getSpeed() < 0 || shiftEntry.getSpeed() > 80) {
				// Remove weird measures
				continue;
			}
			String shiftId = shiftEntry.getId();
			Shift duplicatedShift = idShift.get(shiftId);
			if (duplicatedShift == null) {
				idShift.put(shiftId, shift);
			} else {
				if (shift.getTimestamp() > duplicatedShift.getTimestamp()) {
					idShift.put(shiftId, shift);
				}
			}
		}

		Dataset dataset = new Dataset(stream);
		dataset.writeHeader();
		Collection<Shift> cleanShifts = idShift.values();
		for (Shift shift : cleanShifts) {
			WeatherConditions testWeatherConditions = new WeatherConditions();
			testWeatherConditions.setTimestamp(shift.getTimestamp());
			WeatherConditions weatherConditions = orderedWeatherConditions.lower(testWeatherConditions);
			if (weatherConditions == null) {
				weatherConditions = orderedWeatherConditions.first();
			}

			String routeUID = getRouteUID(shift.getRoute().getLine(), shift.getRoute().getStartTPGCode(),
					shift.getRoute().getEndTPGCode());
			ShiftEntryImpl shiftEntry = new ShiftEntryImpl(shift, km.get(routeUID));
			OutputContext outputContext = new OutputContext(shiftEntry, weatherConditions);
			dataset.writeEntry(outputContext);
		}
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build(System.out, 907579280, 906227763);
	}
}
