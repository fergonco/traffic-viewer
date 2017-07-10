package org.fergonco.tpg.trafficViewer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.fergonco.tpg.trafficViewer.jpa.Shift;

/**
 * Creates the tables and data necessary to run the application
 * 
 * @author fergonco
 */
public class CreateDatabaseSchema {

	public static void main(String[] args) throws SQLException, IOException {
		// Create schema
		JDBCScript script = new JDBCScript("jdbc:postgresql://localhost:54322/tpgtest", "tpg", "tpg");
		script.executeCommands("create schema app;");

		// Some of the tables with data: tpgstoproute
		script.executeScript(new File("../data-gatherer/tpgstoproute.sql"));

		// The rest of the tables
		DBUtils.setPersistenceUnit("test");
		EntityManager em = DBUtils.getEntityManager();
		em.createQuery("SELECT s FROM Shift s", Shift.class).getResultList();

		// Dump the database
		ProcessBuilder pb = new ProcessBuilder(new String[] { "pg_dump", "-h", "localhost", "-p", "54322", "--username",
				"tpg", "--schema=app", "--file=db.sql", "--encoding=utf-8", "-t", "app.*", "tpgtest" });
		pb.environment().put("PGPASSWORD", "tpg");

		Process process = pb.start();
		while (process.isAlive()) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		}
		if (process.exitValue() != 0) {
			throw new RuntimeException("pg_dump failed");
		}

		System.out.println("Ok.");
	}
}
