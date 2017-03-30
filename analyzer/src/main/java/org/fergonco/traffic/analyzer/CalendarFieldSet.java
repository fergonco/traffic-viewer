package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.fergonco.traffic.analyzer.calendar.SchoolCalendar;

import com.google.gson.Gson;

public class CalendarFieldSet implements OutputFieldSet {

	private Map<Integer, String> dayNames;
	private SchoolCalendar schoolCalendar;

	public CalendarFieldSet() throws IOException {
		// build calendar data
		InputStream stream = this.getClass().getResourceAsStream("calendar.json");
		String jsonContent = IOUtils.toString(stream, "utf8");
		Gson gson = new Gson();
		schoolCalendar = gson.fromJson(jsonContent, SchoolCalendar.class);

		dayNames = new HashMap<>();
		dayNames.put(Calendar.MONDAY, "monday");
		dayNames.put(Calendar.TUESDAY, "tuesday");
		dayNames.put(Calendar.WEDNESDAY, "wednesday");
		dayNames.put(Calendar.THURSDAY, "thursday");
		dayNames.put(Calendar.FRIDAY, "friday");
		dayNames.put(Calendar.SATURDAY, "saturday");
		dayNames.put(Calendar.SUNDAY, "sunday");
	}

	@Override
	public String[] getNames() {
		return new String[] { "minutes", "weekday", "holidayfr", "holidaych", "schoolfr", "schoolch" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) throws ParseException {
		long timestamp = outputContext.getShift().getTimestamp();
		Date date = new Date(timestamp);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		int minutesSinceMidnight = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		String dayOfWeek = dayNames.get(calendar.get(Calendar.DAY_OF_WEEK));
		boolean holidayFrance = schoolCalendar.isHoliday("france", timestamp);
		boolean holidaySwitzerland = schoolCalendar.isHoliday("switzerland", timestamp);
		boolean schoolFrance = schoolCalendar.isSchool("france", timestamp);
		boolean schoolSwitzerland = schoolCalendar.isSchool("switzerland", timestamp);

		return new Object[] { minutesSinceMidnight, dayOfWeek, holidayFrance, holidaySwitzerland, schoolFrance,
				schoolSwitzerland };
	}

}
