package org.fergonco.traffic.analyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.postgresql.util.PGobject;

public class ModelBuilder {

	public static void main(String[] args) throws Exception {
		ArrayList<OSMNodePair> nodePairs = buildNodePairList();
		DatasetBuilder datasetBuilder = new DatasetBuilder();
		int i = 0;
		for (OSMNodePair osmNodePair : nodePairs) {
			PrintStream stream = new PrintStream(new FileOutputStream(
					new File("/tmp/dataset-" + osmNodePair.startNode + "-" + osmNodePair.endNode + ".csv")));
			datasetBuilder.build(stream, osmNodePair.startNode, osmNodePair.endNode);
			stream.close();
			System.out.println(++i + "/" + nodePairs.size());
		}
	}

	private static ArrayList<OSMNodePair> buildNodePairList() {
		ArrayList<OSMNodePair> nodePairs = new ArrayList<>();

		EntityManager em = DBUtils.getEntityManager();
		Query query = em.createNativeQuery("select distinct(startnode, endnode) from app.osmshiftinfo;");
		@SuppressWarnings("unchecked")
		List<PGobject> list = query.getResultList();
		for (PGobject nodePairObject : list) {
			String nodePair = nodePairObject.getValue();
			Pattern pattern = Pattern.compile("\\((\\d*),(\\d*)\\)");
			Matcher matcher = pattern.matcher(nodePair);
			if (matcher.find()) {
				long startNode = Long.parseLong(matcher.group(1));
				long endNode = Long.parseLong(matcher.group(2));
				nodePairs.add(new OSMNodePair(startNode, endNode));
			}
		}
		return nodePairs;
	}
}