package org.fergonco.traffic.analyzer.predict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.traffic.analyzer.DatasetBuilder;

public class ModelBuilder {
	private static final Logger logger = LogManager.getLogger(ModelBuilder.class.getName());

	public static void main(String[] args) throws Exception {
		ModelBuilder mb = new ModelBuilder();
		mb.generateModels();
	}

	public void generateModels() throws IOException, ParseException, RException {
		logger.debug("Getting unique osmshifts");
		EntityManager em = DBUtils.getEntityManager();
		List<OSMSegment> osmSegments = em
				.createQuery("select s from " + OSMSegment.class.getSimpleName() + " s", OSMSegment.class)
				.getResultList();

		DatasetBuilder datasetBuilder = new DatasetBuilder();
		int i = 0;
		for (OSMSegment osmSegment : osmSegments) {
			if (osmSegment.getShifts().size() == 0) {
				continue;
			}
			System.out.println("\n\n\n" + osmSegment.getId() + "\n\n\n");
			File datasetFileName = File.createTempFile("dataset", ".csv");

			// Build dataset
			PrintStream stream = new PrintStream(new FileOutputStream(datasetFileName));
			datasetBuilder.build(stream, osmSegment);
			stream.close();

			// Generate file with the model
			File modelFileName = File.createTempFile("model", ".rds");
			Rscript r = new Rscript();
			InputStream stdout = r.executeResource("modeler.r", datasetFileName.getAbsolutePath(),
					modelFileName.getAbsolutePath());
			if (r.getExitCode() != 0) {
				throw new RException(IOUtils.toString(stdout, "utf-8"));
			}

			// Store model in database
			byte[] modelBytes = IOUtils.toByteArray(modelFileName.toURI());
			osmSegment.setModel(modelBytes);
			em.getTransaction().begin();
			em.persist(osmSegment);
			em.getTransaction().commit();

			datasetFileName.delete();
			modelFileName.delete();

			logger.debug(++i + "/" + osmSegments.size());
		}
	}

}