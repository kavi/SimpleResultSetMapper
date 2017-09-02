package dk.javacode.srsm;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.testmodel.Person;
import dk.javacode.testutil.DbTestEnvironment;

public class ResultSetMapperTest {

	private ResultSetMapper mapper;

	private DbTestEnvironment dbEnv = new DbTestEnvironment();

	@Before
	public void setUp() throws Exception {
		dbEnv.setUp();
		mapper = new ResultSetMapper();
	}

	@After
	public void tearDown() throws Exception {
		dbEnv.tearDown();
	}

	
	@Test
	public void testToPojo() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select persons.id, smoker, name, age from persons");
		List<Person> list = mapper.toPojo(Person.class, resultSet);
		for (Person p : list) {
			assertNotNull("Person entity", p);
			assertNotNull("Name", p.getName());
			assertNotNull("Smoker", p.isSmoker());
			assertNotNull("Age", p.getAge());
			assertNotEquals("randomProperty", p.getRandomProperty());
			assertNull("Expect null - address id not in selected columns", p.getAddress());
		}
		assertNotNull(list);
		assertEquals(2, list.size());
	}
	
	
	@Test
	public void testMapWithJoin() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select persons.id, smoker, name, age, persons.address_id, c.person_id as car_person_id, c.id as car__id, a.street_name as address__street_name, a.is_public as address__is_public, c.registration as car__registration from persons join address a on a.id = persons.address_id left join car c on c.person_id = persons.id");
		List<Person> list = mapper.toPojo(Person.class, resultSet);
		assertNotNull(list);
		for (Person p : list) {
			assertNotNull("Name", p.getName());
			assertNotNull("Smoker", p.isSmoker());
			assertNotNull("Age", p.getAge());
			assertNotNull("Address", p.getAddress());
			assertNotNull("Address.streetname", p.getAddress().getStreetName());
			if (p.getId().equals(1)) {
				assertEquals("Car list size 2", 2, p.getCars().size());
				assertEquals("car.registration", "QX94321", p.getCars().get(0).getRegistration());
			} else {
				assertEquals("Car list empty", 0, p.getCars().size());
			}
		}
		assertEquals(2, list.size());
	}
	
	@Test
	public void testMapEmptyResultSet() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select * from persons where name = 'not in database'");
		List<Person> list = mapper.toPojo(Person.class, resultSet);
		assertNotNull(list);
		assertEquals(0, list.size());
	}	
	
	@Test
	public void testMapEmptyResultSetToSingle() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery("select * from persons where name = 'not in database'");
		Person single = mapper.getSinglePojo(Person.class, resultSet);
		assertNull(single);
	}	
}
