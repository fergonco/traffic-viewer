package org.fergonco.traffic.analyzer.calendar;

import org.apache.commons.lang3.StringUtils;

public class Holyday {
	private String country;
	private String[] days;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(country).append(": ").append(StringUtils.join(days, ", "));
		return ret.toString();
	}
}
