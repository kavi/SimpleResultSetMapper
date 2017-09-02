package dk.javacode.srsm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.javacode.srsm.descriptors.ColumnDescriptor;
import dk.javacode.srsm.descriptors.TableDescriptor;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.util.ReflectionUtil;

public class ResultSetMapper {

	private static final Map<Class<?>, TableDescriptor> descriptors = new HashMap<Class<?>, TableDescriptor>();

	private String tableSplitToken = "__";

	public ResultSetMapper() {
		super();
	}

	public ResultSetMapper(String tableSplitToken) {
		super();
		this.tableSplitToken = tableSplitToken;
	}

	//@formatter:off
	/**
	 * <pre>
	 * Maps a resultSet to a POJO.
	 * The class to map to must be annotated with SRTable and SRColumn annotations
	 * to indicate table and column names. (OK, so tableName is actually not being
	 * used for this mapping, but it is still required)
	 * 
	 * All columns from the result set will be attempted to be mapped.
	 * Fields of referenced (indirect) objects must be labelled "[referencedType]__[column]".
	 * 
	 * I.e. a Person has an Address field (private Address address). To instantiate the address
	 * the resultSet columns for address values must be prefixed address__ (example address__street_name).
	 * And the address field must have the columnReference annotation set to the id of the Address class.
	 * 
	 * If only the id of a referenced object is retrieved it is still instantiated, but only the ID field
	 * will be initialised.
	 * 
	 * In general, objects will be created even if not all columns are available. Only as much as is possible
	 * from the data in the resultSet is initialised.
	 * 
	 * This method will not preserve the order of the rows retrieved from the database
	 * </pre>
	 * 
	 * @param clazz The type to map this result set to
	 * @param resultSet The result set to map
	 * @return A list of instances of the type to map to. One instance per row
	 * @throws SQLException
	 * @throws MappingException
	 */
	//@formatter:on
	public <E> List<E> toPojo(Class<E> clazz, ResultSet resultSet) throws SQLException, MappingException {
		return toPojo(clazz, resultSet, false);
	}

	/**
	 * Similar to toPojo(clazz, resultSet) but the preserveOrder flag specifies whether or not order should be preserved.
	 * If preserveOrder=false this is _exactly_ as calling toPojo(clazz, resultSet).
	 * 
	 * Generally there is no need to preserve order unless an "ORDER BY" has been used in the SQL query.
	 * 
	 * @param clazz The type to map this result set to
	 * @param resultSet The result set to map
	 * @param preserveOrder True to preserve the order of rows from the ResultSet
	 * @return A list of instances of the type to map to. One instance per row
	 * @throws SQLException
	 * @throws MappingException
	 */
	public <E> List<E> toPojo(Class<E> clazz, ResultSet resultSet, boolean preserveOrder) throws SQLException, MappingException {
		List<E> result = new ArrayList<E>();
		Map<Object, E> map = new HashMap<Object, E>();
		// Build the tableDescriptor for this Class/Type
		TableDescriptor tableDescriptor = TableDescriptor.build(clazz);

		// Read metaData from the result set
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();

		// Create a pojo for each row in the result set
		while (resultSet.next()) {
			Map<Class<?>, Object> refs = new HashMap<Class<?>, Object>();
			E pojo = ReflectionUtil.getNewInstance(clazz);
			for (int column = 1; column <= columnCount; column++) {
				// int type = metaData.getColumnType(column);
				Object columnValue = resultSet.getObject(column);
				//				if (columnValue != null) {
				String columnname = metaData.getColumnLabel(column).toLowerCase();
				updatePojo(pojo, clazz, columnname, tableDescriptor, columnValue, refs);
				//				}
			}
			// If there is a primary key (of type int) add pojo to Map - if multiple rows of the same id is found
			// collection columns will be populated.
			if (tableDescriptor.hasPrimaryKeyColumn()) {
				Method pkGetter = ReflectionUtil.getGetMethodForField(tableDescriptor.getMappedClass(), tableDescriptor.getPrimaryKeyColumn().getColumnField());
				Object pk = ReflectionUtil.invokeMethod(pojo, pkGetter);
				if (map.containsKey(pk)) {
					E orig = map.get(pk);
					for (ColumnDescriptor cd : tableDescriptor.getColumnDescriptors()) {
						if (cd.isCollection()) {
							Method getter = ReflectionUtil.getGetMethodForField(tableDescriptor.getMappedClass(), cd.getColumnField());
							Collection<?> nc = (Collection<?>) ReflectionUtil.invokeMethod(pojo, getter);
							for (Object o : nc) {
								ReflectionUtil.invokeMethod(orig, cd.getCollectionAddMethod(), o);
							}

						}
					}
					pojo = orig;
				}
				map.put(pk, pojo);
			}
			result.add(pojo);
			refs.clear();
		}
		if (tableDescriptor.hasPrimaryKeyColumn()) {
			if (preserveOrder) {
				Method pkGetter = ReflectionUtil.getGetMethodForField(tableDescriptor.getMappedClass(), tableDescriptor.getPrimaryKeyColumn().getColumnField());
				List<E> tmp = new ArrayList<E>(map.values().size() + 1);
				for (E v : result) {
					Object pk = ReflectionUtil.invokeMethod(v, pkGetter);
					if (map.containsKey(pk)) {
						tmp.add(v);
						map.remove(pk);
					}
				}
				return tmp;
			} else {
				return new ArrayList<E>(map.values());
			}
		} else {
			return result;
		}
	}

	//@formatter:off
	/**
	 * <pre>
	 * Maps a resultSet to a POJO.
	 * The class to map to must be annotated with SRTable and SRColumn annotations
	 * to indicate table and column names. (OK, so tableName is actually not being
	 * used for this mapping, but it is still required)
	 * 
	 * All columns from the result set will be attempted to be mapped.
	 * Fields of referenced (indirect) objects must be labelled "[referencedType]__[column]".
	 * 
	 * I.e. a Person has an Address field (private Address address). To instantiate the address
	 * the resultSet columns for address values must be prefixed address__ (example address__street_name).
	 * And the address field must have the columnReference annotation set to the id of the Address class.
	 * 
	 * If only the id of a referenced object is retrieved it is still instantiated, but only the ID field
	 * will be initialised.
	 * 
	 * In general, objects will be created even if not all columns are available. Only as much as is possible
	 * from the data in the resultSet is initialised.
	 * </pre>
	 * 
	 * @param clazz The type to map this result set to
	 * @param resultSet The result set to map
	 * @return A list of instances of the type to map to. One instance per row
	 * @throws SQLException
	 * @throws MappingException
	 */
	//@formatter:on
	public <E> E getSinglePojo(Class<E> clazz, ResultSet resultSet) throws SQLException, MappingException {
		List<E> result = toPojo(clazz, resultSet);
		if (result.size() == 0) {
			return null;
		} else if (result.size() == 1) {
			return result.get(0);
		} else {
			throw new MappingException("More than one row in resultSet in getSinglePojo method");
		}

	}

	/**
	 * <pre>
	 * Updates a field of a pojo or calls recursively to update the field of a referenced pojo.
	 * </pre>
	 * 
	 * @param pojo
	 * @param clazz
	 * @param columnName
	 * @param resultSet
	 * @param tableDescriptor
	 * @param column
	 * @throws SQLException
	 * @throws MappingException
	 */
	private <E> void updatePojo(Object pojo, Class<E> clazz, String columnName, TableDescriptor tableDescriptor, Object columnValue, Map<Class<?>, Object> refs)
			throws SQLException, MappingException {
		ColumnDescriptor columnDescriptor = tableDescriptor.getColumnDescriptor(columnName);
		if (columnDescriptor == null) {
			// look for column descriptor in referenced types
			int idx = columnName.indexOf(tableSplitToken);
			if (idx < 1) {
				throw new MappingException("No column descriptor found for column: " + columnName + " on class " + clazz.getName());
			}
			String refTable = columnName.substring(0, idx);
			String refColumn = columnName.substring(idx + tableSplitToken.length());

			Class<?> refType = tableDescriptor.getReferencedType(refTable);
			TableDescriptor referencedTable = TableDescriptor.build(refType);
			columnDescriptor = referencedTable.getColumnDescriptor(refColumn);
			Object ref = refs.get(refType);
			if (ref == null) {
				ref = ReflectionUtil.getNewInstance(refType);
			}
			refs.put(refType, ref);
			updatePojo(ref, refType, refColumn, referencedTable, columnValue, refs);
		} else {
			// Column descriptor found
			Field field = columnDescriptor.getColumnField();
			columnValue = columnDescriptor.getDataConverter().convertToPojo(columnValue);

			if (columnDescriptor.isCollection() && columnValue != null) {
				Class<?> referencedType = columnDescriptor.getCollectionType();
				Object ref = refs.get(referencedType);
				if (ref == null) {
					ref = ReflectionUtil.getNewInstance(referencedType);
					refs.put(ref.getClass(), ref);
				}
				Method adder = columnDescriptor.getCollectionAddMethod();
				ReflectionUtil.invokeMethod(pojo, adder, ref);
			} else if (columnDescriptor.getFieldReference() != null && columnValue != null) {
				Class<?> referencedType = field.getType();
				Object ref = refs.get(referencedType);
				if (ref == null) {
					ref = ReflectionUtil.getNewInstance(referencedType);
				}
				Method refMethod = columnDescriptor.getFieldReferenceSetter();
				ReflectionUtil.invokeMethod(ref, refMethod, columnValue);
				ReflectionUtil.invokeMethod(pojo, columnDescriptor.getColumnSetMethod(), ref);
				refs.put(ref.getClass(), ref);
			} else if (columnValue != null) {
				ReflectionUtil.invokeMethod(pojo, columnDescriptor.getColumnSetMethod(), columnValue);
			}
		}
	}

	public static void clearDescriptors() {
		descriptors.clear();
	}

}
