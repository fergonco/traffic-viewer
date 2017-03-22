package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.traffic.analyzer.calendar.SchoolCalendar;

import com.google.gson.Gson;

public class DatasetBuilder {

	public void build() throws IOException, ParseException {
		// build calendar data
		InputStream stream = this.getClass().getResourceAsStream("calendar.json");
		String jsonContent = IOUtils.toString(stream, "utf8");
		Gson gson = new Gson();
		SchoolCalendar schoolCalendar = gson.fromJson(jsonContent, SchoolCalendar.class);
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMShift> query = em.createQuery(
				"SELECT s FROM OSMShift s WHERE s.startNode=:startNode and s.endNode=:endNode", OSMShift.class);
		query.setParameter("startNode", 907579280);
		query.setParameter("endNode", 906227763);
		List<OSMShift> osmShifts = query.getResultList();
		System.out.println("speed,timestamp,saturday,sunday,holidayfr,holidaych,schoolfr,schoolch");
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			long timestamp = shift.getTimestamp();
			Date date = new Date(timestamp);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			boolean saturday = dayOfWeek == Calendar.SATURDAY;
			boolean sunday = dayOfWeek == Calendar.SUNDAY;
			boolean holidayFrance = schoolCalendar.isHoliday("france", timestamp);
			boolean holidaySwitzerland = schoolCalendar.isHoliday("switzerland", timestamp);
			boolean schoolFrance = schoolCalendar.isSchool("france", timestamp);
			boolean schoolSwitzerland = schoolCalendar.isSchool("switzerland", timestamp);
			System.out.println(shift.getSpeed() + "," + timestamp + "," + saturday + "," + sunday + "," + holidayFrance
					+ "," + holidaySwitzerland + "," + schoolFrance + "," + schoolSwitzerland);
		}
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build();
	}
}
