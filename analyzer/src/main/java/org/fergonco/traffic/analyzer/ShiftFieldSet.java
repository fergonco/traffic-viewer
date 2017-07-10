package org.fergonco.traffic.analyzer;

import org.fergonco.traffic.analyzer.OutputContext.ShiftEntry;

public class ShiftFieldSet implements OutputFieldSet {

	@Override
	public String[] getNames() {
		return new String[] { "speed", "timestamp" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) {
		ShiftEntry shift = outputContext.getShift();
		return new Object[] { shift.getSpeed(), shift.getTimestamp() };
	}

}
