package dk.javacode.srsm.examples.sqlexecuter;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Types;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.helpers.DefaultSqlTypeConverter;
import dk.javacode.srsm.testmodel.Person;

public class DefaultSqlTypeConverterTest {

	private DefaultSqlTypeConverter converter;
	
	@Before
	public void setUp() throws Exception {
		converter = new DefaultSqlTypeConverter();
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testGetType() {
		assertEquals(Types.BIT, converter.getSqlType(true));
		assertEquals(Types.TINYINT, converter.getSqlType((byte) 0));
		assertEquals(Types.SMALLINT, converter.getSqlType((short) 0));
		assertEquals(Types.INTEGER, converter.getSqlType(0));
		assertEquals(Types.BIGINT, converter.getSqlType(0L));
		assertEquals(Types.FLOAT, converter.getSqlType(0f));
		assertEquals(Types.DOUBLE, converter.getSqlType(0.0));
		assertEquals(Types.VARCHAR, converter.getSqlType("a"));
		assertEquals(Types.CHAR, converter.getSqlType('a'));
		assertEquals("Numeric", Types.NUMERIC, converter.getSqlType(new BigDecimal(0)));
		assertEquals(Types.OTHER, converter.getSqlType(new Person()));
		assertEquals(Types.DATE, converter.getSqlType(new java.util.Date()));
		assertEquals("Binary (byte)", Types.BINARY, converter.getSqlType(new byte[0]));
		assertEquals("Binary (Byte)", Types.BINARY, converter.getSqlType(new Byte[0]));
	}

}
