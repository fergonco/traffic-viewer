package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TPGStop {

	@Id
	private String code;

	private long osmid1;
	private String destination1;
	private long osmid2;
	private String destination2;

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getNodeId(String destination) {
		if (destination.equals(destination1)) {
			return Long.toString(osmid1);
		} else if (destination.equals(destination2)) {
			return Long.toString(osmid2);
		} else {
			throw new IllegalArgumentException("Unknown destination: " + destination);
		}
	}
}
