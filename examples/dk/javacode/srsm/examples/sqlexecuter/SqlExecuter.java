package dk.javacode.srsm.examples.sqlexecuter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.javacode.srsm.ResultSetMapper;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.helpers.DefaultSqlTypeConverter;

/**
 * Utility class to take some of the pains away from executing queries. Creates
 * PreparedStatements from plain sql-strings and adds parameters - either using
 * named parameters or standard indexed '?' paramters.
 * 
 * May move generics from Constructor to methods in future release.
 * 
 * @author kavi
 * 
 * @param <E>
 */
public class SqlExecuter<E> {

	private Class<E> clazz;
	
	private DefaultSqlTypeConverter sqlTypeConverter = new DefaultSqlTypeConverter();

	public SqlExecuter(Class<E> clazz) {
		this.clazz = clazz;
	}

	private PreparedStatement createPreparedStatement(Connection connection, PreparedSql sql) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(sql.getSql());
		for (int i = 0; i < sql.getParams().size(); i++) {
			TypedSqlValue typedParam = sql.getParams().get(i);
			int sqltype = typedParam.getSqlType();
			Object value = typedParam.getValue();
			if (value == null) {
				stmt.setNull(i + 1, sqltype);
			} else {
				stmt.setObject(i + 1, value, sqltype);
			}
		}
		return stmt;
	}

	private PreparedStatement createPreparedStmt(Connection connection, String select, Object... params) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(select);
		for (int i = 0; i < params.length; i++) {
			stmt.setObject(i + 1, params[i], sqlTypeConverter.getSqlType(params[i]));
		}
		return stmt;
	}

	/**
	 * Execute the given sql-string as a prepared statement using the given
	 * connection. Params will be treated as standard '?' indexed parameters.
	 * 
	 * @param connection
	 *            The connection to execute against
	 * @param select
	 *            The sql-string
	 * @param params
	 *            The parameters
	 * @return the result of preparedStatement.execute()
	 * @throws SQLException
	 * @throws MappingException
	 */
	public boolean execute(Connection connection, String select, Object... params) throws SQLException, MappingException {
		PreparedStatement stmt = createPreparedStmt(connection, select, params);
		return stmt.execute();
	}

	/**
	 * Execute the given sql-string as a prepared statement using the given
	 * connection. Params will be treated as standard '?' indexed parameters.
	 * 
	 * @param connection
	 *            The connection to execute against
	 * @param select
	 *            The sql-string
	 * @param params
	 *            The parameters
	 * @return the result of preparedStatement.executeUpdate()
	 * @throws SQLException
	 * @throws MappingException
	 */
	public int executeUpdate(Connection connection, String select, Object... params) throws SQLException, MappingException {
		PreparedStatement stmt = createPreparedStmt(connection, select, params);
		return stmt.executeUpdate();
	}

	/**
	 * Execute the given sql-string as a prepared statement using the given
	 * connection. Params will be treated as standard '?' indexed parameters.
	 * The ResultSet will be passed through a SimpleResultSetMapper and this
	 * result will be returned as a list.
	 * 
	 * @param connection
	 *            The connection to execute against
	 * @param select
	 *            The sql-string
	 * @param params
	 *            The parameters
	 * @return the result of the Query as a List of POJOs.
	 * @throws SQLException
	 * @throws MappingException
	 */
	public List<E> executeQuery(Connection connection, String select, Object... params) throws SQLException, MappingException {
		PreparedStatement stmt = createPreparedStmt(connection, select, params);
		ResultSet result = stmt.executeQuery();
		ResultSetMapper mapper = new ResultSetMapper();
		return mapper.toPojo(clazz, result);
	}

	/**
	 * Execute the given sql-string as a prepared statement using the given
	 * connection. Params are declared as named parameters using the syntax
	 * ':<param-name>' and passed in a Map where key=<param-name> and value is
	 * the parameter value.
	 * 
	 * Example: select * from persons where id = :id
	 * 
	 * params.put("id", 1);
	 * 
	 * The ResultSet will be passed through a SimpleResultSetMapper and this
	 * result will be returned as a list.
	 * 
	 * @param connection
	 *            The connection to execute against
	 * @param select
	 *            The sql-string
	 * @param params
	 *            The parameters
	 * @return the result of the Query as a List of POJOs.
	 * @throws SQLException
	 * @throws MappingException
	 */
	public List<E> executeQuery(Connection connection, String select, Map<String, Object> params) throws SQLException, MappingException {
		PreparedStatement stmt = createPreparedStatement(connection, toPreparedSql(select, params));
		ResultSet result = stmt.executeQuery();
		ResultSetMapper mapper = new ResultSetMapper();
		return mapper.toPojo(clazz, result);
	}

	public List<E> executeQuery(Connection connection, String select, SqlParam... params) throws SQLException, MappingException {
		Map<String, Object> _params = new HashMap<String, Object>();
		for (SqlParam p : params) {
			_params.put(p.getName(), p.getValue());
		}
		return executeQuery(connection, select, _params);
	}

	PreparedSql toPreparedSql(String select, Map<String, Object> params) {
		PreparedSql rv = new PreparedSql();
		int idx = select.indexOf(":");
		int i = 0;
		while (idx > 0 && i < 100) {
			i++;
			String left = select.substring(0, idx);
			String right = select.substring(idx + 1);
			int spc = right.indexOf(" ");
			if (spc < 1) {
				spc = right.length();
			}
			String paramname = right.substring(0, spc);
			Object paramvalue = params.get(paramname);
			rv.addParam(paramvalue);
			right = right.substring(spc);
			select = left + "?" + right;
			idx = select.indexOf(":");
		}
		rv.setSql(select);
		return rv;
	}



	class PreparedSql {
		private String sql;
		private List<TypedSqlValue> params = new LinkedList<TypedSqlValue>();

		public PreparedSql() {
			super();
		}

		public void addParam(Object paramvalue) {
			TypedSqlValue param; 
			if (paramvalue instanceof TypedSqlValue) {
				param = (TypedSqlValue) paramvalue;
			} else {
				param = new TypedSqlValue();
				param.setSqlType(sqlTypeConverter.getSqlType(paramvalue));
				param.setValue(paramvalue);
			}
			params.add(param);
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public List<TypedSqlValue> getParams() {
			return params;
		}

		@Override
		public String toString() {
			return "PreparedSql [sql=" + sql + ", params=" + params + "]";
		}
	}

	public static class TypedSqlValue {
		private int sqlType;
		private Object value;

		public TypedSqlValue() {
			super();
		}

		public TypedSqlValue(Object value, int sqlType) {
			super();
			this.value = value;
			this.sqlType = sqlType;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public int getSqlType() {
			return sqlType;
		}

		/**
		 * 
		 * @param sqlType an int - use java.sql.Types for constants
		 */
		public void setSqlType(int sqlType) {
			this.sqlType = sqlType;
		}

		@Override
		public String toString() {
			return "TypedSqlParam [sqlType=" + sqlType + ", value=" + value + "]";
		}

	}
}
