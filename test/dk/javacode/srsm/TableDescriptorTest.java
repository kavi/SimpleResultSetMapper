package dk.javacode.srsm;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.descriptors.TableDescriptor;
import dk.javacode.srsm.exceptions.MappingRuntimeException;
import dk.javacode.srsm.testmodel.Person;

public class TableDescriptorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildFromMultipleThreads() throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		List<Runner> runners = new ArrayList<TableDescriptorTest.Runner>();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			Runner r = new Runner();
			Thread t = new Thread(r);
			t.start();
			runners.add(r);
			threads.add(t);
		}
		for (Thread t : threads) {
			t.join();
		}
		long end = System.currentTimeMillis();
		TableDescriptor expected = runners.get(0).td;
		for (Runner r : runners) {
			assertNotNull(r.td);
			assertNull(r.error);
			assertSame(expected, r.td);
		}
		System.out.println("Build time: " + (end - start) + "ms");
		threads.clear();
		runners.clear();
		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			Runner r = new Runner();
			Thread t = new Thread(r);
			t.start();
			runners.add(r);
			threads.add(t);
		}
		for (Thread t : threads) {
			t.join();
		}
		end = System.currentTimeMillis();
		for (Runner r : runners) {
			assertNotNull(r.td);
			assertNull(r.error);
			assertSame(expected, r.td);
		}
	}
	
	@Test
	public void testFailsOnNoSetter() {
		try {
			TableDescriptor.build(NoSetterMethod.class);
			fail("Should throw exception");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}
	}	
	
	@Test
	public void testFailsOnPrivateSetter() {
		try {
			TableDescriptor.build(PrivateSetterMethod.class);
			fail("Should throw exception");
		} catch (MappingRuntimeException e) {
			assertTrue(true);
		}
	}

	public static class Runner implements Runnable {
		TableDescriptor td = null;
		Exception error = null;

		@Override
		public void run() {
			try {
				td = TableDescriptor.build(Person.class);
				if (td.getTableName() == null) {
					error = new Exception("Table name not set");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Table(name = "test")
	public static class NoSetterMethod {
		@Column(name = "test")
		private int noSetter;
		
		
	}
	
	@Table(name = "test")
	public static class PrivateSetterMethod {
		@Column(name = "test")
		private int noSetter;

		@SuppressWarnings("unused")
		private void setNoSetter(int noSetter) {
			this.noSetter = noSetter;
		}
		
		
	}
	
}
