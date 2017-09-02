package dk.javacode.srsm.examples.dao;

import static dk.javacode.srsm.SqlSelectColumn.SqlOperator.EQUALS;
import static dk.javacode.srsm.SqlSelectColumn.SqlOperator.GREATER_THAN;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import dk.javacode.srsm.SqlSelectColumn;
import dk.javacode.srsm.SqlSelectColumn.SqlOperator;
import dk.javacode.srsm.converters.BigIntegerLongJdbcConverter;
import dk.javacode.srsm.examples.model.Customer;
import dk.javacode.srsm.examples.model.Order;
import dk.javacode.srsm.examples.model.Product;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.helpers.SqlInsertHelper;
import dk.javacode.srsm.helpers.SqlQueryDsl;
import dk.javacode.srsm.helpers.SqlQueryHelper;

public class CustomerDao {

//	private ResultSetMapper mapper = new ResultSetMapper();
	private SqlQueryHelper queryHelper = new SqlQueryHelper();
	private SqlInsertHelper insertHelper = new SqlInsertHelper();
//	private SqlExecuter<Order> orderExecuter = new SqlExecuter<Order>(Order.class);
	
	public CustomerDao() {
	}

	
//	public List<Customer> getCustomersPlainJdbc(Connection connection) throws SQLException {
//		String sql = "select * from customer";
//		PreparedStatement stmt = connection.prepareStatement(sql);
//		ResultSet resultSet = stmt.executeQuery();
//		List<Customer> customers = new ArrayList<Customer>();
//		while (resultSet.next()) {
//			Customer c = new Customer();
//			c.setActive(resultSet.getBoolean("active"));
//			c.setId(resultSet.getLong("id"));
//			c.setJoined(resultSet.getDate("joined"));
//			c.setName(resultSet.getString("name"));
//			customers.add(c);
//		}
//		return customers;
//	}

	public List<Customer> getCustomers(Connection connection) throws MappingException, SQLException {
//		SqlQueryDsl.build(Customer.class)
//			.selectAll(connection);
		return queryHelper.getAll(connection, Customer.class);
	}	
	
	public Customer findCustomerById(Connection connection, int id) throws MappingException, SQLException {
//		SqlQueryDsl.build(Customer.class)
//			.eager()
//			.where("id", EQUALS, id)
//			.selectOne(connection);
		Customer c = queryHelper.getOne(connection, Customer.class, true, true, new SqlSelectColumn("customer.id", id));
		return c;
	}
	
	public List<Order> getOrders(Connection connection) throws MappingException, SQLException {
//		SqlQueryDsl.build(Order.class).selectAll(connection);
		return queryHelper.getAll(connection, Order.class, false);
	}
	
	public List<Order> getOrdersFull(Connection connection) throws MappingException, SQLException {
//		SqlQueryDsl.build(Order.class).includeReferences().selectAll(connection);
		return queryHelper.getAll(connection, Order.class, true);
	}
	
	public List<Order> getOrdersOverPrice(Connection connection, float price) throws MappingException, SQLException {
		return SqlQueryDsl.build(Order.class).includeReferences()
				.where("price", GREATER_THAN, price)
				.selectAll(connection);
	}
	
	public Order getOrderById(Connection connection, int id) throws MappingException, SQLException {
		return SqlQueryDsl.build(Order.class)
				.includeReferences()
				.where("id", EQUALS, id)
				.selectOne(connection);
	}
	
	public List<Order> getOrderByPriceOrId(Connection connection, float price, int id) throws MappingException, SQLException {
		return SqlQueryDsl.build(Order.class)
				.includeReferences()
				.where("price", SqlOperator.GREATER_THAN, price)
				.or("id", SqlOperator.EQUALS, id)
				.selectAll(connection);
	}

	public List<Order> getOrderByPriceAndName(Connection connection, float price, String name) throws MappingException, SQLException {
		return SqlQueryDsl.build(Order.class)
				.includeReferences()
				.where("price", GREATER_THAN, price)
				.and("product.name", EQUALS, name)
				.selectAll(connection);
	}

	public void createCustomer(Connection connection, Customer c) throws SQLException, MappingException {
		insertHelper.insert(connection, c, "customer", new BigIntegerLongJdbcConverter());
	}


	public void createProduct(Connection connection, Product product) throws SQLException, MappingException {
		insertHelper.insert(connection, product, "product");
	}	
}
