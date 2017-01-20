package org.fergonco.tpg.trafficViewer.osmrouting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTWriter;

public class OSMRouting {

	private HashMap<String, OSMNode> idNodes = new HashMap<>();
	private HashMap<String, OSMWay> idWays = new HashMap<>();
	public OSMRelation relation = null;
	private DefaultDirectedGraph<OSMNode, OSMStep> graph;

	public void init(File osmxml, File overrides) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(osmxml));
		saxParser.parse(is, new SaxHandler());
		is.close();
		is = new BufferedInputStream(new FileInputStream(overrides));
		saxParser.parse(is, new OverridingSaxHandler());
		is.close();

		graph = new DefaultDirectedGraph<OSMNode, OSMStep>(OSMStep.class);
		for (OSMWay osmWay : relation.getWays()) {
			OSMNode[] wayNodes = osmWay.getNodes();
			for (int i = 0; i < wayNodes.length - 1; i++) {
				OSMNode currentNode = wayNodes[i];
				OSMNode nextNode = wayNodes[i + 1];
				graph.addVertex(currentNode);
				graph.addVertex(nextNode);
				graph.addEdge(currentNode, nextNode, new OSMStep(osmWay));
				if (!"yes".equals(osmWay.getTag("oneway")) && !"roundabout".equals(osmWay.getTag("junction"))) {
					graph.addEdge(nextNode, currentNode, new OSMStep(osmWay));
				}
			}
		}

	}

	public OSMRoutingResult getPath(Coordinate start, Coordinate end) {
		/*
		 * We get the way first because there may be a case where the closest
		 * node does not belong to the closest way, and we want the later.
		 */
		OSMWay startWay = relation.getClosestWay(start);
		OSMWay endWay = relation.getClosestWay(end);
		OSMNode startNode = startWay.getClosestNode(start);
		OSMNode endNode = endWay.getClosestNode(end);
		return getPath(startNode.getId(), endNode.getId());
	}

	public OSMRoutingResult getPath(String startNodeId, String endNodeId) {
		OSMNode a = idNodes.get(startNodeId);
		OSMNode b = idNodes.get(endNodeId);
		GraphPath<OSMNode, OSMStep> result = DijkstraShortestPath.findPathBetween(graph, a, b);
		return new OSMRoutingResult(result);
	}

	public Iterable<OSMWay> getWays() {
		return relation.getWays();
	}

	public static void main(String[] args) throws Exception {
		OSMRouting osmRouting = new OSMRouting();
		osmRouting.init(new File("ligne-y.osm.xml"), new File("osm_overrides.xml"));
		OSMRoutingResult result = osmRouting.getPath(new Coordinate(5.979067, 46.232062),
				new Coordinate(6.1081094, 46.2555700));

		// Ferney-Mairie
		// new Coordinate(6.1081094,46.2555700));

		// ICC Does not find the stop in his side
		// new Coordinate(6.101175, 46.226193));

		// Renfile. Se ha suprimido un oneway=true
		// new Coordinate(6.095520, 46.219171));

		// Last working connection from Val-Thoiry
		// new Coordinate(6.0847346, 46.2198077));

		// Meyrin-gare, Does not find the stop in his side.
		// Warning: to reproduce it must be the end stop!
		// new Coordinate(6.0769624, 46.2217390));

		// OSMRoutingResult result = osmRouting.getPath("266377188",
		// "3058194700");
		WKTWriter wktWriter = new WKTWriter();
		StringBuilder builder = new StringBuilder();
		Iterable<OSMWay> ways = osmRouting.getWays();
		builder.append(
				"create table if not exists test_line (osm_id bigint primary key, geom geometry('LINESTRING', 4326));\n");
		builder.append("delete from test_line;\n");
		for (OSMWay osmWay : ways) {
			builder.append("insert into test_line values(" + osmWay.getId() + ",ST_GeomFromText('")
					.append(wktWriter.write(osmWay.getLineString())).append("', 4326));");
		}

		builder.append(
				"create table if not exists test_shortestpath (id serial primary key, geom geometry('LINESTRING', 4326));\n");
		builder.append("delete from test_shortestpath ;\n");
		if (result.pathFound()) {
			System.out.println("path found!");
			String linestringWKT = wktWriter.write(result.getLineString());
			String[] wayIds = result.getWayIds();
			builder.append("insert into test_shortestpath (geom) values(ST_GeomFromText('").append(linestringWKT)
					.append("', 4326));");
			builder.append("create or replace view test_affectedways as select * from osm_line where osm_id in (")
					.append(StringUtils.join(wayIds, ",")).append(");\n");
		}
		FileOutputStream output = new FileOutputStream("/tmp/result.sql");
		IOUtils.write(builder.toString(), output);
		output.close();
	}

	private class SaxHandler extends DefaultHandler {

		private OSMWay currentWay = null;
		private OSMRelation currentRelation = null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("node")) {
				Coordinate coordinate = new Coordinate(Double.parseDouble(attributes.getValue("lon")),
						Double.parseDouble(attributes.getValue("lat")));
				String nodeId = attributes.getValue("id");
				OSMNode osmNode = new OSMNode(nodeId, coordinate);
				idNodes.put(nodeId, osmNode);
			} else if (qName.equals("way")) {
				String wayId = attributes.getValue("id");
				currentWay = new OSMWay(wayId);
				idWays.put(wayId, currentWay);
			} else if (qName.equals("nd")) {
				String ref = attributes.getValue("ref");
				OSMNode osmNode = idNodes.get(ref);
				osmNode.addWay(currentWay);
				currentWay.addNode(osmNode);
			} else if (qName.equals("relation")) {
				currentRelation = new OSMRelation();
			} else if (qName.equals("member")) {
				String ref = attributes.getValue("ref");
				OSMWay way = idWays.get(ref);
				if (way != null) {
					currentRelation.addWay(way);
				}
			} else if (currentWay != null && qName.equals("tag")) {
				currentWay.setTag(attributes.getValue("k"), attributes.getValue("v"));
			} else if (currentRelation != null && qName.equals("tag")) {
				currentRelation.setTag(attributes.getValue("k"), attributes.getValue("v"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("way")) {
				currentWay = null;
			} else if (qName.equals("relation")) {
				if ("Y".equals(currentRelation.getTag("ref"))) {
					OSMRouting.this.relation = currentRelation;
				}
				currentRelation = null;
			}
		}
	}

	private class OverridingSaxHandler extends DefaultHandler {

		private OSMWay currentWay = null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("way")) {
				String wayId = attributes.getValue("id");
				currentWay = idWays.get(wayId);
			} else if (currentWay != null && qName.equals("tag")) {
				currentWay.setTag(attributes.getValue("k"), attributes.getValue("v"));
			} else if (qName.equals("delete-way")) {
				String id = attributes.getValue("id");
				OSMRouting.this.relation.removeWay(idWays.remove(id));
			} else if (qName.equals("add-way")) {
				String id = attributes.getValue("id");
				OSMRouting.this.relation.addWay(idWays.get(id));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("way")) {
				currentWay = null;
			}
		}
	}
}