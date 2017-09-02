package dk.javacode.srsm.descriptors;

import java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.exceptions.MappingException;
import dk.javacode.srsm.exceptions.MappingRuntimeException;
import dk.javacode.srsm.helpers.DefaultSqlTypeConverter;
import dk.javacode.srsm.helpers.SqlType;
import dk.javacode.srsm.util.ReflectionUtil;

public class TableDescriptorBuilder {
	private static Logger log = LoggerFactory.getLogger(TableDescriptorBuilder.class);

	public TableDescriptorBuilder() {
	}

	public <E> TableDescriptor buildNew(Class<E> clazz, TableDescriptor table) throws MappingRuntimeException {
		log.info("Building table descriptor for class: " + clazz.getCanonicalName());
		try {
			Table tableAnnotation = clazz.getAnnotation(Table.class);
			if (tableAnnotation == null) {
				throw new MappingRuntimeException("Unable to map unannotated class: " + clazz.getName());
			}

			table.setMappedClass(clazz);
			table.setTableName(tableAnnotation.name());

			for (Field field : clazz.getDeclaredFields()) {
				Column columnAnnotation = field.getAnnotation(Column.class);
				if (columnAnnotation != null) {
					String columnName = columnAnnotation.name().toLowerCase();
					ColumnDescriptor column = new ColumnDescriptor();
					log.debug("Setting data-converter for column: " + columnName + " to " + columnAnnotation.dataConverter().getSimpleName());
					column.setDataConverter(ReflectionUtil.getNewInstance(columnAnnotation.dataConverter()));
					column.setColumnName(columnName);
					column.setColumnField(field);
					Method setMethodForField = ReflectionUtil.getSetMethodForField(clazz, field);
					if (setMethodForField == null) {
						throw new MappingRuntimeException("Unable to find setter method for field: " + field);
					}
					if (!Modifier.isPublic(setMethodForField.getModifiers())) {
						throw new MappingRuntimeException("Set method (" + setMethodForField.getName() + ") not accessible for field: " + field);
					}
					if (columnAnnotation.sqlType() != SqlType.ANY) {
						column.setSqlType(columnAnnotation.sqlType().getType());
					} else {
						column.setSqlType(new DefaultSqlTypeConverter().getSqlTypeFromClass(clazz));
					}

					column.setColumnSetMethod(setMethodForField);

					// Collection
					if (columnAnnotation.collection()) {
						if (columnAnnotation.columnReference().equals("")) {
							throw new MappingRuntimeException("ColumnReference MUST be set on collections");
						}
						Class<?> refType = field.getType();
						refType = columnAnnotation.collectionType();
						log.debug("Found collection reference '" + columnAnnotation.name() + "' to class: " + refType);
						column.setCollectionType(refType);
						Method addMethod = ReflectionUtil.getAddMethodForCollection(clazz, field);
						log.debug("Setting addMethod to: " + addMethod.getName());
						column.setCollectionAddMethod(addMethod);
						column.setColumnReference(columnAnnotation.columnReference());
						table.addReferencedType(refType);
					}
					else if (!columnAnnotation.columnReference().equals("")) {
						throw new MappingRuntimeException("Illegal annotation on " + field.getName() + ". ColumnReference MUST NOT be set on non-collections");
					}
					// Column reference
					else if (!"".equals(columnAnnotation.fieldReference())) {
						Class<?> refType = field.getType();
						log.debug("Found reference '" + columnAnnotation.fieldReference() + "' to class: " + refType);
						Field referencedField = ReflectionUtil.getField(refType, columnAnnotation.fieldReference());
						if (referencedField == null) {
							throw new MappingRuntimeException("Unable to find referenced field " + columnAnnotation.fieldReference());
						}
						column.setFieldReference(referencedField);

						Method referencedSetMethod = ReflectionUtil.getSetMethodForField(refType, referencedField);
						if (referencedSetMethod == null) {
							throw new MappingRuntimeException("Unable to find setter method for referenced field " + referencedField);
						}
						column.setFieldReferenceSetter(referencedSetMethod);

//						TableDescriptor referencedType = TableDescriptor.build(refType);
						table.addReferencedType(refType);
					}
					column.setWriteOnly(columnAnnotation.writeOnly());
					column.setPrimaryKey(columnAnnotation.primaryKey());
					column.setCollection(columnAnnotation.collection());

					if (column.isPrimaryKey()) {
						if (table.hasPrimaryKeyColumn()) {
							throw new MappingRuntimeException("Multiple primary key columns detected but not currently supported. columns ("
									+ table.getPrimaryKeyColumn().getColumnName() + " and " + column.getColumnName() + ")");
						}
						table.setPrimaryKeyColumn(column);
					}
					// log.info("Adding columnDescriptor: " + column);
					table.addColumnDescriptor(column);
				}
			}
			return table;
		} catch (MappingException e) {
			throw new MappingRuntimeException(e.getMessage(), e);
		}
	}
}
