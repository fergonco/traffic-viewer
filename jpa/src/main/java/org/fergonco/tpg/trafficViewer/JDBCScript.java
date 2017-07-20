package org.fergonco.tpg.trafficViewer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCScript {

	private Connection connection;

	public JDBCScript(String connectionString, String user, String password) throws SQLException {
		connection = DriverManager.getConnection(connectionString, user, password);

	}

	public void dropCreateSchema(String schemaName) throws SQLException {
		executeCommands("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE", "CREATE SCHEMA " + schemaName);
	}

	public void executeCommands(String... commands) throws SQLException {
		Statement statement = connection.createStatement();
		try {
			for (String command : commands) {
				statement.execute(command);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			statement.close();
		}
	}

	public void executeScript(File script) throws IOException, SQLException {
		InputStream stream = new BufferedInputStream(new FileInputStream(script));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
		Statement statement = connection.createStatement();
		String line;
		StringBuilder multiline = new StringBuilder();
		try {
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("--")) {
					multiline.append(line);
					if (line.endsWith(";")) {
						String statementString = multiline.toString();
						System.out.println(statementString);
						statement.addBatch(statementString);
						multiline.setLength(0);
					}
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			stream.close();
		}
		try {
			statement.executeBatch();
		} catch (SQLException e) {
			e.getNextException().printStackTrace();
			throw e;
		}
	}

	public void close() throws SQLException {
		connection.close();
	}
}
