package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.traffic.dataGatherer.Utils;

import com.vividsolutions.jts.io.WKTWriter;

public class LineRouter {

	public static void main(String[] args) throws Exception {
		ArrayList<Line> lines = new ArrayList<>();
		lines.add(Line.read("line-f.txt"));
		lines.add(Line.read("line-y.txt"));
		lines.add(Line.read("line-o.txt"));

		OSMRouter osmRouter = new OSMRouter(new File("ligne-y.osm.xml"), "F", "Y", "O");
		EntityManager em = DBUtils.getEntityManager();
		WKTWriter wktWriter = new WKTWriter();
		StringBuilder builder = new StringBuilder();
		builder.append("BEGIN;\n");
		builder.append("DROP TABLE IF EXISTS osmlineroutes;\n");
		builder.append(
				"CREATE TABLE osmlineroutes (id SERIAL, direction varchar, line varchar, geom geometry('LINESTRING', 4326));\n");
		for (Line line : lines) {
			String[] stopSequence = line.stopSequence;
			for (int i = 0; i < stopSequence.length - 1; i++) {
				// forward route
				TPGStop start = Utils.getTPGStop(em, stopSequence[i], line.lineName, line.forwardDestination);
				TPGStop end = Utils.getTPGStop(em, stopSequence[i + 1], line.lineName, line.forwardDestination);
				String startNodeId = start.getNodeId();
				String endNodeId = end.getNodeId();
				OSMRoutingResult result = osmRouter.getPathFromNodeOutsideGraph(line.lineName, startNodeId, endNodeId);
				if (result != null) {
					String linestringWKT = wktWriter.write(result.getLineString());
					String sql = "INSERT INTO osmlineroutes (direction, line, geom) VALUES('$direction', '$line', ST_GeomFromText('$wkt', 4326));";
					sql = sql.replace("$wkt", linestringWKT);
					sql = sql.replace("$direction", "forward");
					sql = sql.replace("$line", line.lineName);
					builder.append(sql);
				} else {
					throw new RuntimeException(stopSequence[i] + " - " + stopSequence[i + 1] + " forward");
				}

				// backward
				start = Utils.getTPGStop(em, stopSequence[i + 1], line.lineName, line.backwardDestination);
				end = Utils.getTPGStop(em, stopSequence[i], line.lineName, line.backwardDestination);
				startNodeId = start.getNodeId();
				endNodeId = end.getNodeId();
				result = osmRouter.getPathFromNodeOutsideGraph(line.lineName, startNodeId, endNodeId);
				if (result != null) {
					String linestringWKT = wktWriter.write(result.getLineString());
					String sql = "INSERT INTO osmlineroutes (direction, geom) VALUES('$direction', ST_GeomFromText('$wkt', 4326));";
					sql = sql.replace("$wkt", linestringWKT);
					sql = sql.replace("$direction", "backward");
					builder.append(sql);
				} else {
					throw new RuntimeException(stopSequence[i] + " - " + stopSequence[i + 1] + " backward");
				}
			}
		}
		builder.append("COMMIT;\n");
		FileOutputStream output = new FileOutputStream("/tmp/osmlineroutes.sql");
		IOUtils.write(builder.toString(), output, Charset.forName("utf-8"));
		output.close();

		System.out.println("Results in /tmp/osmlineroutes.sql");
	}

}
