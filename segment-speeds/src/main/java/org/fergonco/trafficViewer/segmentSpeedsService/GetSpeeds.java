package org.fergonco.trafficViewer.segmentSpeedsService;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.OSMShift;
import org.fergonco.tpg.trafficViewer.jpa.Shift;
import org.fergonco.utils.MissingMandatoryParameterException;
import org.fergonco.utils.ParameterConversionException;
import org.fergonco.utils.ServletUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/get-speeds")
public class GetSpeeds extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		DBUtils.setSchemaName("app");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletUtils servletUtils = new ServletUtils(this, req, resp);

		try {
			long nodeStart = servletUtils.getMandatoryLong("nodeStart");
			long nodeEnd = servletUtils.getMandatoryLong("nodeEnd");
			EntityManager em = DBUtils.getEntityManager();
			TypedQuery<OSMShift> query = em.createQuery("SELECT osmshift " + "FROM OSMShift osmshift "
					+ "WHERE osmshift.startNode=" + nodeStart + " and osmshift.endNode=" + nodeEnd, OSMShift.class);
			List<OSMShift> list = query.getResultList();

			JsonArray ret = new JsonArray();
			for (OSMShift osmShift : list) {
				Shift shift = osmShift.getShift();
				JsonObject entry = new JsonObject();
				entry.addProperty("timestamp", shift.getTimestamp());
				entry.addProperty("speed", shift.getSpeed());
				ret.add(entry);
			}

			servletUtils.send("application/json", ret.toString());
		} catch (MissingMandatoryParameterException e) {
			resp.sendError(400, e.getParameterName() + " is mandatory");
		} catch (ParameterConversionException e) {
			resp.sendError(400, e.getParameterName() + " must be long");
		}

	}

}
