package org.fergonco.tpg.trafficViewer.jpa;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class TPG2OSM {

	@EmbeddedId
	private TPG2OSM.Key key;

	private long[] osmids;

	@Embeddable
	public static class Key implements Serializable {
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TPG2OSM.Key) {
				Key that = (Key) obj;
				return this.line.equals(that.line) && this.direction.equals(that.direction)
						&& this.stopName.equals(that.direction);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return line.hashCode() + direction.hashCode() + stopName.hashCode();
		}

	}

	public void setKey(TPG2OSM.Key key) {
		this.key = key;
	}

	public void setOsmids(long[] osmids) {
		this.osmids = osmids;
	}
}
