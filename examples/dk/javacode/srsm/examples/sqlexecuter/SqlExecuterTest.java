package dk.javacode.srsm.examples.sqlexecuter;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.examples.sqlexecuter.SqlExecuter.TypedSqlValue;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.testmodel.Person;
import dk.javacode.testutil.DbTestEnvironment;

public class SqlExecuterTest {

	private SqlExecuter<Person> executer;
	private DbTestEnvironment dbEnv = new DbTestEnvironment();

	@Before
	public void setUp() throws Exception {
		dbEnv.setUp();
		executer = new SqlExecuter<Person>(Person.class);
	}

	@After
	public void tearDown() throws Exception {
		dbEnv.tearDown();
	}

	@Test
	public void testToPreparedSql() {
		Map<String, Object> params = new HashMap<String, Object>();
		String select = "select * from mytable where id = :id and name = :name";
		params.put("id", 1);
		params.put("name", "test");
		SqlExecuter<Person>.PreparedSql preparedSql = executer.toPreparedSql(select, params);

		assertEquals(2, preparedSql.getParams().size());
		TypedSqlValue param0 = (TypedSqlValue) preparedSql.getParams().get(0);
		TypedSqlValue param1 = (TypedSqlValue) preparedSql.getParams().get(1);
		assertEquals(params.get("id"), param0.getValue());
		assertEquals(params.get("name"), param1.getValue());
		assertEquals(Types.INTEGER, param0.getSqlType());
		assertEquals(Types.VARCHAR, param1.getSqlType());
		assertEquals("select * from mytable where id = ? and name = ?", preparedSql.getSql());
	}
//		
	@Test
	public void testExecuteUsingIntParam() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();

		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "select * from persons where persons.id = :id";
		params.put("id", 2);
		List<Person> list = executer.executeQuery(connection, sql, params);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("User", list.get(0).getName());
	}

	@Test
	public void testExecuteUsingStringParam() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();

		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "select * from persons where persons.name = :name";
		params.put("name", "User");
		List<Person> list = executer.executeQuery(connection, sql, params);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("User", list.get(0).getName());
	}
	
	@Test
	public void testExecuteUsingBooleanParam() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();

		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "select * from persons where persons.smoker = :smoker";
		params.put("smoker", true);
		List<Person> list = executer.executeQuery(connection, sql, params);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("Admin", list.get(0).getName());
	}
	
	@Test
	public void testExecuteUsingMultipleParams() throws SQLException, MappingException {
		Connection connection = dbEnv.getConnection();

		Map<String, Object> params = new HashMap<String, Object>();		
		String sql = "select * from persons where persons.id = :id and persons.name = :name and persons.smoker = :smoker";
		params.put("id", 2);
		params.put("name", "User");
		params.put("smoker", false);
		List<Person> list = executer.executeQuery(connection, sql, params);
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals("User", list.get(0).getName());
	}

}
