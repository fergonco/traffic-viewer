package org.fergonco.traffic.analyzer.predict;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class RscriptTest {

	@Test
	public void executeScript() throws Exception {
		Rscript rscript = new Rscript();
		String output = rscript.executeResource("testscript.r", "1", "2", "3");
		assertEquals("[1] 6\n", output);
	}
}
