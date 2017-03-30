package org.fergonco.traffic.analyzer;

import org.fergonco.tpg.trafficViewer.jpa.Shift;

public class ShiftFieldSet implements OutputFieldSet {

	@Override
	public String[] getNames() {
		return new String[] { "speed", "timestamp" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) {
		Shift shift = outputContext.getShift();
		return new Object[] { shift.getSpeed(), shift.getTimestamp() };
	}

}
