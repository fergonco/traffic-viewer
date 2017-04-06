package org.fergonco.traffic.analyzer.duplicates;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.Shift;

public class DuplicateRemover {

	private HashMap<String, Shift> codeShift = new HashMap<>();

	private void doClean() {
		EntityManager em = DBUtils.getEntityManager();
		ArrayList<Shift> toRemove = new ArrayList<>();
		List<Shift> shifts = em.createQuery("SELECT s FROM Shift s ORDER BY s.timestamp", Shift.class).getResultList();
		int repeatedCount = 0;
		for (Shift shift : shifts) {
			String code = getCode(shift);
			if (shift.getSpeed() == -1) {
				toRemove.add(shift);
				if (codeShift.containsKey(code)) {
					toRemove.add(codeShift.get(code));
				}
			} else if (codeShift.containsKey(code)) {
				repeatedCount++;
				Shift existingShift = codeShift.get(code);
				System.out.println(
						"Duplicate found:" + existingShift.getId() + ", " + shift.getId() + ". Timestamp difference: "
								+ (shift.getTimestamp() - existingShift.getTimestamp()) / (60.0 * 1000) + ". Speed: "
								+ existingShift.getSpeed() + "," + shift.getSpeed());
				if (shift.getSpeed() != -1 && shift.getVehicleId().equals(existingShift.getVehicleId())
						&& shift.getTimestamp() >= existingShift.getTimestamp()
						&& shift.getTimestamp() - existingShift.getTimestamp() < 60 * 60 * 1000) {
					toRemove.add(existingShift);
					codeShift.put(code, shift);
				} else {
					// All cases must pass through the other code
					throw new RuntimeException();
				}
			} else {
				codeShift.put(code, shift);
			}
		}
		System.out.println("\n\n\n\n");
		for (Shift shift : toRemove) {
			System.out.println(StringUtils
					.join(new Object[] { shift.getId(), new Date(shift.getTimestamp()), shift.getSpeed() }, ","));
		}
		System.out.println(toRemove.size());
		System.out.println("unique shift count " + codeShift.size() + " of which " + repeatedCount + " repeated");
	}

	private String getCode(Shift shift) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(shift.getTimestamp());
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		if (c.get(Calendar.HOUR_OF_DAY) < 2) {
			dayOfMonth--;
		}
		return dayOfMonth + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR) + "+" + shift.getVehicleId();
	}

	public static void main(String[] args) {
		new DuplicateRemover().doClean();
	}
}
