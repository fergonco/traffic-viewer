package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.fergonco.traffic.analyzer.calendar.SchoolCalendar;

import com.google.gson.Gson;

public class DatasetBuilder {

	public void build() throws IOException {
		// build calendar data
		InputStream stream = this.getClass().getResourceAsStream("calendar.json");
		String jsonContent = IOUtils.toString(stream, "utf8");
		Gson gson = new Gson();
		SchoolCalendar schoolCalendar = gson.fromJson(jsonContent, SchoolCalendar.class);
		System.out.println(gson.toJson(schoolCalendar));
		// EntityManager em = DBUtils.getEntityManager();
		// TypedQuery<OSMShift> query = em.createQuery(
		// "SELECT s FROM OSMShift s WHERE s.startNode=:startNode and
		// s.endNode=:endNode", OSMShift.class);
		// query.setParameter("startNode", 417189919);
		// query.setParameter("endNode", 31396272);
		// List<OSMShift> osmShifts = query.getResultList();
		// for (OSMShift osmShift : osmShifts) {
		// Shift shift = osmShift.getShift();
		// long timestamp = shift.getTimestamp();
		// Date date = new Date(timestamp);
		// Calendar calendar = Calendar.getInstance();
		// calendar.setTime(date);
		// int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		// boolean saturday = dayOfWeek == Calendar.SATURDAY;
		// boolean sunday = dayOfWeek == Calendar.SUNDAY;
		//
		// System.out.println(shift.getSpeed() + "," + timestamp + "," +
		// saturday + "," + sunday);
		// }
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build();
	}
}
