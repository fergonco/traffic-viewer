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

	private static final long DAYMILLIS = 24 * 60 * 60 * 1000;
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
		return new String[] { "minutesHour", "minutesDay", "weekday", "holidayfr", "holidaych", "schoolfr", "schoolch",
				"holidaySizefr", "holidaySizech" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) {
		long timestamp = outputContext.getShift().getTimestamp();
		Calendar calendar = getCalendar(timestamp);
		int minutesInHour = calendar.get(Calendar.MINUTE);
		int minutesSinceMidnight = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		String dayOfWeek;
		if (minutesSinceMidnight < 120) {
			// last services after midnight moved to previous day
			minutesSinceMidnight += 60 * 24;
			dayOfWeek = previousDay.get(calendar.get(Calendar.DAY_OF_WEEK));
		} else {
			dayOfWeek = dayNames.get(calendar.get(Calendar.DAY_OF_WEEK));
		}

		int holidaySizefr = getHolidaySize(timestamp, "france");
		int holidaySizech = getHolidaySize(timestamp, "switzerland");

		boolean holidayFrance = schoolCalendar.isHoliday("france", timestamp);
		boolean holidaySwitzerland = schoolCalendar.isHoliday("switzerland", timestamp);
		boolean schoolFrance = schoolCalendar.isSchool("france", timestamp);
		boolean schoolSwitzerland = schoolCalendar.isSchool("switzerland", timestamp);

		return new Object[] { minutesInHour, minutesSinceMidnight, dayOfWeek, indicator(holidayFrance),
				indicator(holidaySwitzerland), indicator(schoolFrance), indicator(schoolSwitzerland), holidaySizefr,
				holidaySizech };
	}

	private Calendar getCalendar(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
		return calendar;
	}

	private int getHolidaySize(long timestamp, String country) {
		return getHolidaySize(timestamp + DAYMILLIS, 1, country) + getHolidaySize(timestamp, -1, country);
	}

	private int getHolidaySize(long timestamp, int direction, String country) {
		int workdaysInARow = 0;
		long testDayTimestamp = timestamp;
		int size = 0;
		while (true) {
			if (!schoolCalendar.isHoliday(country, testDayTimestamp)
					&& schoolCalendar.isSchool(country, testDayTimestamp)
					&& getCalendar(testDayTimestamp).get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& getCalendar(testDayTimestamp).get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				if (workdaysInARow == 1) {
					break;
				} else {
					workdaysInARow++;
				}
			} else {
				// Count workdays if they are in the middle
				if (size > 0) {
					size += workdaysInARow;
				}

				size++;
				workdaysInARow = 0;
			}
			testDayTimestamp += direction * DAYMILLIS;
		}
		return size;
	}

	private Object indicator(boolean booleanVariable) {
		return booleanVariable;
		// return booleanVariable ? 1 : 0;
	}

}
