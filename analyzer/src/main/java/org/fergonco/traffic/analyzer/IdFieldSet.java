package org.fergonco.traffic.analyzer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.fergonco.tpg.trafficViewer.jpa.Shift;

public class IdFieldSet implements OutputFieldSet {

	@Override
	public String[] getNames() {
		return new String[] { "uid" };
	}

	@Override
	public Object[] getValues(OutputContext outputContext) throws ParseException {
		Shift shift = outputContext.getShift();
		return new Object[] { getShiftId(shift) };
	}

	public static String getShiftId(Shift shift) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(shift.getTimestamp());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		if (c.get(Calendar.HOUR_OF_DAY) < 2) {
			dayOfMonth--;
		}
		return dayOfMonth + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR) + "+" + shift.getVehicleId();
	}

}
