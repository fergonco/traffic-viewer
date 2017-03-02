package org.fergonco.utils;

public class InputException extends Exception {

	public InputException(String parameterName) {
		super(parameterName);
	}

	public String getParameterName() {
		return getMessage();
	}

}
