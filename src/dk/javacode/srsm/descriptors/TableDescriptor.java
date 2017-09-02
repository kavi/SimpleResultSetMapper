package dk.javacode.srsm.descriptors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.exceptions.MappingRuntimeException;

public class TableDescriptor {

	private static TableDescriptorBuilder tableDescriptorBuilder = new TableDescriptorBuilder();
	private static ConcurrentMap<Class<?>, TableDescriptor> descriptors = new ConcurrentHashMap<Class<?>, TableDescriptor>();
	private static ReentrantLock buildDescriptorLock = new ReentrantLock();

	private String tableName;
	private Class<?> mappedClass;
	private Map<String, ColumnDescriptor> columns = new HashMap<String, ColumnDescriptor>();

	private Map<String, Class<?>> referencedTypes = new HashMap<String, Class<?>>();
	

	private ColumnDescriptor primaryKeyColumn = null;

	public boolean hasPrimaryKeyColumn() {
		return primaryKeyColumn != null;
	}

	public ColumnDescriptor getPrimaryKeyColumn() {
		return primaryKeyColumn;
	}

	public void setPrimaryKeyColumn(ColumnDescriptor primaryKeyColumn) {
		this.primaryKeyColumn = primaryKeyColumn;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Class<?> getMappedClass() {
		return mappedClass;
	}

	public void setMappedClass(Class<?> mappedClass) {
		this.mappedClass = mappedClass;
	}

	public ColumnDescriptor getColumnDescriptor(String name) {
		return columns.get(name);
	}

	public void addColumnDescriptor(ColumnDescriptor column) {
		columns.put(column.getColumnName(), column);
	}

	public void addReferencedType(Class<?> refType) {
		Table tableAnnotation = refType.getAnnotation(Table.class);
		if (tableAnnotation == null) {
			throw new MappingRuntimeException("No table annotation on referenced class: " + refType);
		}
		referencedTypes.put(tableAnnotation.name(), refType);
	}

	public Class<?> getReferencedType(String tableName) {
		return referencedTypes.get(tableName);
	}

	public Collection<ColumnDescriptor> getColumnDescriptors() {
		return Collections.unmodifiableCollection(columns.values());
	}

	/**
	 * This method will build a table descriptor for the given class. The build
	 * method caches previously built descriptors in a map. If a cached instance
	 * is found, then this instance is returned instead.
	 * 
	 * @param clazz
	 * @return
	 * @throws MappingException
	 */
	public static <E> TableDescriptor build(Class<E> clazz) throws MappingRuntimeException {
		// Lookup/create the tableDescriptor for this Class/Type
		TableDescriptor tableDescriptor = descriptors.get(clazz);
		if (tableDescriptor != null) {
			return tableDescriptor;
		}
		try {
			buildDescriptorLock.lock();
			tableDescriptor = descriptors.get(clazz);
			if (tableDescriptor == null) {
				tableDescriptor = new TableDescriptor();
				tableDescriptorBuilder.buildNew(clazz, tableDescriptor);
				descriptors.put(clazz, tableDescriptor);
			}
			return tableDescriptor;
		} finally {
			buildDescriptorLock.unlock();
		}
	}
	

}
