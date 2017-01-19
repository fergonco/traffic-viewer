package org.fergonco.tpg.trafficViewer.osmrouting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMRouting {

	private HashMap<String, OSMNode> idNodes = new HashMap<>();
	private HashMap<String, OSMWay> idWays = new HashMap<>();
	public ArrayList<OSMWay> relation = null;
	private DefaultDirectedGraph<OSMNode, OSMStep> graph;

	public void init(File osmxml) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(osmxml));
		saxParser.parse(is, new SaxHandler());
		is.close();

		graph = new DefaultDirectedGraph<OSMNode, OSMStep>(OSMStep.class);
		for (OSMWay osmWay : relation) {
			System.out.println(osmWay.getTag("junction"));
			OSMNode[] wayNodes = osmWay.getNodes();
			for (int i = 0; i < wayNodes.length - 1; i++) {
				OSMNode currentNode = wayNodes[i];
				OSMNode nextNode = wayNodes[i + 1];
				graph.addVertex(currentNode);
				graph.addVertex(nextNode);
				graph.addEdge(nextNode, currentNode, new OSMStep(osmWay, nextNode, currentNode));
				// if oneway=yes or juntion=roundabout
				if (!"yes".equals(osmWay.getTag("oneway")) && !"roundabout".equals(osmWay.getTag("junction"))) {
					graph.addEdge(currentNode, nextNode, new OSMStep(osmWay, currentNode, nextNode));
				}
			}
		}

	}

	public List<OSMNode> getPath(String startNodeId, String endNodeId) {
		OSMNode a = idNodes.get(startNodeId);
		OSMNode b = idNodes.get(endNodeId);
		GraphPath<OSMNode, OSMStep> result = DijkstraShortestPath.findPathBetween(graph, a, b);
		return result.getVertexList();
	}

	public static void main(String[] args) throws Exception {
		OSMRouting osmRouting = new OSMRouting();
		osmRouting.init(new File("ligne-y.osm.xml"));
		List<OSMNode> path = osmRouting.getPath("266377188", "3058194700");
		StringBuilder builder = new StringBuilder();
		builder.append(
				"create table if not exists shortestpathresult (id serial primary key, geom geometry('LINESTRING', 4326));\n");
		builder.append("delete from shortestpathresult ;\n");
		builder.append("insert into shortestpathresult (geom) values(ST_GeomFromText('LINESTRING(");
		for (OSMNode node : path) {
			Coordinate coordinate = node.getCoordinate();
			builder.append(coordinate.x).append(" ").append(coordinate.y).append(",");
		}
		builder.setLength(builder.length() - 1);// remove last comma
		builder.append(")', 4326));");
		System.out.println(builder.toString());
		FileOutputStream output = new FileOutputStream("/tmp/result.sql");
		IOUtils.write(builder.toString(), output);
		output.close();
	}

	private class SaxHandler extends DefaultHandler {

		private OSMWay currentWay = null;
		private ArrayList<OSMWay> currentRelationWays = null;
		private Map<String, String> currentRelationTags = null;

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
				currentRelationWays = new ArrayList<>();
				currentRelationTags = new HashMap<>();
			} else if (qName.equals("member")) {
				String ref = attributes.getValue("ref");
				OSMWay way = idWays.get(ref);
				if (way != null) {
					currentRelationWays.add(way);
				}
			} else if (currentWay != null && qName.equals("tag")) {
				currentWay.setTag(attributes.getValue("k"), attributes.getValue("v"));
			} else if (currentRelationTags != null && qName.equals("tag")) {
				currentRelationTags.put(attributes.getValue("k"), attributes.getValue("v"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("way")) {
				currentWay = null;
			} else if (qName.equals("relation")) {
				if ("Y".equals(currentRelationTags.get("ref"))) {
					OSMRouting.this.relation = currentRelationWays;
				}
				currentRelationWays = null;
				currentRelationTags = null;
			}
		}
	}
}