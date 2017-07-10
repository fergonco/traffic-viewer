package org.fergonco.traffic.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.fergonco.traffic.analyzer.calendar.SchoolCalendar;

import com.google.gson.Gson;

public class CalendarFieldSet implements OutputFieldSet {

	private Map<Integer, String> dayNames;
	private Map<Integer, String> previousDay;
	private SchoolCalendar schoolCalendar;

	public CalendarFieldSet() {
		// build calendar data
		InputStream stream = this.getClass().getResourceAsStream("calendar.json");
		String jsonContent;
		try {
			jsonContent = IOUtils.toString(stream, "utf8");
		} catch (IOException e) {
			throw new RuntimeException("Should not happen", e);
		}
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

		previousDay = new HashMap<>();
		previousDay.put(Calendar.MONDAY, "sunday");
		previousDay.put(Calendar.TUESDAY, "monday");
		previousDay.put(Calendar.WEDNESDAY, "tuesday");
		previousDay.put(Calendar.THURSDAY, "wednesday");
		previousDay.put(Calendar.FRIDAY, "thursday");
		previousDay.put(Calendar.SATURDAY, "friday");
		previousDay.put(Calendar.SUNDAY, "saturday");
	}

	@Override
	public String[] getNames() {
		return new String[] { "minutesHour", "minutesDay", "distortedMinutes", "morningrush", "morningfall",
				"morningrise", "remainingday", "weekday", "holidayfr", "holidaych", "schoolfr", "schoolch" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) {
		long timestamp = outputContext.getShift().getTimestamp();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		int minutesInHour = calendar.get(Calendar.MINUTE);
		int minutesSinceMidnight = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		double distortedMinutes = Math.sqrt(Math.abs(450 - minutesSinceMidnight));
		String dayOfWeek;
		if (minutesSinceMidnight < 120) {
			// last services after midnight moved to previous day
			minutesSinceMidnight += 60 * 24;
			dayOfWeek = previousDay.get(calendar.get(Calendar.DAY_OF_WEEK));
		} else {
			dayOfWeek = dayNames.get(calendar.get(Calendar.DAY_OF_WEEK));
		}
		boolean morningrush = minutesSinceMidnight > 400 && minutesSinceMidnight < 550;

		// morning fall
		String morningFall = "NA";
		if (minutesSinceMidnight < 450) {
			morningFall = Integer.toString(450 - minutesSinceMidnight);
		}

		// morning rise
		String morningRise = "NA";
		if (minutesSinceMidnight >= 450 && minutesSinceMidnight < 570) {
			morningRise = Integer.toString(minutesSinceMidnight - 450);
		}

		// remainingDay
		String remainingDay = "NA";
		if (minutesSinceMidnight >= 570) {
			remainingDay = Integer.toString(minutesSinceMidnight - 570);
		}

		boolean holidayFrance = schoolCalendar.isHoliday("france", timestamp);
		boolean holidaySwitzerland = schoolCalendar.isHoliday("switzerland", timestamp);
		boolean schoolFrance = schoolCalendar.isSchool("france", timestamp);
		boolean schoolSwitzerland = schoolCalendar.isSchool("switzerland", timestamp);

		return new Object[] { minutesInHour, minutesSinceMidnight, distortedMinutes, indicator(morningrush),
				morningFall, morningRise, remainingDay, dayOfWeek, indicator(holidayFrance),
				indicator(holidaySwitzerland), indicator(schoolFrance), indicator(schoolSwitzerland) };
	}

	private Object indicator(boolean booleanVariable) {
		return booleanVariable;
		// return booleanVariable ? 1 : 0;
	}

}
