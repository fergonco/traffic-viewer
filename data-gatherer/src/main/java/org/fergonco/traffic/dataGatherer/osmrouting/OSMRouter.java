package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class OSMRouter {

	private HashMap<String, OSMLineRouter> lineRouter;

	public OSMRouter(File osmxml, String... lineNames) throws SAXException, IOException, ParserConfigurationException {
		OSMParser osmParser = new OSMParser(osmxml, lineNames);
		osmParser.parse();
		lineRouter = new HashMap<>();
		lineRouter.put("Y", new OSMLineRouter(osmParser, "Y"));
		lineRouter.put("O", new OSMLineRouter(osmParser, "O"));
	}

	public OSMRoutingResult getPathFromNodeOutsideGraph(String lineName, String startNodeId, String endNodeId) {
		OSMLineRouter osmLineRouter = lineRouter.get(lineName);
		if (osmLineRouter == null) {
			throw new IllegalArgumentException("Line not found: " + lineName);
		}

		return osmLineRouter.getPathFromNodeOutsideGraph(startNodeId, endNodeId);
	}
}
