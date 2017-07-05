package org.fergonco.traffic.analyzer.predict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rscript {
	private static final Logger logger = LogManager.getLogger(Rscript.class);
	private Process process = null;

	public InputStream executeResource(String resourceName, String... parameters) throws IOException {
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
		process = processBuilder.start();
		return process.getInputStream();
	}

	public int getExitCode() {
		if (process == null) {
			throw new IllegalStateException(
					"executeResource must be successfully executed before getExitCode is called");
		}
		while (process.isAlive()) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		}
		return process.exitValue();

	}

}
