package org.fergonco.tpg.trafficViewer.backups;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class LoadPrePreroutingDistances {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("../analyzer/pre-prerouting-distances_16062017.txt")));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			String[] parts = line.split(",");
			String start = parts[0];
			String end = parts[1];
			String distance = parts[2];
			String sql = "insert into app.pre_prerouting_distances values ('$1', '$2', $3);".replace("$1", start)
					.replace("$2", end).replace("$3", distance);
			System.out.println(sql);
		}
	}
}
