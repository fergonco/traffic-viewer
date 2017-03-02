package org.fergonco.utils;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtils {

	private HttpServlet servlet;
	private HttpServletRequest req;
	private HttpServletResponse resp;

	public ServletUtils(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
		this.servlet = servlet;
		this.req = req;
		this.resp = resp;
	}

	public long getMandatoryLong(String parameterName)
			throws MissingMandatoryParameterException, ParameterConversionException {
		String parameterValue = req.getParameter(parameterName);
		if (parameterValue != null) {
			try {
				return Long.parseLong(parameterValue);
			} catch (NumberFormatException e) {
				throw new ParameterConversionException(parameterName);
			}
		} else {
			throw new MissingMandatoryParameterException(parameterName);
		}
	}

	public void send(String contentType, String content) throws IOException {
		resp.setContentType(contentType);
		resp.getWriter().write(content);
	}

}
