package org.fergonco.traffic.analyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

public class Dataset {

	private PrintStream stream;
	private OutputFieldSet[] outputFieldSets = new OutputFieldSet[] { new IdFieldSet(), new ShiftFieldSet(),
			new CalendarFieldSet(), new WeatherFieldSet() };

	public Dataset(PrintStream stream) {
		this.stream = stream;
	}

	public void writeHeader() {
		ArrayList<Object> outputLine = new ArrayList<>();
		for (OutputFieldSet outputFieldSet : outputFieldSets) {
			Collections.addAll(outputLine, outputFieldSet.getNames());
		}
		stream.println(StringUtils.join(outputLine, ","));
	}

	public void writeEntry(OutputContext outputContext) {
		ArrayList<Object> outputLine = new ArrayList<>();
		for (OutputFieldSet outputFieldSet : outputFieldSets) {
			Collections.addAll(outputLine, outputFieldSet.getValues(outputContext));
		}

		stream.println(StringUtils.join(outputLine, ","));
	}

}
