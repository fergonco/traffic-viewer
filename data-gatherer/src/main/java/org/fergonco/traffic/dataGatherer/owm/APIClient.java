package org.fergonco.traffic.dataGatherer.owm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class APIClient {
	private final static Logger logger = Logger.getLogger(APIClient.class);

	private String key;
	private final String baseURL;

	public APIClient(String keyResourceURL, String baseURL) {
		InputStream stream = APIClient.class.getResourceAsStream("/openweathermapkey");
		try {
			key = IOUtils.toString(stream, Charset.forName("utf8"));
		} catch (IOException e) {
			throw new IllegalArgumentException("Key not found", e);
		}
		this.baseURL = baseURL;
	}

	public String get(String command, String... params) throws IOException {
		try {
			String url = baseURL + command + "?appid=" + key;
			for (int i = 0; i < params.length; i++) {
				url += "&" + params[i];
			}
			logger.debug(url);
			String ret = IOUtils.toString(new URI(url), Charset.forName("utf-8"));
			logger.debug("ok");
			logger.debug(ret);
			return ret;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Bug: Malformed URI", e);
		}
	}
}
