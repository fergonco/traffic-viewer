package org.fergonco.traffic.dataGatherer.owm;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class OWMTimer {

	private OWM owm;
	private int periodMs;
	private OWMListener listener;
	private Timer timer;

	public OWMTimer(OWM owm, int periodMs, OWMListener listener) {
		this.owm = owm;
		this.periodMs = periodMs;
		this.listener = listener;
	}

	public void start() {
		if (timer != null) {
			throw new IllegalStateException("timer is already started");
		}
		timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					listener.newConditions(owm.currentConditions(6, 46.25));
				} catch (IOException e) {
					listener.error(e);
				}
			}
		}, periodMs, periodMs);
	}

	public void stop() {
		if (timer == null) {
			throw new IllegalStateException("timer is already stopped");
		}
		timer.cancel();
		timer = null;
	}

}
