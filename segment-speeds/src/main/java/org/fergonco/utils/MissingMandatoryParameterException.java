package org.fergonco.utils;

public class MissingMandatoryParameterException extends InputException {

	public MissingMandatoryParameterException(String parameterName) {
		super(parameterName);
	}

}
