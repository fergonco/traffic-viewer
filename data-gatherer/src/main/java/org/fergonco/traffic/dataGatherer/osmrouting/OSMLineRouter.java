package org.fergonco.traffic.dataGatherer.osmrouting;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;

public class OSMLineRouter {

	OSMRelation relation = null;
	private DefaultDirectedGraph<OSMNode, OSMStep> graph;
	private OSMParser osmParser;

	public OSMLineRouter(OSMParser osmParser, String lineName)
			throws ParserConfigurationException, SAXException, IOException {
		this.osmParser = osmParser;
		relation = osmParser.getRelation(lineName);

		graph = new DefaultDirectedGraph<OSMNode, OSMStep>(OSMStep.class);
		for (OSMWay osmWay : relation.getWays()) {
			OSMNode[] wayNodes = osmWay.getNodes();
			for (int i = 0; i < wayNodes.length - 1; i++) {
				OSMNode currentNode = wayNodes[i];
				OSMNode nextNode = wayNodes[i + 1];
				graph.addVertex(currentNode);
				graph.addVertex(nextNode);
				graph.addEdge(currentNode, nextNode, new OSMStep(currentNode, nextNode, osmWay));
				boolean twoWay = !"yes".equals(osmWay.getTag("oneway"))
						&& !"roundabout".equals(osmWay.getTag("junction"));
				if (twoWay) {
					graph.addEdge(nextNode, currentNode, new OSMStep(nextNode, currentNode, osmWay));
				}
			}
		}

	}

	public OSMRoutingResult getPathFromNodeOutsideGraph(String startNodeId, String endNodeId) {
		Coordinate startCoordinate = osmParser.getNode(startNodeId).getCoordinate();
		Coordinate endCoordinate = osmParser.getNode(endNodeId).getCoordinate();
		return getPathOSMStops(startCoordinate, endCoordinate);
	}

	private OSMRoutingResult getPathOSMStops(Coordinate start, Coordinate end) {
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

	private OSMRoutingResult getPath(String startNodeId, String endNodeId) {
		OSMNode a = osmParser.getNode(startNodeId);
		OSMNode b = osmParser.getNode(endNodeId);
		GraphPath<OSMNode, OSMStep> result = DijkstraShortestPath.findPathBetween(graph, a, b);
		if (result != null) {
			return new OSMRoutingResult(result);
		} else {
			return null;
		}
	}

	public Iterable<OSMWay> getWays() {
		return relation.getWays();
	}

	public Iterable<OSMNode> getNodes() {
		return relation.getNodes();
	}

}