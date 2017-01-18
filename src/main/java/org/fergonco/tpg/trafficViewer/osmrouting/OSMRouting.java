package org.fergonco.tpg.trafficViewer.osmrouting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMRouting {

	private HashMap<String, OSMNode> idNodes = new HashMap<>();
	private HashMap<String, OSMWay> idWays = new HashMap<>();
	public ArrayList<OSMWay> relation = null;
	private ListenableUndirectedGraph<OSMNode, OSMWay> graph;

	public void init(File osmxml) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(osmxml));
		saxParser.parse(is, new SaxHandler());
		is.close();

		graph = new ListenableUndirectedGraph<OSMNode, OSMWay>(OSMWay.class);
		for (OSMWay osmWay : relation) {
			OSMNode start = osmWay.getFirstNode();
			OSMNode end = osmWay.getLastNode();
			graph.addVertex(start);
			graph.addVertex(end);
			graph.addEdge(start, end, osmWay);
		}

	}

	public String[] getPath(String startNodeId, String endNodeId) {
		OSMNode a = idNodes.get(startNodeId);
		OSMNode b = idNodes.get(endNodeId);
		GraphPath<OSMNode, OSMWay> result = DijkstraShortestPath.findPathBetween(graph, a, b);
		List<OSMWay> edgeList = result.getEdgeList();

		String[] ret = new String[edgeList.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = edgeList.get(i).getId();
		}

		return ret;
	}

	public static void main(String[] args) throws Exception {
		OSMRouting osmRouting = new OSMRouting();
		osmRouting.init(new File("ligne-y.osm.xml"));
		String[] path = osmRouting.getPath("134231234", "235235235");
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
						Double.parseDouble(attributes.getValue("lon")));
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