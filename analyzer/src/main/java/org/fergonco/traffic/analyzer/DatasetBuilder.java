package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.tpg.trafficViewer.jpa.WeatherConditions;
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
		System.out.println(
				"speed,timestamp,minutes,saturday,sunday,holidayfr,holidaych,schoolfr,schoolch,humidity,pressure,rain3h,snow3h,temperature,weather");
		for (OSMShift osmShift : osmShifts) {
			Shift shift = osmShift.getShift();
			long timestamp = shift.getTimestamp();
			Date date = new Date(timestamp);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.setTimeZone(TimeZone.getTimeZone("GMT+1"));
			int minutesSinceMidnight = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			boolean saturday = dayOfWeek == Calendar.SATURDAY;
			boolean sunday = dayOfWeek == Calendar.SUNDAY;
			boolean holidayFrance = schoolCalendar.isHoliday("france", timestamp);
			boolean holidaySwitzerland = schoolCalendar.isHoliday("switzerland", timestamp);
			boolean schoolFrance = schoolCalendar.isSchool("france", timestamp);
			boolean schoolSwitzerland = schoolCalendar.isSchool("switzerland", timestamp);
			String humidity = "";
			String pressure = "";
			String rain3h = "";
			String snow3h = "";
			String temperature = "";
			String weather = "";

			try {
				Query weatherConditionsQuery = em
						.createNativeQuery("select * from app.WeatherConditions w " + "where w.timestamp=("
								+ "select max(w2.timestamp) from app.WeatherConditions w2 where w2.timestamp < "
								+ timestamp + ");", WeatherConditions.class);
				WeatherConditions weatherConditions = (WeatherConditions) weatherConditionsQuery.getSingleResult();
				humidity = weatherConditions.getHumidity() + "";
				pressure = weatherConditions.getPressure() + "";
				rain3h = (weatherConditions.getRain3h() != null) ? weatherConditions.getRain3h() + "" : "";
				snow3h = (weatherConditions.getSnow3h() != null) ? weatherConditions.getSnow3h() + "" : "";
				temperature = weatherConditions.getTemperature() + "";
				weather = weatherConditions.getWeather() + "";
			} catch (NoResultException e) {
				System.out.println("No result");
			}

			System.out.println(shift.getSpeed() + "," + timestamp + "," + minutesSinceMidnight + "," + saturday + ","
					+ sunday + "," + holidayFrance + "," + holidaySwitzerland + "," + schoolFrance + ","
					+ schoolSwitzerland + "," + humidity + "," + pressure + "," + rain3h + "," + snow3h + ","
					+ temperature + "," + weather);
		}
	}

	public static void main(String[] args) throws Exception {
		new DatasetBuilder().build();
	}
}
