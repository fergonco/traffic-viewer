package org.fergonco.traffic.analyzer.predict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rscript {
	private static final Logger logger = LogManager.getLogger(Rscript.class);

	public String executeResource(String resourceName, String... parameters) throws IOException, RException {
		ArrayList<String> command = new ArrayList<String>();
		command.add("Rscript");
		InputStream scriptStream = this.getClass().getResourceAsStream(resourceName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(scriptStream));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) {
				continue;
			}
			command.add("-e");
			command.add(line);
		}
		for (String parameter : parameters) {
			command.add(parameter);
		}
		logger.debug("Executing: " + StringUtils.join(command.toArray(new String[0]), " "));

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		String output = IOUtils.toString(process.getInputStream(), "utf-8");
		while (process.isAlive()) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		}
		if (process.exitValue() > 0) {
			throw new RException("modeler returned error");
		}
		return output;
	}

}
