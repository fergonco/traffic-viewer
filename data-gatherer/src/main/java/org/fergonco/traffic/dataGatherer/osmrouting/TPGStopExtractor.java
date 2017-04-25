package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import co.geomati.tpg.Stop;
import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.TPGCachedParser;

public class TPGStopExtractor {

	public static void main(String[] args) throws IOException, SAXException, ParseException {
		TPGCachedParser tpg = new TPGCachedParser(new TPG());
		StringBuilder builder = new StringBuilder();
		builder.append("DROP TABLE tpgstops;\n");
		builder.append("CREATE TABLE tpgstops (code varchar primary key, geom geometry('POINT', 4326));\n");
		String[] lines = new String[] { "O", "Y", "F" };
		for (String line : lines) {
			Stop[] stops = tpg.getStops(line);
			for (Stop stop : stops) {
				String sql = "INSERT INTO tpgstops VALUES('$code', ST_GeomFromText('POINT($lon $lat)', 4326));\n";
				sql = sql.replace("$code", stop.getCode()).replace("$lon", Double.toString(stop.getLon()))
						.replace("$lat", Double.toString(stop.getLat()));
				builder.append(sql);
			}
		}

		FileOutputStream output = new FileOutputStream("/tmp/stops.sql");
		IOUtils.write(builder.toString(), output, Charset.forName("utf-8"));
		output.close();
	}
}
