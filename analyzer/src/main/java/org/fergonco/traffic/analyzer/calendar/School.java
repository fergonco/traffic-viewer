package org.fergonco.traffic.analyzer.calendar;

public class School {
	private String country;
	private String[][] intervals;

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
}
