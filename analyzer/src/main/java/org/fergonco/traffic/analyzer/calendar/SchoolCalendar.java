package org.fergonco.traffic.analyzer.calendar;

public class SchoolCalendar {
	private Holyday[] holiday;
	private School[] school;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("holidays\n***********\n");
		for (Holyday holyday : holiday) {
			ret.append(holyday.toString()).append("\n");
		}
		ret.append("school\n***********\n");
		for (School school : school) {
			ret.append(school.toString());
		}

		return ret.toString();
	}
}
