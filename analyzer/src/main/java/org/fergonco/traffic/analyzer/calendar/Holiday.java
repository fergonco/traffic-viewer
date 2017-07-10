package org.fergonco.traffic.analyzer.calendar;

import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

public class Holiday {
	private String country;
	private String[] days;
	private long[] holidaysTimestamps = null;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(country).append(": ").append(StringUtils.join(days, ", "));
		return ret.toString();
	}

	public String getCountry() {
		return country;
	}

	public boolean isHoliday(long timestamp) {
		long[] holidaysTimestamps = getHolidaysTimestamps();
		for (long holidayTimestamp : holidaysTimestamps) {
			if (holidayTimestamp < timestamp && holidayTimestamp + SchoolCalendar.DAY_MILLIS > timestamp) {
				return true;
			}
		}
		return false;
	}

	private long[] getHolidaysTimestamps() {
		if (holidaysTimestamps == null) {
			holidaysTimestamps = new long[days.length];
			for (int i = 0; i < days.length; i++) {
				try {
					holidaysTimestamps[i] = SchoolCalendar.SDF.parse(days[i]).getTime();
				} catch (ParseException e) {
					throw new RuntimeException("Bad syntax in calendar.json!", e);
				}
			}
		}

		return holidaysTimestamps;
	}
}
