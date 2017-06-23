package org.fergonco.traffic.analyzer.predict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.traffic.analyzer.DatasetBuilder;
import org.postgresql.util.PGobject;

public class ModelBuilder {

	public static void main(String[] args) throws Exception {
		ModelBuilder mb = new ModelBuilder();
		ArrayList<OSMNodePair> nodePairs = mb.buildNodePairList();

		DatasetBuilder datasetBuilder = new DatasetBuilder();
		int i = 0;
		for (OSMNodePair osmNodePair : nodePairs) {
			long startNode = osmNodePair.startNode;
			long endNode = osmNodePair.endNode;
			PrintStream stream = new PrintStream(new FileOutputStream(getFileName(startNode, endNode)));
			datasetBuilder.build(stream, startNode, endNode);
			stream.close();
			System.out.println(++i + "/" + nodePairs.size());
		}

		for (OSMNodePair osmNodePair : nodePairs) {
			LinearModel linearModel = mb.parse(getFileName(osmNodePair.startNode, osmNodePair.endNode));
			System.out.println(linearModel);
		}
	}

	private static File getFileName(long startNode, long endNode) {
		return new File("/tmp/dataset-" + startNode + "-" + endNode + ".csv");
	}

	public ArrayList<OSMNodePair> buildNodePairList() {
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

	public LinearModel parse(File file) throws IOException {
		String command = "Rscript analyse/modeler.r " + file.getAbsolutePath();
		ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s"));
		processBuilder.redirectOutput(Redirect.PIPE);
		Process process = processBuilder.start();
		InputStreamReader isr = new InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String line;
		Pattern pattern = Pattern.compile("\\[1\\]\\s\\\"([^\\s]+)\\s(-?\\d+\\.\\d+)");
		LinearModel variableCoefficient = new LinearModel();
		while ((line = br.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String variable = matcher.group(1);
				double coefficient = Double.parseDouble(matcher.group(2));
				System.out.println(line);
				System.out.println(variable + ": " + coefficient);
				variableCoefficient.put(variable, coefficient);
			} else {
				throw new RuntimeException("Cannot parse line: " + line);
			}
		}
		return variableCoefficient;
	}
}