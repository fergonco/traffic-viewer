package org.fergonco.tpg.trafficViewer.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tpgCode", "line", "destination" }))
public class TPGStop2 {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String tpgCode;
	private String line;
	private String destination;
	private long osmid;

	public String getCode() {
		return tpgCode;
	}

	public String getNodeId() {
		return Long.toString(osmid);
	}

}
