package org.fergonco.traffic.analyzer.predict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegmentModel;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.traffic.analyzer.DatasetBuilder;

public class ModelBuilder {

	public static void main(String[] args) throws Exception {
		ModelBuilder mb = new ModelBuilder();
		mb.generateModels();
	}

	public void generateModels() throws IOException, ParseException, RException {
		generateModels(getUniqueOSMShifts());
	}

	private void generateModels(ArrayList<OSMShift> osmShifts) throws IOException, ParseException, RException {
		DatasetBuilder datasetBuilder = new DatasetBuilder();
		int i = 0;
		for (OSMShift osmShift : osmShifts) {
			long startNode = osmShift.getStartNode();
			long endNode = osmShift.getEndNode();
			File datasetFileName = getDatasetFileName(startNode, endNode);

			// Build dataset
			PrintStream stream = new PrintStream(new FileOutputStream(datasetFileName));
			datasetBuilder.build(stream, startNode, endNode);
			stream.close();

			// Generate file with the model
			File modelFileName = getModelFileName(osmShift.getStartNode(), osmShift.getEndNode());
			generateModel(datasetFileName, modelFileName);
			System.out.println(++i + "/" + osmShifts.size());

			// Store model in database
			byte[] modelBytes = IOUtils.toByteArray(datasetFileName.toURI());
			EntityManager em = DBUtils.getEntityManager();
			OSMSegmentModel osmSegmentModel = new OSMSegmentModel();
			osmSegmentModel.setStartNode(startNode);
			osmSegmentModel.setEndNode(endNode);
			osmSegmentModel.setModel(modelBytes);
			em.getTransaction().begin();
			em.persist(osmSegmentModel);
			em.getTransaction().commit();
		}
	}

	public void generateModel(File datasetFileName, File modelFileName) throws IOException, RException {
		String command = "Rscript analyse/modeler.r " + datasetFileName.getAbsolutePath() + " " + modelFileName;
		ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s"));
		Process process = processBuilder.start();
		while (process.isAlive()) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		}
		if (process.exitValue() > 0) {
			throw new RException("modeler returned error");
		}
	}

	private static File getModelFileName(long startNode, long endNode) {
		return new File("/tmp/dataset-" + startNode + "-" + endNode + ".rda");
	}

	private static File getDatasetFileName(long startNode, long endNode) {
		return new File("/tmp/dataset-" + startNode + "-" + endNode + ".csv");
	}

	public ArrayList<OSMShift> getUniqueOSMShifts() {
		ArrayList<OSMShift> osmShifts = new ArrayList<>();

		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMShift> query = em.createQuery("select o from OSMShift o", OSMShift.class);
		List<OSMShift> list = query.getResultList();
		HashSet<OSMNodePair> uniqueSet = new HashSet<>();
		for (OSMShift osmShift : list) {
			OSMNodePair osmNodePair = new OSMNodePair(osmShift.getStartNode(), osmShift.getEndNode());
			if (uniqueSet.contains(osmNodePair)) {
				continue;
			} else {
				osmShifts.add(osmShift);
				uniqueSet.add(osmNodePair);
			}
		}
		return osmShifts;
	}

	private class OSMNodePair {
		long startNode;
		long endNode;

		public OSMNodePair(long startNode, long endNode) {
			super();
			this.startNode = startNode;
			this.endNode = endNode;
		}

		@Override
		public int hashCode() {
			return (int) (startNode - endNode);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OSMNodePair) {
				OSMNodePair that = (OSMNodePair) obj;
				return this.startNode == that.startNode && this.endNode == that.endNode;
			}
			return false;
		}
	}
}