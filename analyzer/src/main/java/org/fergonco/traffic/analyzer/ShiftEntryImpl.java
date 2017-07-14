package org.fergonco.traffic.analyzer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.traffic.analyzer.OutputContext.ShiftEntry;

public class ShiftEntryImpl implements ShiftEntry {

	private Shift shift;
	private double distance;

	public ShiftEntryImpl(Shift shift, double distance) {
		super();
		this.shift = shift;
		this.distance = distance;
	}

	@Override
	public long getTimestamp() {
		return shift.getTimestamp();
	}

	@Override
	public String getId() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(shift.getTimestamp());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		if (c.get(Calendar.HOUR_OF_DAY) < 2) {
			dayOfMonth--;
		}
		return dayOfMonth + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR) + "+" + shift.getVehicleId();
	}

	@Override
	public int getSpeed() {
		double h = shift.getSeconds() / (60.0 * 60);
		double km = distance / 1000;
		return (int) (km / h);
	}

}
