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

		public Key() {
		}

		public Key(String line, String direction, String stopName) {
			super();
			this.line = line;
			this.direction = direction;
			this.stopName = stopName;
		}

	}

	public void setKey(TPG2OSM.Key key) {
		this.key = key;
	}

	public void setOsmids(long[] osmids) {
		this.osmids = osmids;
	}
}
