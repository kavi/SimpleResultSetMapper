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
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.helpers.SqlQueryHelper.SqlSelect;
import dk.javacode.srsm.testmodel.Address;
import dk.javacode.srsm.testmodel.Car;
import dk.javacode.srsm.testmodel.CarType;
import dk.javacode.srsm.testmodel.Person;
import dk.javacode.testutil.DbTestEnvironment;

public class SqlQueryHelperTest {

	private DbTestEnvironment dbEnv = new DbTestEnvironment();
	private ResultSetMapper resultMapper = new ResultSetMapper();
	private SqlQueryHelper sqlQuery = new SqlQueryHelper();

	@Before
	public void setUp() throws Exception {
		dbEnv.setUp();
	}

	@After
	public void tearDown() throws Exception {
		dbEnv.tearDown();
	}

	@Test
	public void testBuildSelectSql() throws MappingException, SQLException {
		SqlSelect sql = sqlQuery.buildSelectSql(Person.class);
		sql.addJoin(Address.class, "id", "address_id");
		// sql.addJoin(Car.class, "id", "car_id", JoinType.LEFT_JOIN);
		// System.out.println(sql.getSelect());

		Connection con = dbEnv.getConnection();
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql.getSelect());
		List<Person> persons = resultMapper.toPojo(Person.class, result);
		assertEquals(2, persons.size());
		// int carCount = 0;
		for (Person p : persons) {
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNotNull(p.getAddress().getIsPublic());
			assertNotNull(p.getAddress().getStreetName());
			// if (p.getCar() != null) carCount++;
			System.out.println(p);
		}
		// assertEquals(1, carCount);
	}
	
	

	@Test
	public void testSelectOneFail() {
		try {
			SqlSelect sql = sqlQuery.buildSelectSql(Person.class);
			Connection connection = dbEnv.getConnection();
			sqlQuery.selectOne(connection, sql, Person.class);
		} catch (MappingException e) {
			assertTrue(e.getMessage().contains("More than one row"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testBuildSelectSqlNormalJoin() throws MappingException, SQLException {
		SqlSelect sql = sqlQuery.buildSelectSql(Person.class);
		sql.addJoin(Address.class, "id", "address_id");
		System.out.println(sql.getSelect());

		Connection con = dbEnv.getConnection();
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql.getSelect());
		List<Person> persons = resultMapper.toPojo(Person.class, result);
		assertEquals(2, persons.size());
		for (Person p : persons) {
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNotNull(p.getAddress().getIsPublic());
			assertNotNull(p.getAddress().getStreetName());
			assertEquals("No join to car", 0, p.getCars().size());
			System.out.println(p);
		}
	}
	
	@Test
	public void testBuildSelectSqlExtendedJoin() throws MappingException, SQLException {
		SqlSelect sql = sqlQuery.buildSelectSql(Person.class);
		sql.addJoin(Car.class, "person_id", "id");
		sql.addJoin(Car.class, CarType.class, "id", "car_type_id");
		sql.addColumn(new SqlQueryHelper.SqlColumn("car", "person_id", "car_person_id", false));
		sql.addColumn(new SqlQueryHelper.SqlColumn("car_type", "make", "car__car_type__make", false));
		sql.addColumn(new SqlQueryHelper.SqlColumn("car_type", "model", "car__car_type__model", false));
		System.out.println(sql.getSelect());

		Connection con = dbEnv.getConnection();
		Statement stmt = con.createStatement();
		ResultSet result = stmt.executeQuery(sql.getSelect());
		List<Person> persons = resultMapper.toPojo(Person.class, result);
		assertEquals(1, persons.size());
		for (Person p : persons) {
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNull(p.getAddress().getIsPublic());
			assertNull(p.getAddress().getStreetName());
			System.out.println(p);
		}
	}

	@Test
	public void testGetAll() throws MappingException, SQLException {
		Connection con = dbEnv.getConnection();
		List<Person> persons = sqlQuery.getAll(con, Person.class);
		assertEquals(2, persons.size());
		for (Person p : persons) {
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNull(p.getAddress().getIsPublic());
			assertNull(p.getAddress().getStreetName());
		}
	}

	@Test
	public void testGetAllById2() throws MappingException, SQLException {
		Connection con = dbEnv.getConnection();
		List<Person> persons = sqlQuery.getAll(con, Person.class, new SqlSelectColumn("id", 2));
		assertEquals(1, persons.size());
		Person p = persons.get(0);
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNull(p.getAddress().getIsPublic());
		assertNull(p.getAddress().getStreetName());
	}

	@Test
	public void testGetOne() throws MappingException, SQLException {
		Connection con = dbEnv.getConnection();
		Person p = sqlQuery.getOne(con, Person.class, new SqlSelectColumn("name", "Admin"));
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNull(p.getAddress().getIsPublic());
		assertNull(p.getAddress().getStreetName());
		// System.out.println(p);
	}


	@Test
	public void testGetOneIncludeRef() throws MappingException, SQLException {
		Connection con = dbEnv.getConnection();
		Car c = sqlQuery.getOne(con, Car.class, false, true, new SqlSelectColumn("car.id", "1"));
		assertNotNull(c);
		System.out.println(c);
	}

}
