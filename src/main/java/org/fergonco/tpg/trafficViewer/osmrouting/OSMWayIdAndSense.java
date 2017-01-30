package org.fergonco.tpg.trafficViewer.osmrouting;

public class OSMWayIdAndSense {

	private String osmId;
	private boolean forward;

	public OSMWayIdAndSense(String osmId, boolean forward) {
		this.osmId = osmId;
		this.forward = forward;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OSMWayIdAndSense) {
			OSMWayIdAndSense that = (OSMWayIdAndSense) obj;
			return that.osmId.equals(osmId) && that.forward == forward;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return osmId.hashCode() + (forward ? 1 : 0);
	}

	public String getOsmId() {
		return osmId;
	}

	public boolean isForward() {
		return forward;
	}
}
