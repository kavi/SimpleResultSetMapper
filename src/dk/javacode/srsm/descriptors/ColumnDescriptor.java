package dk.javacode.srsm.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dk.javacode.srsm.converters.JdbcDataConverter;
import dk.javacode.srsm.helpers.SqlType;

public class ColumnDescriptor
{
	private String columnName;
	private Field columnField;
	private Method columnSetMethod;

	private Field fieldReference;
	private Method fieldReferenceSetter;

	private String columnReference;
	private Method collectionAddMethod;

	private JdbcDataConverter<?> dataConverter;

	private boolean writeOnly;
	private boolean primaryKey;
	private boolean collection;
	private Class<?> collectionType;

	private int sqlType;

	public int getSqlType()
	{
		return sqlType;
	}

	public void setSqlType(int sqlType)
	{
		this.sqlType = sqlType;
	}

	public Class<?> getMappedType()
	{
		return columnField.getType();
	}

	/**
	 * The type of the objects in the collection
	 * @return
	 */
	public Class<?> getCollectionType()
	{
		return collectionType;
	}

	public void setCollectionType(Class<?> collectionType)
	{
		this.collectionType = collectionType;
	}

	/**
	 * Is this column marked as a collection?
	 * @return
	 */
	public boolean isCollection()
	{
		return collection;
	}

	public void setCollection(boolean collection)
	{
		this.collection = collection;
	}

	/**
	 * The method used to add to a collection [
	 * add<propertyName>(<PropertyType>) ]
	 * 
	 * @return
	 */
	public Method getCollectionAddMethod()
	{
		return collectionAddMethod;
	}

	public void setCollectionAddMethod(Method collectionAddMethod)
	{
		this.collectionAddMethod = collectionAddMethod;
	}

	public JdbcDataConverter<?> getDataConverter()
	{
		return dataConverter;
	}

	public void setDataConverter(JdbcDataConverter<?> dataConverter)
	{
		this.dataConverter = dataConverter;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public Field getColumnField()
	{
		return columnField;
	}

	public void setColumnField(Field columnField)
	{
		this.columnField = columnField;
	}

	public Method getColumnSetMethod()
	{
		return columnSetMethod;
	}

	public void setColumnSetMethod(Method columnSetMethod)
	{
		this.columnSetMethod = columnSetMethod;
	}

	public Field getFieldReference()
	{
		return fieldReference;
	}

	public void setFieldReference(Field columnReference)
	{
		this.fieldReference = columnReference;
	}

	public Method getFieldReferenceSetter()
	{
		return fieldReferenceSetter;
	}

	public void setFieldReferenceSetter(Method columnReferenceSetter)
	{
		this.fieldReferenceSetter = columnReferenceSetter;
	}

	public boolean isPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	public boolean isWriteOnly()
	{
		return writeOnly;
	}

	public void setWriteOnly(boolean writeOnly)
	{
		this.writeOnly = writeOnly;
	}

	public String getColumnReference()
	{
		return columnReference;
	}

	public void setColumnReference(String columnReference)
	{
		this.columnReference = columnReference;
	}

	@Override
	public String toString()
	{
		return "ColumnDescriptor [columnName=" + columnName + ", columnField=" + columnField + ", columnSetMethod=" + columnSetMethod
						+ ", fieldReference=" + fieldReference + ", fieldReferenceSetter=" + fieldReferenceSetter + ", collectionAddMethod="
						+ collectionAddMethod + ", dataConverter=" + dataConverter + ", writeOnly=" + writeOnly + ", primaryKey=" + primaryKey
						+ ", collection=" + collection + ", collectionType=" + collectionType + "]";
	}

}
