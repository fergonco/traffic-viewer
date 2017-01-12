package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class TPG2OSM {

	@EmbeddedId
	private TPG2OSM.Key key;

	private long[] osmids;

	@Embeddable
	public static class Key {
		private String line;
		private String direction;
		private String stopName;
	}

}
