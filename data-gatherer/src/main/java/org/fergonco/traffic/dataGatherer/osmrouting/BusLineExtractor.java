package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.vividsolutions.jts.io.WKTWriter;

public class BusLineExtractor {

	public static void main(String[] args) throws Exception {
		String[] lines = new String[] { "Y", "O", "F" };
		OSMParser osmParser = new OSMParser(new File("ligne-y.osm.xml"), lines);
		osmParser.parse();

		WKTWriter wktWriter = new WKTWriter();
		StringBuilder builder = new StringBuilder();
		builder.append(
				"create table if not exists test_line (osm_id bigint primary key, linecode varchar, geom geometry('LINESTRING', 4326));\n");
		builder.append("delete from test_line;\n");
		for (String line : lines) {
			Iterable<OSMWay> ways = osmParser.getRelation(line).getWays();
			for (OSMWay osmWay : ways) {
				builder.append("insert into test_line values(" + osmWay.getId() + ",'").append(line)
						.append("',ST_GeomFromText('").append(wktWriter.write(osmWay.getLineString()))
						.append("', 4326));\n");
			}
		}
		// Iterable<OSMNode> nodes = osmRouting.getNodes();
		// builder.append(
		// "create table if not exists test_stops (osm_id bigint primary key,
		// geom geometry('POINT', 4326));\n");
		// builder.append("delete from test_stops;\n");
		// for (OSMNode osmNode : nodes) {
		// builder.append("insert into test_stops values(" + osmNode.getId() +
		// ",ST_GeomFromText('")
		// .append(wktWriter.write(OSMUtils.buildPoint(osmNode.getCoordinate()))).append("',
		// 4326));");
		// }
		//
		// builder.append(
		// "create table if not exists test_shortestpath (id serial primary key,
		// stop1 varchar, stop2 varchar, geom geometry('LINESTRING',
		// 4326));\n");
		// builder.append("delete from test_shortestpath ;\n");
		//
		// String[] stops = new String[] { //
		// "VATH", "THGA", "THMA", "PAGN", "POLT", "SGBO", //
		// "SGGA", "JMON", //
		// "CFUS", "HTAI", "PLIO", "MLVT", "SHUM", "CERN", "MAIX", "HTOU",
		// "MAIL", "VROT", "PFON", "LMLD", "ZIMG",
		// "PRGM", "SIGN", "RENF", "BLDO", "GDHA", "ICC0", "TOCO", "WTC0",
		// "AERO", "AREN", "PXPH", "FRET", "TRTI",
		// "GSDN", "FVDO", "BRUN", "JAGI", "AJUR", "FEMA"//
		// };
		//
		// // String destination = "FERNEY-VOLTAIRE";
		// String destination = "VAL-THOIRY";
		// List<String> list = Arrays.asList(stops);
		// Collections.reverse(list);
		// stops = (String[]) list.toArray();
		//
		// for (int i = 0; i < stops.length - 1; i++) {
		// TPGStop start = em.find(TPGStop.class, stops[i]);
		// TPGStop end = em.find(TPGStop.class, stops[i + 1]);
		// String startNodeId = start.getNodeId(destination);
		// String endNodeId = end.getNodeId(destination);
		// String geomSQL = "null";
		// try {
		// OSMRoutingResult result =
		// osmRouting.getPathFromNodeOutsideGraph(startNodeId, endNodeId);
		// if (result != null) {
		// String linestringWKT = wktWriter.write(result.getLineString());
		// geomSQL = "ST_GeomFromText('" + linestringWKT + "', 4326)";
		// }
		// } catch (RuntimeException e) {
		// e.printStackTrace();
		// }
		// builder.append("insert into test_shortestpath (stop1, stop2, geom)
		// values('" + stops[i] + "', '"
		// + stops[i + 1] + "', " + geomSQL + ");");
		// // OSMWayIdAndSense[] wayIdsAndSense =
		// // result.getWayIdsAndSenses();
		// // builder.append("create or replace view test_affectedways as
		// // select * from osm_line where osm_id in (")
		// // .append(StringUtils.join(wayIds, ",")).append(");\n");
		// }
		FileOutputStream output = new FileOutputStream("/tmp/result.sql");
		IOUtils.write(builder.toString(), output, Charset.forName("utf-8"));
		output.close();

	}
}
