package dk.javacode.srsm.converters;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.exceptions.MappingRuntimeException;

public class BooleanCharJdbcConverterTest {

	private BooleanCharJdbcConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new BooleanCharJdbcConverter();
	}

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void testConvertNullToPojo() {
		Boolean aspojo = converter.convertToPojo(null);
		assertNull(aspojo);
	}


	@Test
	public void testConvertCharToPojo() {
		Boolean aspojo = converter.convertToPojo('y');
		assertEquals(true, aspojo);
		aspojo = converter.convertToPojo('Y');
		assertEquals(true, aspojo);

		aspojo = converter.convertToPojo('n');
		assertEquals(false, aspojo);

		aspojo = converter.convertToPojo('N');
		assertEquals(false, aspojo);
	}

	@Test
	public void testConvertStringToPojo() {
		Boolean aspojo = converter.convertToPojo("y");
		assertEquals(true, aspojo);
		aspojo = converter.convertToPojo("Y");
		assertEquals(true, aspojo);

		aspojo = converter.convertToPojo("n");
		assertEquals(false, aspojo);

		aspojo = converter.convertToPojo("N");
		assertEquals(false, aspojo);
	}

	@Test
	public void testConvertInvalidStringToPojo() {
		try {
			converter.convertToPojo("yes");
			fail("Should fail");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testConvertInvalidCharToPojo() {
		try {
			converter.convertToPojo('J');
			fail("Should fail");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}

	}

	@Test
	public void testConvertNumberToPojoFails() {
		try {
			converter.convertToPojo(1);
			fail("Should fail");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testPojoToDatabase() {
		char dbobject = (Character) converter.pojoToDatabase(true);
		assertEquals('Y', dbobject);
		
		dbobject = (Character) converter.pojoToDatabase(false);
		assertEquals('N', dbobject);

		Object dbnull = converter.pojoToDatabase(null);
		assertNull(dbnull);
	}

}
