package dk.javacode.srsm.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.javacode.srsm.ResultSetMapper;
import dk.javacode.srsm.SqlOrderByColumn;
import dk.javacode.srsm.SqlSelectColumn;
import dk.javacode.srsm.descriptors.ColumnDescriptor;
import dk.javacode.srsm.descriptors.TableDescriptor;
import dk.javacode.srsm.exceptions.MappingException;

/**
 * Utility class to execute standard SQL queries for mapped classes.
 * 
 * @author Kavi
 */
public class SqlQueryHelper {

	private static final Logger querylog = LoggerFactory.getLogger("dk.javacode.srsm.query");

	/**
	 * Build an SqlSelect object that can be manipulated to add extra columns,
	 * joins etc. Can be used to generate plain SQL which can then be altered
	 * and executed using for example a prepared statement.
	 * 
	 * @param clazz
	 *            The class to base the SQL on.
	 * @return An SqlSelect object which will select columns matching mapped
	 *         properties on clazz.
	 * @throws MappingException
	 */
	public SqlSelect buildSelectSql(Class<?> clazz) throws MappingException {
		return buildSelectSql(clazz, false, false);
	}

	/**
	 * Builds a select statement from the given class, using the annotations on
	 * the class. On the returned SqlSelect you can add columns and join with
	 * other tables. Set includeCollection to false to exclude collections from
	 * the insert. Including collections will automatically add a join.
	 * Referenced objects (mapped by ColumnReference) will not be joined
	 * automatically but can be added manually on the SqlSelect object.
	 * 
	 * The depth of joins is currently limited to 1. Ie. a Customer class with a
	 * reference to an Order will only join with the order table, not with any
	 * tables Order might reference.
	 * 
	 * @param clazz
	 * @return
	 * @throws MappingException
	 */
	public SqlSelect buildSelectSql(Class<?> clazz, boolean includeCollection, boolean includeReferences) throws MappingException {
		TableDescriptor tableDescriptor = TableDescriptor.build(clazz);
		SqlSelect sql = new SqlSelect(tableDescriptor.getTableName());
		for (ColumnDescriptor cd : tableDescriptor.getColumnDescriptors()) {
			if (cd.isWriteOnly()) {
				continue;
			}
			if (!cd.isCollection()) {
				sql.addColumn(cd.getColumnName());
				if (includeReferences && cd.getFieldReference() != null) {
					TableDescriptor tdRef = TableDescriptor.build(cd.getMappedType());
					sql.addJoin(cd.getMappedType(), tdRef.getPrimaryKeyColumn().getColumnName(), cd.getColumnName(), JoinType.LEFT_JOIN);
				}
			} else if (includeCollection) {
				TableDescriptor refTd = TableDescriptor.build(cd.getCollectionType());
//				System.out.println(cd.getColumnName() + ": " + cd.getFieldReference());
				String columnReference = cd.getColumnReference();
				sql.addJoin(cd.getCollectionType(), columnReference, tableDescriptor.getPrimaryKeyColumn().getColumnName(), JoinType.LEFT_JOIN);
				sql.addColumn(new SqlColumn(refTd.getTableName(), columnReference, cd.getColumnName(), false));
			}
		}
		return sql;
	}

	/**
	 * Build default Sql for the given class and select all objects matching the
	 * SqlSelectColumns given. SqlSelectColumn are AND based - that is only rows
	 * matching all sqlSelectColumns are returned.
	 * 
	 * @param connection
	 *            The connection to use
	 * @param clazz
	 *            The type
	 * @param columns
	 *            The Column to match on in the WHERE clause
	 * @return A list of POJOs from the table
	 * @throws MappingException
	 * @throws SQLException
	 */
	public <E> List<E> selectAll(Connection connection, Class<E> clazz, SqlSelectColumn... columns) throws MappingException, SQLException {
		return selectAll(connection, buildSelectSql(clazz), clazz, columns);
	}

	public <E> List<E> selectAll(Connection connection, SqlSelect sql, Class<E> clazz, SqlSelectColumn... columns) throws MappingException, SQLException {
		return selectAll(connection, sql, clazz, null, null, null, columns);
	}

	public <E> List<E> selectAll(Connection connection, SqlSelect sql, Class<E> clazz, List<SqlOrderByColumn> orderbys, Integer limit, Integer offset, SqlSelectColumn... columns)
			throws MappingException, SQLException {
		String select = sql.getSelect();
		if (columns.length > 0) {
			boolean first = true;
			String condition = "";
			for (SqlSelectColumn c : columns) {
				condition = c.getPreviousCondition().getValue();
				if (first) {
					select += " WHERE";
					first = false;
				} else {
					select += " " + condition;
				}
				if (c.getOperator().isListOperator()) {
					select += " " + c.getColumn() + " " + c.getOperator().getValue() + " ( ";
					if (c.getListValue().length > 0) {
						select += " ?";
					}
					for (int i = 1; i < c.getListValue().length;i++) {
						select += ", ?";
					}					
					select += " )";
				} else {
					select += " " + c.getColumn() + " " + c.getOperator().getValue() + " ?";
				}
			}
		}
		boolean preserveOrder = false;
		if (orderbys != null && orderbys.size() > 0) {
			preserveOrder = true;
			select += " ORDER BY ";
			for (SqlOrderByColumn o : orderbys) {
				select += o.getColumn() + " " + o.getDirection().getValue() + " ";
			}
		}
		if (limit != null) {
			select += " LIMIT ?";
			if (offset != null) {
				select += " OFFSET ?";
			}
		}
//		System.out.println(select);
		querylog.debug(select);
		PreparedStatement stmt = connection.prepareStatement(select);
		int p = 1;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].getOperator().isListOperator()) {
				for (int j = 0;j < columns[i].getListValue().length;j++) {
					stmt.setObject(p, columns[i].getListValue()[j]);
					p++;
				}
			} else {
				stmt.setObject(p, columns[i].getValue());
			}
			p++;
		}
		if (limit != null) {
			stmt.setInt(p, limit);
			p++;
			if (offset != null) {
				stmt.setInt(p, offset);
				p++;
			}
		}
		ResultSet result = stmt.executeQuery();
		ResultSetMapper mapper = new ResultSetMapper();
		return mapper.toPojo(clazz, result, preserveOrder);
	}

	/**
	 * Get all instances of the given class using the database connection given.
	 * Note that referenced classes are NOT retrieved. I.e. a so called lazy
	 * fetch strategy.
	 * 
	 * Use getAll with includeReferences := true to perform Eager fetching.
	 * 
	 * @param connection
	 *            The database connection to use for the query.
	 * @param clazz
	 *            The class to fetch instances of.
	 * @return A list of instances retrieved from the database.
	 * @throws MappingException
	 *             If unable to map the result set.
	 * @throws SQLException
	 *             If JDBC throws a SQLException.
	 */
	public <E> List<E> getAll(Connection connection, Class<E> clazz, SqlSelectColumn... columns) throws MappingException, SQLException {
		return getAll(connection, clazz, false, false, columns);
	}

	/**
	 * Get all instances of the given class using the database connection given.
	 * Note that referenced classes are NOT retrieved. I.e. a so called lazy
	 * fetch strategy.
	 * 
	 * @param connection
	 *            The database connection to use for the query.
	 * @param clazz
	 *            The class to fetch instances of.
	 * @param includeReferences
	 *            Fetch collections and references as well
	 * @return A list of instances retrieved from the database.
	 * @throws MappingException
	 *             If unable to map the result set.
	 * @throws SQLException
	 *             If JDBC throws a SQLException.
	 */
	public <E> List<E> getAll(Connection connection, Class<E> clazz, boolean includeReferences, SqlSelectColumn... columns) throws MappingException,
			SQLException {
		return getAll(connection, clazz, includeReferences, includeReferences, columns);
	}

	/**
	 * Get all instances of the given class using the database connection given.
	 * Note that referenced classes are NOT retrieved. I.e. a so called lazy
	 * fetch strategy.
	 * 
	 * @param connection
	 *            The database connection to use for the query.
	 * @param clazz
	 *            The class to fetch instances of.
	 * @param includeCollection
	 *            Fetch collections as well
	 * @param includeReferences
	 *            Fetch references as well
	 * @return A list of instances retrieved from the database.
	 * @throws MappingException
	 *             If unable to map the result set.
	 * @throws SQLException
	 *             If JDBC throws a SQLException.
	 */
	public <E> List<E> getAll(Connection connection, Class<E> clazz, boolean includeCollections, boolean includeReferences, Integer limit, Integer offset, List<SqlOrderByColumn> orderBys,
			SqlSelectColumn... columns) throws MappingException, SQLException {
		SqlSelect sql = buildSelectSql(clazz, includeCollections, includeReferences);
		return selectAll(connection, sql, clazz, orderBys, limit, offset, columns);
	}

	/**
	 * Get all instances of the given class using the database connection given.
	 * Note that referenced classes are NOT retrieved. I.e. a so called lazy
	 * fetch strategy.
	 * 
	 * @param connection
	 *            The database connection to use for the query.
	 * @param clazz
	 *            The class to fetch instances of.
	 * @param includeCollection
	 *            Fetch collections as well
	 * @param includeReferences
	 *            Fetch references as well
	 * @return A list of instances retrieved from the database.
	 * @throws MappingException
	 *             If unable to map the result set.
	 * @throws SQLException
	 *             If JDBC throws a SQLException.
	 */
	public <E> List<E> getAll(Connection connection, Class<E> clazz, boolean includeCollections, boolean includeReferences, SqlSelectColumn... columns)
			throws MappingException, SQLException {
		SqlSelect sql = buildSelectSql(clazz, includeCollections, includeReferences);
		return selectAll(connection, sql, clazz, columns);
	}

	/**
	 * Lazy load one instance matching criteria. Returns null if no match is
	 * found. Throws an exception if multiple instances are found.
	 * 
	 * @param connection
	 * @param clazz
	 * @param columns
	 * @return
	 * @throws MappingException
	 * @throws SQLException
	 */
	public <E> E getOne(Connection connection, Class<E> clazz, SqlSelectColumn... columns) throws MappingException, SQLException {
		return getOne(connection, clazz, false, false, null, null, null, columns);
	}

	public <E> E getOne(Connection connection, Class<E> clazz, boolean includeCollections, boolean includeReferences, SqlSelectColumn... columns) throws MappingException, SQLException {
		return getOne(connection, clazz, includeCollections, includeReferences, null, null, null, columns);
	}

	public <E> E getOne(Connection connection, Class<E> clazz, boolean includeCollections, boolean includeReferences, List<SqlOrderByColumn> orderbys, Integer limit, Integer offset, SqlSelectColumn... columns)
			throws MappingException, SQLException {
		SqlSelect sql = buildSelectSql(clazz, includeCollections, includeReferences);
		return selectOne(connection, sql, clazz, orderbys, limit, offset, columns);
	}
	
	public <E> E selectOne(Connection connection, SqlSelect sql, Class<E> clazz, SqlSelectColumn... columns) throws MappingException, SQLException {
		return selectOne(connection, sql, clazz, null, null, null, columns);
	}


	public <E> E selectOne(Connection connection, SqlSelect sql, Class<E> clazz, List<SqlOrderByColumn> orderbys, Integer limit, Integer offset, SqlSelectColumn... columns) throws MappingException, SQLException {		
		String select = sql.getSelect();
		if (columns.length > 0) {
			boolean first = true;
			String condition = "";
			for (SqlSelectColumn c : columns) {
				if (first) {
					select += " WHERE";
					first = false;
				} else {
					condition = c.getPreviousCondition().getValue();
					select += " " + condition;
				}
				if (c.getOperator().isListOperator()) {
					throw new UnsupportedOperationException("Select one is currently not supported for list operators");
				}
				select += " " + c.getColumn() + " " + c.getOperator().getValue() + " ?";
			}
		}		
		querylog.debug(select);
		if (orderbys != null && orderbys.size() > 0) {
			select += " ORDER BY ";
			for (SqlOrderByColumn o : orderbys) {
				select += o.getColumn() + " " + o.getDirection().getValue() + " ";
			}
		}
		if (limit != null) {
			select += " LIMIT ?";
			if (offset != null) {
				select += " OFFSET ?";
			}
		}
		PreparedStatement stmt = connection.prepareStatement(select);
		int p = 1;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].getOperator().isListOperator()) {
				for (int j = 0;j < columns[i].getListValue().length;j++) {
					stmt.setObject(p, columns[i].getListValue()[j]);
					p++;
				}
			} else {
				stmt.setObject(p, columns[i].getValue());
			}
			p++;
		}
		if (limit != null) {
			stmt.setInt(p, limit);
			p++;
			if (offset != null) {
				stmt.setInt(p, offset);
				p++;
			}
		}
		ResultSet result = stmt.executeQuery();
		ResultSetMapper mapper = new ResultSetMapper();
		return mapper.getSinglePojo(clazz, result);
	}

	/**
	 * This class represents a SQL select query. You can add/remove columns and
	 * joins. The where clause must currently be added manually.
	 */
	public static class SqlSelect {
		private List<SqlColumn> columns = new ArrayList<SqlColumn>();
		private String table;
		private List<SqlJoin> joins = new ArrayList<SqlJoin>();

		public SqlSelect(String table) {
			super();
			this.table = table;
		}

		public void addJoin(SqlJoin join) {
			joins.add(join);
		}

		public void addJoin(Class<?> clazz, String joinColumn, String refColumn) throws MappingException {
			TableDescriptor td = TableDescriptor.build(clazz);
			SqlJoin join = new SqlJoin(table, td.getTableName(), joinColumn, refColumn);
			joins.add(join);
			for (ColumnDescriptor cd : td.getColumnDescriptors()) {
				if (!cd.isCollection() && !cd.getColumnName().equals(joinColumn)) {
					addColumn(td.getTableName(), cd.getColumnName());
				}
			}
		}

		public void addJoin(Class<?> clazz, Class<?> joinClazz, String joinColumn, String refColumn) throws MappingException {
			TableDescriptor td = TableDescriptor.build(clazz);
			TableDescriptor jointd = TableDescriptor.build(joinClazz);
			SqlJoin join = new SqlJoin(td.getTableName(), jointd.getTableName(), joinColumn, refColumn);
			joins.add(join);
			for (ColumnDescriptor cd : td.getColumnDescriptors()) {
				if (!cd.isCollection() && !cd.getColumnName().equals(joinColumn)) {
					addColumn(td.getTableName(), cd.getColumnName());
				}
			}
		}

		/**
		 * Add a join to the given class.
		 * 
		 * Note that the class is always joined with the 'main' table of the
		 * select. (So far longer joins you may have to write some SQL by hand)
		 * 
		 * @param clazz
		 *            The class with the annotation-mapping to the table to
		 *            join.
		 * @param joinColumn
		 *            The column to join on the table to join
		 * @param refColumn
		 *            The column referencing the table to join (the foreign key
		 *            on the 'main' table)
		 * @param type
		 *            Specify whether to join, right join, left join etc.
		 * @throws MappingException
		 *             If unable to build the TableDescriptor from the provided
		 *             class.
		 */
		public void addJoin(Class<?> clazz, String joinColumn, String refColumn, JoinType type) throws MappingException {
			TableDescriptor td = TableDescriptor.build(clazz);
			SqlJoin join = new SqlJoin(table, td.getTableName(), joinColumn, refColumn, type);
			joins.add(join);
			for (ColumnDescriptor cd : td.getColumnDescriptors()) {
				if (!cd.isCollection() && !cd.getColumnName().equals(joinColumn)) {
					addColumn(td.getTableName(), cd.getColumnName());
				}
			}
		}

		/**
		 * Specify an extra column to select
		 * 
		 * @param column
		 */
		public void addColumn(String column) {
			columns.add(new SqlColumn(column));
		}

		/**
		 * <pre>
		 * Specify an extra column to select - from a table different from the main table.
		 * This column will be selected 'as' <table>__<column>
		 * </pre>
		 * 
		 * @param table
		 * @param column
		 */
		public void addColumn(String table, String column) {
			columns.add(new SqlColumn(table, column, column));
		}

		/**
		 * Specify an extra column to select.
		 * 
		 * @param column
		 *            The details of the column to select.
		 */
		public void addColumn(SqlColumn column) {
			columns.add(column);
		}

		/**
		 * Generate the select statement from the current state of this sql
		 * select.
		 * 
		 * @return The select query as a string. Append
		 *         " where bla = 'some string'" if you need before executing the
		 *         query.
		 */
		public String getSelect() {
			StringBuilder sb = new StringBuilder();
			sb.append("select ");
			int i = 0;
			for (SqlColumn c : columns) {
				i++;
				String lab = c.getLabel();
				String col = c.getColumn();
				if (c.getTable() != null) {
					if (!c.getTable().equals(table) && c.isLabelPrefix()) {
						lab = c.getTable() + "__" + lab;
					}
					col = c.getTable() + "." + col;
				} else {
					col = table + "." + col;
				}
				sb.append(col);
				if (!col.equals(lab)) {
					sb.append(" as " + lab);
				}
				if (i < columns.size()) {
					sb.append(", ");
				}

			}
			sb.append(" from " + table);
			for (SqlJoin j : joins) {
				sb.append(" " + j.getType().getValue() + " " + j.joinTable + " on ");
				sb.append(j.getTable() + "." + j.refColumn);
				sb.append(" = " + j.joinTable + "." + j.joinColumn);
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return "SqlSelect [columns=" + columns + ", table=" + table + ", joins=" + joins + "]";
		}

	}

	public static class SqlColumn {
		private String column;
		private String label;
		private String table;
		private boolean labelPrefix = true;

		public SqlColumn() {
			super();
		}

		public SqlColumn(String column) {
			super();
			this.column = column;
			this.label = column;
		}

		public SqlColumn(String column, String label) {
			super();
			this.column = column;
			this.label = label;
		}

		public SqlColumn(String table, String column, String label) {
			super();
			this.column = column;
			this.label = label;
			this.table = table;
		}

		public SqlColumn(String table, String column, String label, boolean labelPrefix) {
			super();
			this.column = column;
			this.label = label;
			this.table = table;
			this.labelPrefix = labelPrefix;
		}

		public String getColumn() {
			return column;
		}

		public void setColumn(String column) {
			this.column = column;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}

		public boolean isLabelPrefix() {
			return labelPrefix;
		}

		public void setLabelPrefix(boolean labelPrefix) {
			this.labelPrefix = labelPrefix;
		}

		@Override
		public String toString() {
			return "SqlColumn [column=" + column + ", label=" + label + ", table=" + table + ", labelPrefix=" + labelPrefix + "]";
		}

	}

	public static class SqlJoin {
		private String table;
		private String joinTable;
		private String joinColumn;
		private String refColumn;
		private JoinType type = JoinType.JOIN;

		public SqlJoin(String table) {
			super();
			this.table = table;
		}

		public SqlJoin(String table, String joinTable, String joinColumn, String refColumn) {
			super();
			this.table = table;
			this.joinTable = joinTable;
			this.joinColumn = joinColumn;
			this.refColumn = refColumn;
		}

		public SqlJoin(String table, String joinTable, String joinColumn, String refColumn, JoinType type) {
			super();
			this.table = table;
			this.joinTable = joinTable;
			this.joinColumn = joinColumn;
			this.refColumn = refColumn;
			this.type = type;
		}

		public JoinType getType() {
			return type;
		}

		public void setType(JoinType type) {
			this.type = type;
		}

		public String getJoinTable() {
			return joinTable;
		}

		public void setJoinTable(String joinTable) {
			this.joinTable = joinTable;
		}

		public String getJoinColumn() {
			return joinColumn;
		}

		public void setJoinColumn(String joinColumn) {
			this.joinColumn = joinColumn;
		}

		public String getRefColumn() {
			return refColumn;
		}

		public void setRefColumn(String refColumn) {
			this.refColumn = refColumn;
		}

		public String getTable() {
			return table;
		}

		public void setTable(String table) {
			this.table = table;
		}
	}

	public static enum JoinType {
		JOIN("join"), LEFT_JOIN("left join"), RIGHT_JOIN("right join"), INNER_JOIN("inner join"), OUTER_JOIN("outer join");

		private final String value;

		private JoinType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
