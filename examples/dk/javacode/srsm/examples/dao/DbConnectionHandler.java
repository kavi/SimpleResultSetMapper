package dk.javacode.srsm.examples.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionHandler {
	
	private int port;
	private String host;
	private String user;
	private String password;
	private String dbName;

	public DbConnectionHandler(int port, String host, String user, String password, String dbName) {
		super();
		this.port = port;
		this.host = host;
		this.user = user;
		this.password = password;
		this.dbName = dbName;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to load database driver", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to load database driver", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to load database driver", e);
		}
	}

	public Connection getConnection() throws SQLException {
		String connString = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?user=" + user + "&password=" + password + "&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC";
		System.out.println(connString);
		return DriverManager.getConnection(connString);
	}

}
