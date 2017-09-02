package dk.javacode.srsm.examples;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.javacode.srsm.SqlOrderByColumn.OrderByDirection;
import dk.javacode.srsm.SqlSelectColumn.SqlOperator;
import dk.javacode.srsm.converters.BigIntegerIntJdbcConverter;
import dk.javacode.srsm.converters.DefaultJdbcDataConverter;
import dk.javacode.srsm.examples.dao.CustomerDao;
import dk.javacode.srsm.examples.dao.DbConnectionHandler;
import dk.javacode.srsm.examples.model.Customer;
import dk.javacode.srsm.examples.model.Order;
import dk.javacode.srsm.examples.model.Product;
import dk.javacode.srsm.helpers.SqlBatchInsertHelper;
import dk.javacode.srsm.helpers.SqlInsertHelper;
import dk.javacode.srsm.helpers.SqlQueryDsl;

public class LoadCustomersExample {

	private DbConnectionHandler connectionHandler;
	
	private int dbPort = 3306;
	private String dbHost = "127.0.0.1";
	private String dbUser = "test";
	private String dbPass = "1234";
	private String dbName = "srsm_test";
	
	@BeforeClass
	public static void before() {
		Logger.getRootLogger().removeAllAppenders();
		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		String PATTERN = "%d [%p|%c] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}

	@Before
	public void setUp() throws Exception {
		connectionHandler = new DbConnectionHandler(dbPort, dbHost, dbUser, dbPass, dbName);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetCustomers() throws SQLException {
		CustomerDao dao = new CustomerDao();
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			List<Customer> customers = dao.getCustomers(connection);
			for (Customer c : customers) {
				System.out.println(c);
			}

			Customer customer = dao.findCustomerById(connection, 1);
			assertEquals(3, customer.getOrderList().size());
			System.out.println(customer);

			customer = dao.findCustomerById(connection, 2);
			assertEquals(2, customer.getOrderList().size());
			System.out.println(customer);

		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}

	@Test
	public void testGetOrders() throws SQLException {
		CustomerDao dao = new CustomerDao();
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();

			List<Order> orders = dao.getOrders(connection);
			for (Order o : orders) {
				System.out.println(o);
			}
			System.out.println("********\n");
			orders = dao.getOrdersFull(connection);
			for (Order o : orders) {
				System.out.println(o);
			}

			System.out.println("********\n");
			orders = dao.getOrdersOverPrice(connection, 50f);
			for (Order o : orders) {
				System.out.println(o);
			}

			System.out.println("********\n");
			Order order = dao.getOrderById(connection, 2);
			System.out.println(order);

			System.out.println("********\n");
			orders = dao.getOrderByPriceOrId(connection, 50f, 2);
			for (Order o : orders) {
				System.out.println(o);
			}

			System.out.println("********\n");
			orders = dao.getOrderByPriceAndName(connection, 50f, "Cookies");
			for (Order o : orders) {
				System.out.println(o);
			}
		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}

	@Test
	public void testInsertCustomer() throws SQLException {
		CustomerDao dao = new CustomerDao();
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			Customer other = new Customer();
			String name = "..";
			for(int i = 0;other != null;i++) {
				name = "Carl" + i;
				other = SqlQueryDsl.build(Customer.class).where("name", SqlOperator.EQUALS, name).selectOne(connection);
			}
			Customer customer = new Customer();
			customer.setPassword("testing");
			customer.setActive(true);
			customer.setName(name);
			dao.createCustomer(connection, customer);

			System.out.println("Customer id: " + customer.getId());

		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}
	
	@Test
	public void testInsertProduct() throws SQLException {
		CustomerDao dao = new CustomerDao();
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			Product other = new Product();
			String name = "..";
			for(int i = 0;other != null;i++) {
				name = "Grazers" + i;
				other = SqlQueryDsl.build(Product.class).where("name", SqlOperator.EQUALS, name).selectOne(connection);
			}
			Product product = new Product();
			product.setName(name);
			dao.createProduct(connection, product);

			System.out.println("Product id: " + product.getId());

		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}
	
	@Test
	public void testInsertProducts() throws SQLException {
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			Product other = new Product();
			String name = "..";
			int i = 1;
			for(;other != null;i++) {
				name = "Grazers" + i;
				other = SqlQueryDsl.build(Product.class).where("name", SqlOperator.EQUALS, name).selectOne(connection);
			}
			i = i - 1;
			System.out.println(i);
			List<Product> products = new ArrayList<Product>();
			for (int j = i;j < i + 10;j++) {
				Product product = new Product();
				product.setName("Grazers" + j);
				products.add(product);
			}
//			dao.createProduct(connection, product);
			SqlBatchInsertHelper batch = new SqlBatchInsertHelper();
			batch.insertList(connection, products, new DefaultJdbcDataConverter());
//			System.out.println("Product id: " + product.getId());

		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}
	
	@Test
	public void testInsertOrders() throws SQLException {
		SqlBatchInsertHelper batchInserter = new SqlBatchInsertHelper();
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			Product cookies = SqlQueryDsl.build(Product.class).where("name", SqlOperator.EQUALS, "Cookies").selectOne(connection);
			Customer customer1 =  SqlQueryDsl.build(Customer.class).where("id", SqlOperator.EQUALS, 1).selectOne(connection);
			
			System.out.println("P: " + cookies + ", C: " + customer1);
			List<Order> orders = new ArrayList<Order>();
			for (int i = 0;i < 10;i++) {
				Order o = new Order();
				o.setCustomer(customer1);
				o.setProduct(cookies);
				o.setPrice(10);
				orders.add(o);
			}
			batchInserter.insertList(connection, orders, new BigIntegerIntJdbcConverter());
		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
		assertTrue("Look at sysout - this is NOT an automated unit test", true);
	}
	
	@Test
	public void testOrderBy() throws SQLException {
		Connection connection = null;
		try {
			connection = connectionHandler.getConnection();
			
//			List<Product> products = SqlQueryDsl.build(Product.class).selectAll(connection);
//			System.out.println(products);
			
			List<?> products = SqlQueryDsl.build(Product.class).orderBy("name", OrderByDirection.ASC).selectAll(connection);
			System.out.println(products);


		} catch (Exception e) {
			if (connection != null) {
				connection.close();
			}
			throw new RuntimeException(e);
		}
	}
}
