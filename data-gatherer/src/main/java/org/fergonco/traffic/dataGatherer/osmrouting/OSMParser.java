package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMParser {

	private HashMap<String, OSMNode> idNodes = new HashMap<>();
	private HashMap<String, OSMWay> idWays = new HashMap<>();
	private HashMap<String, OSMRelation> interestingRelations = null;
	private File osmxml;

	public OSMParser(File osmxml, String... relations) {
		this.osmxml = osmxml;
		this.interestingRelations = new HashMap<>();
		for (String relation : relations) {
			interestingRelations.put(relation, null);
		}
	}

	public void parse() throws SAXException, IOException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(osmxml));
		saxParser.parse(is, new SaxHandler());
		is.close();
		is = new BufferedInputStream(this.getClass().getResourceAsStream("osm_overrides.xml"));
		saxParser.parse(is, new OverridingSaxHandler());
		is.close();
	}

	public OSMNode getNode(String nodeId) {
		return idNodes.get(nodeId);
	}

	public OSMRelation getRelation(String relationRef) {
		return interestingRelations.get(relationRef);
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
				String type = attributes.getValue("type");
				if (type.equals("way")) {
					OSMWay way = idWays.get(ref);
					if (way != null) {
						currentRelation.addWay(way);
					}
				} else if (type.equals("node")) {
					OSMNode node = idNodes.get(ref);
					if (node != null) {
						currentRelation.addNode(node);
					}
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
				String ref = currentRelation.getTag("ref");
				if (interestingRelations.containsKey(ref)) {
					interestingRelations.put(ref, currentRelation);
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
			} else if (currentWay != null && qName.equals("nd")) {
				String ref = attributes.getValue("ref");
				OSMNode node = idNodes.get(ref);
				currentWay.addNode(node);
			} else if (currentWay != null && qName.equals("noroute")) {
				currentWay.setNoRoute(attributes.getValue("direction"));
			} else if (qName.equals("delete-node")) {
				String id = attributes.getValue("id");
				String relation = attributes.getValue("relation");
				interestingRelations.get(relation).removeNode(idNodes.remove(id));
			} else if (qName.equals("delete-way")) {
				String id = attributes.getValue("id");
				String relation = attributes.getValue("relation");
				interestingRelations.get(relation).removeWay(idWays.remove(id));
			} else if (qName.equals("add-way")) {
				String id = attributes.getValue("id");
				String relation = attributes.getValue("relation");
				interestingRelations.get(relation).addWay(idWays.get(id));
			} else if (qName.equals("add-node")) {
				String id = attributes.getValue("id");
				String relation = attributes.getValue("relation");
				interestingRelations.get(relation).addNode(idNodes.get(id));
			} else if (qName.equals("create-way")) {
				String id = attributes.getValue("id");
				OSMWay newWay = new OSMWay(id);
				idWays.put(id, newWay);
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