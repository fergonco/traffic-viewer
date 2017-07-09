package org.fergonco.traffic.analyzer.predict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.DBUtils.AbortPaginationException;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegmentModel;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.traffic.analyzer.DatasetBuilder;

public class ModelBuilder {
	private static final Logger logger = LogManager.getLogger(ModelBuilder.class.getName());

	public static void main(String[] args) throws Exception {
		ModelBuilder mb = new ModelBuilder();
		mb.generateModels();
	}

	public void generateModels() throws IOException, ParseException, RException {
		logger.debug("Getting unique osmshifts");
		ArrayList<OSMShift> osmShifts = getUniqueOSMShifts();
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
			Rscript r = new Rscript();
			InputStream stdout = r.executeResource("modeler.r", datasetFileName.getAbsolutePath(),
					modelFileName.getAbsolutePath());
			if (r.getExitCode() != 0) {
				throw new RException(IOUtils.toString(stdout, "utf-8"));
			}

			// Store model in database
			byte[] modelBytes = IOUtils.toByteArray(modelFileName.toURI());
			EntityManager em = DBUtils.getEntityManager();
			OSMSegmentModel osmSegmentModel = new OSMSegmentModel();
			osmSegmentModel.setStartNode(startNode);
			osmSegmentModel.setEndNode(endNode);
			osmSegmentModel.setModel(modelBytes);
			em.getTransaction().begin();
			em.persist(osmSegmentModel);
			em.getTransaction().commit();

			logger.debug(++i + "/" + osmShifts.size());
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
		HashSet<OSMNodePair> uniqueSet = new HashSet<>();

		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMShift> query = em.createQuery("select o from OSMShift o", OSMShift.class);
		try {
			DBUtils.paginatedProcessing(query, 20000, new DBUtils.PageProcessor<OSMShift>() {

				@Override
				public void processPage(List<OSMShift> list) throws AbortPaginationException {
					logger.debug("processing page...");
					for (OSMShift osmShift : list) {
						OSMNodePair osmNodePair = new OSMNodePair(osmShift.getStartNode(), osmShift.getEndNode());
						if (uniqueSet.contains(osmNodePair)) {
							continue;
						} else {
							osmShifts.add(osmShift);
							uniqueSet.add(osmNodePair);
						}
					}
				}
			});
		} catch (AbortPaginationException e) {
			throw new RuntimeException("Bug", e);
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
			return (int) (startNode + endNode);
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