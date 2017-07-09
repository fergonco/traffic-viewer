package org.fergonco.traffic.analyzer.predict;

public class RException extends Exception {

	private static final long serialVersionUID = 1L;

	public RException(String message, Throwable cause) {
		super(message, cause);
	}

	public RException(String message) {
		super(message);
	}

	public RException(Throwable cause) {
		super(cause);
	}

}
