package org.fergonco.traffic.analyzer.calendar;

import java.text.ParseException;

public class School {
	private String country;
	private String[][] intervals;
	private long[][] intervalsTimestamps = null;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(country).append(": ");
		for (String[] interval : intervals) {
			ret.append(interval[0]).append(" - ").append(interval[1]).append(", ");
		}
		ret.append("\n");
		return ret.toString();
	}

	public String getCountry() {
		return country;
	}

	public boolean isSchool(long timestamp) {
		long[][] intervalsTimestamps = getIntervalsTimestamps();
		for (long[] intervalTimestamp : intervalsTimestamps) {
			if (intervalTimestamp[0] < timestamp && intervalTimestamp[1] >= timestamp) {
				return true;
			}
		}
		return false;
	}

	private long[][] getIntervalsTimestamps() {
		if (intervalsTimestamps == null) {
			intervalsTimestamps = new long[intervals.length][];
			for (int i = 0; i < intervals.length; i++) {
				try {
					intervalsTimestamps[i] = new long[] { SchoolCalendar.SDF.parse(intervals[i][0]).getTime(),
							SchoolCalendar.SDF.parse(intervals[i][1]).getTime() };
				} catch (ParseException e) {
					throw new RuntimeException("Bad syntax in calendar.json!", e);
				}
			}

		}

		return intervalsTimestamps;
	}
}
