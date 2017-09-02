package dk.javacode.srsm.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.javacode.srsm.converters.JdbcDataConverter;
import dk.javacode.srsm.descriptors.ColumnDescriptor;
import dk.javacode.srsm.descriptors.TableDescriptor;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.exceptions.MappingRuntimeException;
import dk.javacode.srsm.util.ReflectionUtil;

/**
 * Utility class to insert mapped objects.
 */
public class SqlBatchInsertHelper {
	private static Logger log = LoggerFactory.getLogger(SqlBatchInsertHelper.class);

	/**
	 * Creates an insert SQL statement from a given pojo. Note that POJOs that
	 * uses custom converters will likely fail on insert - or worse may succeed
	 * in inserting invalid data into the database. (A future solution may be to
	 * make converters two-way)
	 * 
	 * Returns a result containing the Sql and the parameterValues as a list.
	 * This method uses setAccessible(true) on fields and therefore won't work
	 * in some security contexts.
	 * 
	 * @throws MappingException
	 */
	public <E> SqlBatchInsert buildBatchInsertSql(Class<E> clazz) throws IllegalArgumentException, IllegalAccessException, MappingException {
		if (clazz == null) {
			throw new RuntimeException("Cannot insert null class");
		}
		SqlBatchInsert result = new SqlBatchInsert();
		String columns = "";
		String values = "";

		TableDescriptor tableDescriptor = TableDescriptor.build(clazz);
		int i = 1;
		for (ColumnDescriptor cd : tableDescriptor.getColumnDescriptors()) {
			if (cd.isCollection()) {
				continue;
			}

			Method getter = ReflectionUtil.getGetMethodForField(clazz, cd.getColumnField());
			if (cd.getFieldReference() != null) {
				int sqlType = cd.getSqlType();
				result.addParameter(getter, sqlType, i, cd.getFieldReference());
				columns += cd.getColumnName() + ", ";
				values += "?, ";
				i++;
			} else {
				int sqlType = cd.getSqlType();
				if (cd.isPrimaryKey()) {
					Method setMethod = cd.getColumnSetMethod();
					result.setPrimaryKeySetMethod(setMethod);
				} else {
					result.addParameter(getter, sqlType, i);
					columns += cd.getColumnName() + ", ";
					values += "?, ";
					i++;
				}
			}
		}
		if (columns.length() == 0) {
			throw new MappingRuntimeException("No fields found on class: " + clazz.getName());
		}
		columns = columns.substring(0, columns.length() - 2);
		values = values.substring(0, values.length() - 2);
		String sql = "insert into " + tableDescriptor.getTableName() + " (" + columns + ") values (" + values + ")";
		result.setSql(sql);
		return result;
	}

	public <E, P> void insertList(Connection connection, List<E> objects, JdbcDataConverter<P> converter)
			throws SQLException, MappingException {
		if (objects == null) {
			throw new MappingRuntimeException("Cannot insert list: null");
		}
		if (objects.isEmpty()) {
			return;
		}
		PreparedStatement stmt = null;
		
		try {
			SqlBatchInsert sql = buildBatchInsertSql(objects.get(0).getClass());
			String sqlString = sql.getSql();
			log.debug("inserting object using: " + sqlString);
			stmt = connection.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);

			for (E object : objects) {
				for (SqlInsertParameter p : sql.getParameters()) {
					Object obj = ReflectionUtil.invokeMethod(object, p.getMethod());
					if (obj != null) {
						if (p.isFieldReference()) {
							Method frGetter = ReflectionUtil.getGetMethodForField(p.getFieldReference().getDeclaringClass(), p.getFieldReference());
							obj = ReflectionUtil.invokeMethod(obj, frGetter);
						}
						stmt.setObject(p.getIndex(), obj);
					} else {
						stmt.setNull(p.getIndex(), p.getSqlType());
					}
				}
				stmt.addBatch();
			}

			stmt.executeBatch();
			ResultSet keys = stmt.getGeneratedKeys();
			if (keys.getMetaData().getColumnCount() < 1) {
				log.info("No key column in generatedKeys resultSet for object: {}" + objects.get(0));
				return;
			}
			for (E object : objects) {
				if (!keys.next()) {
					log.debug("No key found for object {} ", object);
					continue;
				}
				Object tmpId = keys.getObject(1);
				P id = converter.convertToPojo(tmpId);
				if (sql.getPrimaryKeySetMethod() != null) {
					try {
						log.debug("Setting primary key using " + sql.getPrimaryKeySetMethod().getName() + " with argument: " + id);
						sql.getPrimaryKeySetMethod().invoke(object, id);
					} catch (Exception e) {
						log.warn("Unable to set id on object: " + object + " to '" + id + "' using method: " + sql.getPrimaryKeySetMethod().getName());
						continue;
					}
				}
			}
		} catch (IllegalArgumentException e1) {
			throw new MappingRuntimeException("Illegal argument", e1);
		} catch (IllegalAccessException e1) {
			throw new MappingRuntimeException("Illegal access", e1);
		} finally {
			stmt.close();
		}
		return;
	}

	public static class SqlBatchInsert {
		private String sql;
		private List<SqlInsertParameter> parameters = new ArrayList<SqlInsertParameter>();
		private Method primaryKeySetMethod;

		public SqlBatchInsert() {
			super();
		}

		public Method getPrimaryKeySetMethod() {
			return primaryKeySetMethod;
		}

		public void setPrimaryKeySetMethod(Method primaryKeySetMethod) {
			this.primaryKeySetMethod = primaryKeySetMethod;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public void addParameter(Method method, int sqlType, int index) {
			this.parameters.add(new SqlInsertParameter(sqlType, index, method));
		}

		public void addParameter(Method method, int sqlType, int index, Field fieldReference) {
			this.parameters.add(new SqlInsertParameter(sqlType, index, method, fieldReference));
		}

		public List<SqlInsertParameter> getParameters() {
			return parameters;
		}

		public void setParameters(List<SqlInsertParameter> parameters) {
			this.parameters = parameters;
		}

	}

	public static class SqlInsertParameter {
		private int sqlType;
		private int index;
		private Method method;
		private Field fieldReference;

		public SqlInsertParameter(int sqlType, int index, Method method) {
			super();
			this.sqlType = sqlType;
			this.index = index;
			this.method = method;
		}

		public SqlInsertParameter(int sqlType, int index, Method method, Field fieldReference) {
			super();
			this.sqlType = sqlType;
			this.index = index;
			this.method = method;
			this.fieldReference = fieldReference;
		}

		public boolean isFieldReference() {
			return fieldReference != null;
		}

		public Field getFieldReference() {
			return fieldReference;
		}

		public int getSqlType() {
			return sqlType;
		}

		public int getIndex() {
			return index;
		}

		public Method getMethod() {
			return method;
		}
	}
}