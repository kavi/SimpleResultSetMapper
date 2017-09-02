package dk.javacode.srsm.converters;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.exceptions.MappingRuntimeException;

public class BooleanIntJdbcConverterTest {

	private BooleanIntJdbcConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new BooleanIntJdbcConverter();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertIntToPojo() {
		Boolean pojo = converter.convertToPojo(1);
		assertEquals(true, pojo);

		pojo = converter.convertToPojo(-1);
		assertEquals(true, pojo);

		pojo = converter.convertToPojo(0);
		assertEquals(false, pojo);
	}

	@Test
	public void testConvertLongToPojo() {
		Boolean pojo = converter.convertToPojo(1L);
		assertEquals(true, pojo);

		pojo = converter.convertToPojo(-1L);
		assertEquals(true, pojo);

		pojo = converter.convertToPojo(0L);
		assertEquals(false, pojo);
	}

	@Test
	public void testConvertStringToPojoFails() {
		try {
			converter.convertToPojo("1");
			fail("Should throw exception");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testConvertNullToPojo() {
		Boolean pojo = converter.convertToPojo(null);
		assertNull(pojo);
	}

	@Test
	public void testPojoToDatabase() {
		Integer dbobject = (Integer) converter.pojoToDatabase(true);
		assertEquals(1, (int) dbobject);

		dbobject = (Integer) converter.pojoToDatabase(false);
		assertEquals(0, (int) dbobject);

		assertNull(converter.pojoToDatabase(null));
	}

}
