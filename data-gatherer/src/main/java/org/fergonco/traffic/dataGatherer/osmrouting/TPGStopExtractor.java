package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.IOException;
import java.text.ParseException;

import org.xml.sax.SAXException;

import co.geomati.tpg.Stop;
import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.TPGCachedParser;

public class TPGStopExtractor {

	public static void main(String[] args) throws IOException, SAXException, ParseException {
		TPGCachedParser tpg = new TPGCachedParser(new TPG());
		Stop[] stops = tpg.getStops("O");
		for (Stop stop : stops) {
			System.out.println(stop.getCode() + " (" + stop.getLat() + ", " + stop.getLon() + ")");
		}
	}
}
