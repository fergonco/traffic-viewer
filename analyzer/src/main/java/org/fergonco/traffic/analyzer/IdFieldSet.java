package org.fergonco.traffic.analyzer;

import org.fergonco.traffic.analyzer.OutputContext.ShiftEntry;

public class IdFieldSet implements OutputFieldSet {

	@Override
	public String[] getNames() {
		return new String[] { "uid" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) {
		ShiftEntry shift = outputContext.getShift();
		return new Object[] { shift.getId() };
	}

}
