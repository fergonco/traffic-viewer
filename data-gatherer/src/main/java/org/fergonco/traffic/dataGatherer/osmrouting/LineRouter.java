package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop2;

import com.vividsolutions.jts.io.WKTWriter;

public class LineRouter {

	public static class Line {
		private String lineName;
		private String forwardDestination;
		private String backwardDestination;
		private String[] stopSequence;

		public Line(String lineName, String forwardDestinations, String backwardDestinations, String[] stopSequence) {
			super();
			this.lineName = lineName;
			this.forwardDestination = forwardDestinations;
			this.backwardDestination = backwardDestinations;
			this.stopSequence = stopSequence;
		}

	}

	public static void main(String[] args) throws Exception {
		ArrayList<Line> lines = new ArrayList<>();
		lines.add(new Line("Y", "FERNEY-VOLTAIRE", "VAL-THOIRY",
				new String[] { //
						"VATH", "THGA", "THMA", "PAGN", "POLT", "SGBO", //
						"SGGA", "JMON", //
						"CFUS", "HTAI", "PLIO", "MLVT", "SHUM", "CERN", "MAIX", "HTOU", "MAIL", "VROT", "PFON", "LMLD",
						"ZIMG", "PRGM", "SIGN", "RENF", "BLDO", "GDHA", "ICC0", "TOCO", "WTC0", "AERO", "AREN", "PXPH",
						"FRET", "TRTI", "GSDN", "FVDO", "BRUN", "JAGI", "AJUR", "FEMA"//
				}));
		// lines.add(new Line("O", null, null, new String[] { "GRVI", "MATG",
		// "CAND", "PAAN", "ADOU", "BRET", "AGLA",
		// "CLJO", "PREV", "HAMA", "CHTE", "PBRU", "FEMA", "AJUR", "COLX",
		// "LYIN" }));

		OSMRouting osmRouting = new OSMRouting();
		osmRouting.init(new File("ligne-y.osm.xml"), "Y", "O");
		EntityManager em = DBUtils.getEntityManager();
		WKTWriter wktWriter = new WKTWriter();
		StringBuilder builder = new StringBuilder();
		builder.append("BEGIN;\n");
		builder.append("DROP TABLE IF EXISTS osmlineroutes;\n");
		builder.append(
				"CREATE TABLE osmlineroutes (id SERIAL, direction varchar, geom geometry('LINESTRING', 4326));\n");
		for (Line line : lines) {
			String[] stopSequence = line.stopSequence;
			for (int i = 0; i < stopSequence.length - 1; i++) {
				TPGStop2 start = getTPGStop(em, stopSequence[i], line.lineName, line.forwardDestination);
				TPGStop2 end = getTPGStop(em, stopSequence[i + 1], line.lineName, line.forwardDestination);
				String startNodeId = start.getNodeId();
				String endNodeId = end.getNodeId();
				OSMRoutingResult result = osmRouting.getPathFromNodeOutsideGraph(startNodeId, endNodeId);
				if (result != null) {
					String linestringWKT = wktWriter.write(result.getLineString());
					String sql = "INSERT INTO osmlineroutes (direction, geom) VALUES('$direction', ST_GeomFromText('$wkt', 4326));";
					sql = sql.replace("$wkt", linestringWKT);
					sql = sql.replace("$direction", "forward");
					builder.append(sql);
				} else {
					throw new RuntimeException(stopSequence[i] + " forward");
				}
				start = getTPGStop(em, stopSequence[i + 1], line.lineName, line.backwardDestination);
				end = getTPGStop(em, stopSequence[i], line.lineName, line.backwardDestination);
				startNodeId = start.getNodeId();
				endNodeId = end.getNodeId();
				result = osmRouting.getPathFromNodeOutsideGraph(startNodeId, endNodeId);
				if (result != null) {
					String linestringWKT = wktWriter.write(result.getLineString());
					String sql = "INSERT INTO osmlineroutes (direction, geom) VALUES('$direction', ST_GeomFromText('$wkt', 4326));";
					sql = sql.replace("$wkt", linestringWKT);
					sql = sql.replace("$direction", "backward");
					builder.append(sql);
				} else {
					throw new RuntimeException(stopSequence[i] + " backward");
				}
			}
		}
		builder.append("COMMIT;\n");
		FileOutputStream output = new FileOutputStream("/tmp/osmlineroutes.sql");
		IOUtils.write(builder.toString(), output, Charset.forName("utf-8"));
		output.close();
	}

	private static TPGStop2 getTPGStop(EntityManager em, String tpgCode, String line, String destination) {
		TypedQuery<TPGStop2> query = em.createQuery(
				"SELECT s FROM TPGStop2 s WHERE s.tpgCode=:tpgCode AND s.line=:line AND s.destination=:destination",
				TPGStop2.class);
		query.setParameter("tpgCode", tpgCode);
		query.setParameter("line", line);
		query.setParameter("destination", destination);
		return query.getSingleResult();
	}
}
