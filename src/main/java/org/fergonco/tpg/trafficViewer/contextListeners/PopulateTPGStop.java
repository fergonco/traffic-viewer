package org.fergonco.tpg.trafficViewer.contextListeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fergonco.tpg.trafficViewer.DBUtils;
import org.fergonco.tpg.trafficViewer.jpa.TPGStop;
import org.fergonco.tpg.trafficViewer.osmrouting.OSMUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

@WebListener
public class PopulateTPGStop implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		InputStream tpgStops = this.getClass().getResourceAsStream("tpgstops.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(tpgStops));
		String line;
		EntityManager em = DBUtils.getEntityManager();
		TPGStop cernStop = em.find(TPGStop.class, "CERN");
		if (cernStop == null) {
			try {
				em.getTransaction().begin();
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(Pattern.quote(","));
					String stopCode = parts[0];
					double lon = Double.parseDouble(parts[1]);
					double lat = Double.parseDouble(parts[2]);
					TPGStop stop = new TPGStop();
					stop.setCode(stopCode);
					Geometry point = OSMUtils.buildPoint(new Coordinate(lon, lat));
					point.setSRID(4326);
					stop.setGeom(point);
					em.persist(stop);
				}
				em.getTransaction().commit();
			} catch (IOException e) {
				em.getTransaction().rollback();
				throw new RuntimeException("Cannot read internal resource!", e);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
