package org.fergonco.traffic.analyzer.calendar;

import java.text.SimpleDateFormat;

public class SchoolCalendar {
	public static final long DAY_MILLIS = 1000 * 60 * 60 * 24;
	public static SimpleDateFormat SDF = new SimpleDateFormat("d/M/yyyy");

	private Holiday[] holiday;
	private School[] school;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("holidays\n***********\n");
		for (Holiday holiday : holiday) {
			ret.append(holiday.toString()).append("\n");
		}
		ret.append("school\n***********\n");
		for (School school : school) {
			ret.append(school.toString());
		}

		return ret.toString();
	}

	public boolean isHoliday(String country, long timestamp) {
		for (Holiday h : holiday) {
			if (h.getCountry().equals(country)) {
				return h.isHoliday(timestamp);
			}
		}

		throw new IllegalArgumentException("Country not found: " + country);
	}

	public boolean isSchool(String country, long timestamp) {
		for (School h : school) {
			if (h.getCountry().equals(country)) {
				return h.isSchool(timestamp);
			}
		}

		throw new IllegalArgumentException("Country not found: " + country);
	}
}
