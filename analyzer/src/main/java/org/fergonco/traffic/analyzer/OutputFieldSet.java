package org.fergonco.traffic.analyzer;

import java.text.ParseException;

public interface OutputFieldSet {

	String[] getNames();

	Object[] getValues(OutputContext outputContext) throws ParseException;

}
