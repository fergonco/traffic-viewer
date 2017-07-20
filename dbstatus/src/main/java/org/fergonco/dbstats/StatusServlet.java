package org.fergonco.dbstats;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fergonco.tpg.trafficViewer.DBUtils;

@WebServlet("/dbstatus")
public class StatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String answer = "weather: $weatherStatus\ntransport: $transportStatus\npredictions: $predictionStatus";
		Date nowDate = new Date();
		long now = nowDate.getTime();

		EntityManager em = DBUtils.getEntityManager();
		Long lastWeatherCondition = (Long) em.createNativeQuery("select max(\"timestamp\") from app.WeatherConditions;")
				.getSingleResult();
		String weatherStatus = (now - lastWeatherCondition > 5 * 60 * 60 * 1000) ? "fail" : "success";

		Long lastTransportShift = (Long) em.createNativeQuery("select max(\"timestamp\") from app.Shift;")
				.getSingleResult();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(nowDate);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String transportStatus = ((hour > 7) && (now - lastTransportShift > 60 * 60 * 1000)) ? "fail" : "success";

		Long lastPrediction = (Long) em.createNativeQuery("select max(\"millis\") from app.predictedshift;")
				.getSingleResult();
		String predictionStatus = (lastPrediction == null || lastPrediction < now) ? "fail" : "success";

		answer = answer.replace("$weatherStatus", weatherStatus);
		answer = answer.replace("$transportStatus", transportStatus);
		answer = answer.replace("$predictionStatus", predictionStatus);
		resp.getWriter().write(answer);
	}

}
