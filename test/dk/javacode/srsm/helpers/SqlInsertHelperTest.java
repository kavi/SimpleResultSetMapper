package dk.javacode.srsm.helpers;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.ResultSetMapper;
import dk.javacode.srsm.SqlSelectColumn;
import dk.javacode.srsm.SqlSelectColumn.SqlOperator;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.testmodel.Address;
import dk.javacode.srsm.testmodel.Car;
import dk.javacode.srsm.testmodel.Person;
import dk.javacode.testutil.DbTestEnvironment;

public class SqlInsertHelperTest {

	private DbTestEnvironment dbEnv = new DbTestEnvironment();
	private SqlInsertHelper sqlInsert = new SqlInsertHelper();
	private SqlQueryHelper sqlQuery = new SqlQueryHelper();
	private ResultSetMapper mapper = new ResultSetMapper();
	
	@Before
	public void setUp() throws Exception {
		dbEnv.setUp();
	}

	@After
	public void tearDown() throws Exception {
		dbEnv.tearDown();
	}


	@Test
	public void testInsert() throws MappingException, SQLException, IllegalArgumentException, IllegalAccessException {
		Connection con = dbEnv.getConnection();
		Person p = new Person();
		p.setAddress(null);
		p.setAge(10);
		p.setName("Hans");
		p.setRandomProperty("ssdj");
		p.setSmoker(true);
		sqlInsert.insert(con, p, "persons");
		System.out.println(p);
		
		// Assert
		List<Person> all = sqlQuery.getAll(con, Person.class);
		assertEquals(3, all.size());
		
		assertNotNull("Id property should be set", p.getId());
		
		Person fromDb = sqlQuery.getOne(con, Person.class, new SqlSelectColumn("id", p.getId()));
		assertNotNull("Find by id should locate an object", fromDb);
	}
	
	@Test
	public void testInsertAddress() throws MappingException, SQLException, IllegalArgumentException, IllegalAccessException {
		Connection con = dbEnv.getConnection();
		Address a = new Address();
		a.setIsPublic(true);
		a.setStreetName("MyStreet");
		sqlInsert.insert(con, a, "address", Long.class);
		assertNotNull("id", a.getId2());
		
		assertNotNull(SqlQueryDsl.build(Address.class).where("id", SqlOperator.EQUALS, a.getId2()));
	}
	
	
	@Test
	public void testInsert2() throws SQLException, IllegalArgumentException, IllegalAccessException, MappingException {
		// Setup
		Connection connection = dbEnv.getConnection();
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		connection.setAutoCommit(false);
		
		Address a = new Address();
		a.setId2(1L);
		
		Car c = new Car();
		c.setId(1);
		
		Person p = new Person();
		p.setName("Vlad III");
		p.setAge(423);
		p.setAddress(a);
//		p.setCar(c);
		
		// Execute
		Integer id = sqlInsert.insert(connection, p, "persons");
		System.out.println("id: " + id);
		connection.commit();

		// Assert (dbUnit does support direct table comparison rather than querying manually...
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select * from persons");
		List<Person> list = mapper.toPojo(Person.class, resultSet);
		for (Person p1 : list) {
			System.out.println(p1);
		}
		assertNotNull(list);
		assertEquals(3, list.size());
	}
	
	@Test
	public void testInsertWithNullValue() throws SQLException, IllegalArgumentException, IllegalAccessException, MappingException {
		// Setup
		Connection connection = dbEnv.getConnection();
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		connection.setAutoCommit(false);
		
		Person p = new Person();
		p.setAge(423);
		
		// Execute
		sqlInsert.insert(connection, p, "persons");		
		connection.commit();

		// Assert (dbUnit does support direct table comparison rather than querying manually...
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select * from persons");
		List<Person> list = mapper.toPojo(Person.class, resultSet);
		for (Person p1 : list) {
			System.out.println(p1);
		}
		assertNotNull(list);
		assertEquals(3, list.size());
	}
	
	@Test
	public void testInsert3() throws MappingException, SQLException, IllegalArgumentException, IllegalAccessException {
		Connection con = dbEnv.getConnection();
		Person p = new Person();
		p.setAddress(null);
		p.setAge(10);
		p.setName("Hans");
		p.setRandomProperty("ssdj");
		p.setSmoker(true);
		sqlInsert.insert(con, p, "persons");
		System.out.println(p);
		List<Person> all = sqlQuery.getAll(con, Person.class);
		assertEquals(3, all.size());
		// System.out.println(all);
	}

}
