package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ModelBuilderTest {

	@Test
	public void testSaveModelAndReuse() throws IOException, PredictionException {
		ModelBuilder builder = new ModelBuilder();
		File modelFile = File.createTempFile("ModelBuilderTest", ".rds");
		builder.generateModel(new File("analyse/tocern.csv"), modelFile);

		Predictor predictor = new Predictor();
		String testDatasetContent = "uid,speed,timestamp,minutesHour,minutesDay,distortedMinutes,morningrush,morningfall,morningrise,remainingday,weekday,holidayfr,holidaych,schoolfr,schoolch,humidity,pressure,rain3h,snow3h,temperature,weather\n"
				+ "2-2-2017+217666,27,1488433956000,52,412,6.164414002968976,true,38,NA,NA,thursday,false,false,false,true,93.0,1015.0,,,7.26,rain";
		File testDataset = File.createTempFile("ModelBuilderTest", ".csv");
		IOUtils.write(testDatasetContent, new FileOutputStream(testDataset), "utf-8");

		try {
			double[] prediction = predictor.getCenterAndPredictedInterval(modelFile, testDataset);
			assertTrue(prediction[0] < 50 && prediction[0] > 0);
			assertTrue(prediction[0] - prediction[1] < 20);
			assertTrue(prediction[2] - prediction[0] < 20);
		} finally {
			testDataset.delete();
			modelFile.delete();
		}
	}

}
