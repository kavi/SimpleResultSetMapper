package dk.javacode.srsm.helpers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dk.javacode.srsm.SqlOrderByColumn.OrderByDirection;
import dk.javacode.srsm.SqlOrderByColumn;
import dk.javacode.srsm.SqlSelectColumn;
import dk.javacode.srsm.SqlSelectColumn.SqlCondition;
import dk.javacode.srsm.SqlSelectColumn.SqlOperator;
import dk.javacode.srsm.descriptors.TableDescriptor;
import dk.javacode.srsm.exceptions.MappingException;

/**
 * Utility class to execute standard SQL queries for mapped classes.
 * 
 * @author Kavi
 */
public class SqlQueryDsl<E> {
	private boolean includeCollections = false;
	private boolean includeReferences = false;
	private Class<E> clazz;
	private TableDescriptor td;
	private List<SqlSelectColumn> conditions = new ArrayList<SqlSelectColumn>();
	private List<SqlOrderByColumn> orderBys = new ArrayList<SqlOrderByColumn>();
	private SqlQueryHelper helper = new SqlQueryHelper();
	
	private Integer limit;
	private Integer offset;

	private SqlQueryDsl(Class<E> clazz) {
		super();
		this.clazz = clazz;
		this.td = TableDescriptor.build(clazz);
	}

	/**
	 * Build a new SqlQuery instance that will map queries to the given class.
	 * 
	 * @param clazz
	 *            The class this query will map to
	 * @return An SqlQuery instance ready to be configured
	 */
	public static <T> SqlQueryDsl<T> build(Class<T> clazz) {
		return new SqlQueryDsl<T>(clazz);
	}

	/**
	 * Adds a where clause to this SqlQuery. (sql +=
	 * " WHERE <column> <operator> <value>")
	 * 
	 * @param column
	 *            The column that must match a value
	 * @param operator
	 *            The binary operator (EQUALS, GREATER_THAN, etc.)
	 * @param value
	 *            The value the column must match.
	 * @return The SqlQuery
	 */
	public SqlQueryDslInner where(String column, SqlOperator operator, Object value) {
		addSelectColumn(SqlCondition.AND, column, operator, value);
		return new SqlQueryDslInner();
	}
	
	/**
	 * Adds a where clause to this SqlQuery using a list operator. (sql +=
	 * " WHERE <column> <operator> (<value>,<value>...)")
	 * 
	 * @param column
	 *            The column that must match a value
	 * @param operator
	 *            The binary operator (EQUALS, GREATER_THAN, etc.)
	 * @param value
	 *            The value the column must match.
	 * @return The SqlQuery
	 */
	public SqlQueryDslInner where(String column, SqlOperator operator, Object[] values) {
		addSelectColumn(SqlCondition.AND, column, operator, values);
		return new SqlQueryDslInner();
	}
	
	private void addSelectColumn(SqlCondition condition, String column, SqlOperator operator, Object[] values) {
		if (!column.contains(".")) {
			column = td.getTableName() + "." + column;
		}
		this.conditions.add(new SqlSelectColumn(condition, column, operator, values));
	}

	private void addSelectColumn(SqlCondition condition, String column, SqlOperator operator, Object value) {
		if (!column.contains(".")) {
			column = td.getTableName() + "." + column;
		}
		this.conditions.add(new SqlSelectColumn(condition, column, operator, value));
	}
	
	private void addOrderByColumn(String column, OrderByDirection direction) {
	    this.orderBys.add(new SqlOrderByColumn(column, direction));
	}

	/**
	 * Configure this SqlQuery to perform eager fetching. That is, include
	 * references and collections.
	 * 
	 * @return This SqlQuery
	 */
	public SqlQueryDsl<E> eager() {
		this.includeReferences = true;
		this.includeCollections = true;
		return this;
	}
	
	public SqlQueryDslLimit limit(int limit) {
		this.limit = limit;
		return new SqlQueryDslLimit();
	}	

	/**
	 * Configure this SqlQuery to include references.
	 * 
	 * @return This SqlQuery
	 */
	public SqlQueryDsl<E> includeReferences() {
		this.includeReferences = true;
		return this;
	}

	/**
	 * Configure this SqlQuery to include collections.
	 * 
	 * @return This SqlQuery
	 */
	public SqlQueryDsl<E> includeCollections() {
		this.includeCollections = true;
		return this;
	}
	
	
	
	/**
	 * Configure this SqlQuery to preserve the order of objects and specify the order by column
	 * 
	 * @param column The column to ORDER BY
	 * @param direction The Direction (Ascending or Descending)
	 * 
	 * @return The SqlQuery
	 */
	public SqlQueryDslOrderBy orderBy(String column, OrderByDirection direction) {
	    addOrderByColumn(column, direction);
	    return new SqlQueryDslOrderBy();
	}

	/**
	 * Execute the query and return the result as a list.
	 * 
	 * @param connection
	 *            The database connection to use for the query (MUST be ready)
	 * @return A list of Java objects mapped from the query.
	 * @throws SQLException
	 *             If a database error occurs.
	 * @throws MappingException
	 *             If a Object Mapping error occurs.
	 */
	public List<E> selectAll(Connection connection) throws SQLException, MappingException {
		return helper.getAll(connection, clazz, includeCollections, includeReferences, limit, offset, orderBys, conditions.toArray(new SqlSelectColumn[0]));
	}

	/**
	 * Execute the query and return the result as a single object. If multiple
	 * results are found an exception is thrown. Useful for cases such as
	 * findById.
	 * 
	 * @param connection
	 *            The database connection to use for the query (MUST be ready)
	 * @return The Java object mapped from the query.
	 * @throws SQLException
	 *             If a database error occurs.
	 * @throws MappingException
	 *             If a Object Mapping error occurs.
	 */
	public E selectOne(Connection connection) throws SQLException, MappingException {
		return helper.getOne(connection, clazz, includeCollections, includeReferences, orderBys, limit, offset, conditions.toArray(new SqlSelectColumn[0]));
	}

	/**
	 * @see SqlQueryDsl
	 */
	public class SqlQueryDslInner {
		private SqlQueryDslInner() {
			super();
		}

		/**
		 * Adds an AND to this query's WHERE clause.
		 * 
		 * @param column
		 *            The column that must match a value
		 * @param operator
		 *            The binary operator (EQUALS, GREATER_THAN, etc.)
		 * @param value
		 *            The value the column must match.
		 * @return The SqlQuery
		 */
		public SqlQueryDslInner and(String column, SqlOperator operator, Object value) {
			addSelectColumn(SqlCondition.AND, column, operator, value);
			return this;
		}
		
		/**
		 * Adds an AND to this query's WHERE clause.
		 * 
		 * @param column
		 *            The column that must match a value
		 * @param operator
		 *            The binary operator (EQUALS, GREATER_THAN, etc.)
		 * @param value
		 *            The value the column must match.
		 * @return The SqlQuery
		 */
		public SqlQueryDslInner and(String column, SqlOperator operator, List<Object> value) {
			addSelectColumn(SqlCondition.AND, column, operator, value);
			return this;
		}

		/**
		 * Adds an OR to this query's WHERE clause.
		 * 
		 * @param column
		 *            The column that must match a value
		 * @param operator
		 *            The binary operator (EQUALS, GREATER_THAN, etc.)
		 * @param value
		 *            The value the column must match.
		 * @return The SqlQuery
		 */
		public SqlQueryDslInner or(String column, SqlOperator operator, Object value) {
			addSelectColumn(SqlCondition.OR, column, operator, value);
			return this;
		}
		
		/**
		 * Adds an OR to this query's WHERE clause.
		 * 
		 * @param column
		 *            The column that must match a value
		 * @param operator
		 *            The binary operator (EQUALS, GREATER_THAN, etc.)
		 * @param value
		 *            The value the column must match.
		 * @return The SqlQuery
		 */
		public SqlQueryDslInner or(String column, SqlOperator operator, List<Object> value) {
			addSelectColumn(SqlCondition.OR, column, operator, value);
			return this;
		}
		
		/**
		 * @see SqlQueryDsl#orderBy(String, OrderByDirection)
		 */
		public SqlQueryDslOrderBy orderBy(String column, OrderByDirection direction) {
		    addOrderByColumn(column, direction);
		    return new SqlQueryDslOrderBy();
		}

		/**
		 * @see SqlQueryDsl#selectAll(Connection)
		 */
		public List<E> selectAll(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectAll(connection);
		}

		/**
		 * @see SqlQueryDsl#selectOne(Connection)
		 */
		public E selectOne(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectOne(connection);
		}
		
		public SqlQueryDslLimit limit(int limit) {
			return SqlQueryDsl.this.limit(limit);
		}
	}
	
	public class SqlQueryDslOrderBy {		
		/**
		 * @see SqlQueryDsl#selectAll(Connection)
		 */
		public List<E> selectAll(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectAll(connection);
		}				
		
		public SqlQueryDslLimit limit(int limit) {
			return SqlQueryDsl.this.limit(limit);
		}				
	}
	
	public class SqlQueryDslLimit {
		
		public SqlQueryDslOffset offset(int offset) {
			SqlQueryDsl.this.offset = offset;
			return new SqlQueryDslOffset();
		}		
		
		/**
		 * @see SqlQueryDsl#selectAll(Connection)
		 */
		public List<E> selectAll(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectAll(connection);
		}
		
		/**
		 * @see SqlQueryDsl#selectOne(Connection)
		 */
		public E selectOne(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectOne(connection);
		}
	}
	
	public class SqlQueryDslOffset{
		/**
		 * @see SqlQueryDsl#selectAll(Connection)
		 */
		public List<E> selectAll(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectAll(connection);
		}
		
		/**
		 * @see SqlQueryDsl#selectOne(Connection)
		 */
		public E selectOne(Connection connection) throws SQLException, MappingException {
			return SqlQueryDsl.this.selectOne(connection);
		}
	}

}
