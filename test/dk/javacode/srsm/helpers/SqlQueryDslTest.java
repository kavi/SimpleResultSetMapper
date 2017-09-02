package dk.javacode.srsm.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.SqlOrderByColumn.OrderByDirection;
import dk.javacode.srsm.SqlSelectColumn.SqlOperator;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.testmodel.Address;
import dk.javacode.srsm.testmodel.Person;
import dk.javacode.testutil.DbTestEnvironment;

public class SqlQueryDslTest
{

	private DbTestEnvironment dbEnv = new DbTestEnvironment();

	@Before
	public void setUp() throws Exception
	{
		dbEnv.setUp();
	}

	@After
	public void tearDown() throws Exception
	{
		dbEnv.tearDown();
	}

	@Test
	public void testLimit() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).limit(1).selectAll(connection);

		assertEquals(1, persons.size());
		for (Person p : persons)
		{
			assertNotNull(p);
			assertEquals(1, (int) p.getId());
			System.out.println(p);
		}
	}

	@Test
	public void testLimitOffset() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).limit(1).offset(1).selectAll(connection);

		assertEquals(1, persons.size());
		for (Person p : persons)
		{
			assertNotNull(p);
			assertEquals(2, (int) p.getId());
			System.out.println(p);
		}
	}

	@Test
	public void testEager() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).eager().selectAll(connection);

		assertEquals(2, persons.size());
		for (Person p : persons)
		{
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNotNull(p.getAddress().getIsPublic());
			assertNotNull(p.getAddress().getStreetName());

			System.out.println(p);
		}
	}

	@Test
	public void testAddress() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Address> address = SqlQueryDsl.build(Address.class).where("id", SqlOperator.EQUALS, 1).selectAll(connection);

		assertEquals(1, address.size());
		Address a = address.get(0);
		assertEquals((long) 1, (long) a.getId2());
		System.out.println(a);
	}

	@Test
	public void testWhere() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).where("id", SqlOperator.EQUALS, 1).selectAll(connection);

		assertEquals(1, persons.size());
		Person p = persons.get(0);
		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNull(p.getAddress().getIsPublic());
		assertNull(p.getAddress().getStreetName());
	}

	@Test
	public void testWhereIn() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Integer> ids = new ArrayList<>();
		ids.add(1);
		ids.add(2);
		List<Person> persons = SqlQueryDsl.build(Person.class).where("id", SqlOperator.IN, ids.toArray()).selectAll(connection);

		assertEquals(2, persons.size());
		Person p = persons.get(0);
		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNull(p.getAddress().getIsPublic());
		assertNull(p.getAddress().getStreetName());
	}

	@Test
	public void testWhereInNotSupportedForSelectOne() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Integer> ids = new ArrayList<>();
		ids.add(1);
		ids.add(2);

		try
		{
			SqlQueryDsl.build(Person.class).where("id", SqlOperator.IN, ids.toArray()).selectOne(connection);
			fail("Should throw exception");
		}
		catch (UnsupportedOperationException e)
		{
			assertTrue(true);
		}
	}

	@Test
	public void testWhereOr() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).eager()
						.where("id", SqlOperator.EQUALS, 2)
						.or("name", SqlOperator.EQUALS, "Admin")
						.selectAll(connection);

		assertEquals(2, persons.size());
		for (Person p : persons)
		{
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNotNull(p.getAddress().getIsPublic());
			assertNotNull(p.getAddress().getStreetName());

			System.out.println(p);
		}
	}

	@Test
	public void testOrderBy() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).eager()
						.where("id", SqlOperator.EQUALS, 2)
						.or("name", SqlOperator.EQUALS, "Admin")
						.orderBy("id", OrderByDirection.DESC)
						.selectAll(connection);

		assertEquals(2, persons.size());
		assertEquals((Integer) 2, persons.get(0).getId());
		assertEquals((Integer) 1, persons.get(1).getId());
		for (Person p : persons)
		{
			assertNotNull(p);
			assertNotNull(p.getAddress());
			assertNotNull(p.getAddress().getId2());
			assertNotNull(p.getAddress().getIsPublic());
			assertNotNull(p.getAddress().getStreetName());

			System.out.println(p);
		}

	}

	@Test
	public void testOrderByDescSelectOne() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		Person p = SqlQueryDsl.build(Person.class).eager()
						.where("id", SqlOperator.EQUALS, 2)
						.or("name", SqlOperator.EQUALS, "Admin")
						.orderBy("id", OrderByDirection.DESC)
						.limit(1)
						.selectOne(connection);

		assertNotNull(p);
		assertEquals(2, (int) p.getId()); 
	}
	
	@Test
	public void testOrderByAscSelectOne() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		Person p = SqlQueryDsl.build(Person.class).eager()
						.where("id", SqlOperator.EQUALS, 2)
						.or("name", SqlOperator.EQUALS, "Admin")
						.orderBy("id", OrderByDirection.ASC)
						.limit(1)
						.selectOne(connection);

		assertNotNull(p);
		assertEquals(1, (int) p.getId()); 
	}

	@Test
	public void testWhereOrSelectOne() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		Person person = SqlQueryDsl.build(Person.class).eager()
						.where("id", SqlOperator.EQUALS, 7123)
						.or("name", SqlOperator.EQUALS, "Admin")
						.selectOne(connection);

		System.out.println(person);

		assertNotNull(person);
		assertNotNull(person);
		assertNotNull(person.getAddress());
		assertNotNull(person.getAddress().getId2());
		assertNotNull(person.getAddress().getIsPublic());
		assertNotNull(person.getAddress().getStreetName());

	}

	@Test
	public void testWhereAnd() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).eager()
						.includeReferences()
						.where("address.street_name", SqlOperator.LIKE, "Falsters%")
						.and("name", SqlOperator.EQUALS, "Admin")
						.selectAll(connection);

		assertEquals(1, persons.size());
		Person p = persons.get(0);
		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNotNull(p.getAddress().getIsPublic());
		assertNotNull(p.getAddress().getStreetName());

	}

	@Test
	public void testIncludeCollection() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		List<Person> persons = SqlQueryDsl.build(Person.class).eager()
						.where("car.registration", SqlOperator.LIKE, "AB%")
						.selectAll(connection);

		assertEquals(1, persons.size());
		Person p = persons.get(0);
		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNotNull(p.getAddress().getIsPublic());
		assertNotNull(p.getAddress().getStreetName());
		assertEquals(1, p.getCars().size());
	}

	@Test
	public void testSelectOne() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		Person p = SqlQueryDsl.build(Person.class).eager()
						.where("car.registration", SqlOperator.LIKE, "AB%")
						.selectOne(connection);

		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNotNull(p.getAddress().getIsPublic());
		assertNotNull(p.getAddress().getStreetName());

	}

	@Test
	public void testSelectOneInner() throws MappingException, SQLException
	{
		Connection connection = dbEnv.getConnection();
		Person p = SqlQueryDsl.build(Person.class).eager()
						.includeCollections()
						.where("car.registration", SqlOperator.LIKE, "AB%")
						.and("name", SqlOperator.EQUALS, "Admin")
						.selectOne(connection);

		assertEquals((int) 1, (int) p.getId());
		assertEquals("Admin", p.getName());
		assertNotNull(p);
		assertNotNull(p.getAddress());
		assertNotNull(p.getAddress().getId2());
		assertNotNull(p.getAddress().getIsPublic());
		assertNotNull(p.getAddress().getStreetName());

	}

}
