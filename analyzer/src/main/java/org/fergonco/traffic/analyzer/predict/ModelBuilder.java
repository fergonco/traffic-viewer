package org.fergonco.traffic.analyzer.predict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.DBUtils.AbortPaginationException;
import org.fergonco.tpg.trafficViewer.jpa.OSMSegment;
import org.fergonco.traffic.analyzer.DatasetBuilder;
import org.fergonco.traffic.analyzer.NotEnoughShiftsForSegment;

public class ModelBuilder {
	private static final Logger logger = LogManager.getLogger(ModelBuilder.class.getName());

	public static void main(String[] args) throws Exception {
		ModelBuilder mb = new ModelBuilder();
		mb.generateModels();
	}

	public void generateModels(List<Long> segmentIds) throws IOException, ParseException, RException {
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMSegment> query = em
				.createQuery("select s from " + OSMSegment.class.getSimpleName() + " s where s.id in :ids",
						OSMSegment.class)
				.setParameter("ids", segmentIds);

		generateModels(em, query);
	}

	public void generateModels() throws IOException, ParseException, RException {
		logger.debug("Getting osmsegments");
		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<OSMSegment> query = em.createQuery("Select a from " + OSMSegment.class.getSimpleName() + " a",
				OSMSegment.class);
		generateModels(em, query);
	}

	private void generateModels(EntityManager em, TypedQuery<OSMSegment> segmentQuery) throws AbortPaginationException {
		DatasetBuilder datasetBuilder = new DatasetBuilder();

		DBUtils.paginatedProcessing(segmentQuery, 100, new DBUtils.PageProcessor<OSMSegment>() {

			@Override
			public void processPage(List<OSMSegment> osmSegments) throws AbortPaginationException {
				int i = 0;
				em.getTransaction().begin();
				for (OSMSegment osmSegment : osmSegments) {
					try {
						File datasetFileName = File.createTempFile("dataset", ".csv");

						// Build dataset
						PrintStream stream = new PrintStream(new FileOutputStream(datasetFileName));
						try {
							datasetBuilder.build(stream, osmSegment);
						} catch (NotEnoughShiftsForSegment e) {
							continue;
						} finally {
							stream.close();
						}

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
						em.persist(osmSegment);

						datasetFileName.delete();
						modelFileName.delete();

						logger.debug(++i + "/" + osmSegments.size());
					} catch (IOException | RException e) {
						throw new AbortPaginationException(e);
					}
				}
				em.getTransaction().commit();
				em.clear();
			}
		});

	}

}