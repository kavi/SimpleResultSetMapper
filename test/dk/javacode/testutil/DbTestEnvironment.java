package dk.javacode.testutil;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

public class DbTestEnvironment {

	private IDatabaseTester databaseTester;


	private FileUtil fileUtil;
	
	private String dbName = "data/dbtest";
	
	private Connection connection;

	public void setUp() throws Exception {
		fileUtil = new FileUtil();
		System.out.println("Configuring database");
		setupH2Db();

		setupDbUnit();
		System.out.println("Database configured");
		connection = DriverManager.getConnection(getDbUrl());
	}

	public void tearDown() throws Exception {
		databaseTester.onTearDown();
		connection.close();
	}

	private void setupDbUnit() throws ClassNotFoundException, MalformedURLException, DataSetException, Exception {
		// Create JdbcDatabaseTest using h2
		databaseTester = new JdbcDatabaseTester("org.h2.Driver", getDbUrl());
		
		// Load the dataset which will be inserted before the test.
		IDataSet dataSet = null;
		FlatXmlDataSetBuilder xml = new FlatXmlDataSetBuilder();
		dataSet = xml.build(new File("data/persons.xml"));
		databaseTester.setDataSet(dataSet);

		// will call default setUpOperation (clean insert of dataset)
		databaseTester.onSetup();
	}

	private void setupH2Db() throws SQLException {
		// Load Table definitions from file
		String createSql = fileUtil.readFile("data/persons.sql");
		
		// Execute table definitions against db
		Connection connection = DriverManager.getConnection(getDbUrl());
		Statement stmt = connection.createStatement();
		stmt.execute(createSql);
	}

	public String getDbUrl() {
		return "jdbc:h2:" + dbName;
		// return "jdbc:mysql://" + dbHost + "/" + dbName + "?user=" + dbUser +
		// "&password=" + dbPass;
	}
	
	public Connection getConnection() {
		return connection;
	}
}
