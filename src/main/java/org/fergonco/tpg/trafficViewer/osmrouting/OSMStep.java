package org.fergonco.tpg.trafficViewer.osmrouting;

public class OSMStep {

	private OSMWay way;
	private OSMNode start;
	private OSMNode end;

	public OSMStep(OSMWay way, OSMNode start, OSMNode end) {
		super();
		this.way = way;
		this.start = start;
		this.end = end;
	}

}
