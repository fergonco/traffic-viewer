package org.fergonco.traffic.analyzer;

public interface OutputFieldSet {

	String[] getNames();

	Object[] getValues(OutputContext outputContext);

}
