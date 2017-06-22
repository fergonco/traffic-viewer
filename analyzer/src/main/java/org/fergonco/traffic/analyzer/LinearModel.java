package org.fergonco.traffic.analyzer;

import java.util.HashMap;
import java.util.Set;

public class LinearModel {

	private HashMap<String, Double> variableCoefficient = new HashMap<>();

	public void put(String variable, double coefficient) {
		variableCoefficient.put(variable, coefficient);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Set<String> variableNames = variableCoefficient.keySet();
		for (String variableName : variableNames) {
			builder.append(variableName).append(": ").append(variableCoefficient.get(variableName)).append("\n");
		}

		return builder.toString();
	}
}
