package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class RscriptTest {

	@Test
	public void executeScript() throws Exception {
		Rscript rscript = new Rscript();
		InputStream processOutput = rscript.executeResource("testscript.r", "1", "2", "3");
		String output = IOUtils.toString(processOutput, "utf-8");
		assertEquals("[1] 6\n", output);
	}
}
