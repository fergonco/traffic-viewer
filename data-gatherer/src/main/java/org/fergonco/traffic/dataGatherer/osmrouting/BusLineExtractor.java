package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;

import com.vividsolutions.jts.io.WKTWriter;

public class BusLineExtractor {

	public static void main(String[] args) throws Exception {
		String[] lines = new String[] { "Y", "O", "F" };
		OSMParser osmParser = new OSMParser(new File("ligne-y.osm.xml"), lines);
		osmParser.parse();

		HashSet<String> nodeCodes = new HashSet<>();
		WKTWriter wktWriter = new WKTWriter();
		StringBuilder builder = new StringBuilder();
		builder.append("BEGIN;");
		builder.append("DROP TABLE IF EXISTS osmlines;\n");
		builder.append(
				"CREATE TABLE osmlines (id serial, osm_id bigint, linecode varchar, geom geometry('LINESTRING', 4326));\n");
		builder.append("DROP TABLE IF EXISTS osmlinenodes;\n");
		builder.append("CREATE TABLE osmlinenodes (osm_id bigint primary key, geom geometry('POINT', 4326));\n");
		builder.append("DROP TABLE IF EXISTS osmstops;\n");
		builder.append(
				"CREATE TABLE osmstops (id serial, osm_id bigint, linecode varchar, geom geometry('POINT', 4326));\n");
		for (String line : lines) {
			Iterable<OSMWay> ways = osmParser.getRelation(line).getWays();
			for (OSMWay osmWay : ways) {
				builder.append("INSERT INTO osmlines (osm_id, linecode, geom) VALUES(" + osmWay.getId() + ",'")
						.append(line).append("',ST_GeomFromText('").append(wktWriter.write(osmWay.getLineString()))
						.append("', 4326));\n");
				// Adds all the nodes from the lines
				OSMNode[] lineNodes = osmWay.getNodes();
				for (OSMNode osmNode : lineNodes) {
					if (!nodeCodes.contains(osmNode.getId())) {
						String sql = "INSERT INTO osmlinenodes VALUES ($osmid, ST_GeomFromText('POINT($lon $lat)', 4326));\n";
						sql = sql.replace("$osmid", osmNode.getId())
								.replace("$lon", Double.toString(osmNode.getCoordinate().x))
								.replace("$lat", Double.toString(osmNode.getCoordinate().y));
						builder.append(sql);
						nodeCodes.add(osmNode.getId());
					}
				}
			}
			Iterable<OSMNode> nodes = osmParser.getRelation(line).getNodes();
			for (OSMNode osmNode : nodes) {
				String sql = "INSERT INTO osmstops (osm_id, linecode, geom) VALUES ($osmid, '$line', ST_GeomFromText('POINT($lon $lat)', 4326));\n";
				sql = sql.replace("$osmid", osmNode.getId()).replace("$line", line)
						.replace("$lon", Double.toString(osmNode.getCoordinate().x))
						.replace("$lat", Double.toString(osmNode.getCoordinate().y));
				builder.append(sql);
			}
		}
		builder.append("COMMIT;");

		FileOutputStream output = new FileOutputStream("/tmp/result.sql");
		IOUtils.write(builder.toString(), output, Charset.forName("utf-8"));
		output.close();

	}
}
