package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

public class Line {
	String lineName;
	String forwardDestination;
	String backwardDestination;
	String[] stopSequence;

	public Line(String lineName, String forwardDestinations, String backwardDestinations, String[] stopSequence) {
		super();
		this.lineName = lineName;
		this.forwardDestination = forwardDestinations;
		this.backwardDestination = backwardDestinations;
		this.stopSequence = stopSequence;
	}

	public static Line read(String resourceName) throws IOException {
		InputStream stream = Line.class.getResourceAsStream(resourceName);
		String content = IOUtils.toString(stream, "utf-8");
		stream.close();
		String[] lines = content.split("\n");
		String name = null;
		String forwardDestination = null;
		String backwardDestination = null;
		ArrayList<String> stopSequence = new ArrayList<>();
		for (String entry : lines) {
			if (entry.startsWith("name:")) {
				name = entry.substring("name:".length()).trim();
			} else if (entry.startsWith("forward:")) {
				forwardDestination = entry.substring("forward:".length()).trim();
			} else if (entry.startsWith("backward:")) {
				backwardDestination = entry.substring("backward:".length()).trim();
			} else {
				stopSequence.add(entry);
			}
		}

		return new Line(name, forwardDestination, backwardDestination,
				stopSequence.toArray(new String[stopSequence.size()]));
	}

}