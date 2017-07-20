package org.fergonco.tpg.trafficViewer.backups;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author fergonco Tool to process old backups. Set values for null
 *         sourceStartPoint and sourceEndPoint
 */
public class SetSourceEndPoints {

	public static void main(String[] args) throws SQLException {
		Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:54322/b", "tpg", "tpg");
		Statement s = c.createStatement();

		while (true) {
			ResultSet r = s
					.executeQuery("select id from app.shift where sourcestartpoint is null and sourceendpoint is null");
			long id = -1;
			if (r.next()) {
				id = r.getLong("id");
			} else {
				System.exit(0);
			}
			r.close();
			if (id != -1) {
				r = s.executeQuery("select startnode, endnode from app.osmshift os where os.shift_id=" + id);
				if (!r.next()) {
					throw new RuntimeException(id + "");
				}
				long startnode = r.getLong("startnode");
				long endnode = r.getLong("endnode");
				r = s.executeQuery("select sourcestartpoint, sourceendpoint " //
						+ "from app.shift s, app.osmshift os "//
						+ "where os.shift_id=s.id"//
						+ " and os.startnode=" + startnode //
						+ " and os.endnode=" + endnode //
						+ " and s.sourcestartpoint is not null" //
						+ " and s.sourceendpoint is not null " //
						+ "group by sourcestartpoint, sourceendpoint");
				if (!r.next()) {
					throw new RuntimeException();
				}
				String sourcestartpoint = r.getString("sourcestartpoint");
				String sourceendpoint = r.getString("sourceendpoint");
				String where = "where id in (select shift_id from app.osmshift os where os.startnode=" + startnode
						+ " and os.endnode=" + endnode + ")";
				System.out.println("select sourcestartpoint, sourceendpoint, count(*) from app.shift " + where
						+ " group by sourcestartpoint, sourceendpoint ;");
				String sql = "update app.shift set sourcestartpoint='" + sourcestartpoint + "', sourceendpoint='"
						+ sourceendpoint + "' " + where + ";";
				System.out.println(sql);
				if (r.next()) {
					throw new RuntimeException();
				}
				s.execute(sql);
			}
		}

	}

}
