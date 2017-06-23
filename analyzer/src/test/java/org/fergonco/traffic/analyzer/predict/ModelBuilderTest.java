package org.fergonco.traffic.analyzer.predict;

import java.io.File;
import java.io.IOException;

import org.fergonco.traffic.analyzer.predict.LinearModel;
import org.fergonco.traffic.analyzer.predict.ModelBuilder;
import org.junit.Test;

public class ModelBuilderTest {

	@Test
	public void testExecuteRScriptAndParseOutput() throws IOException {
		ModelBuilder builder = new ModelBuilder();
		LinearModel linearModel = builder.parse(new File("analyse/tocern.csv"));
		System.out.println(linearModel);
	}

}
