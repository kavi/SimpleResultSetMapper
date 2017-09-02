package dk.javacode.srsm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dk.javacode.srsm.converters.DefaultJdbcDataConverter;
import dk.javacode.srsm.converters.JdbcDataConverter;
import dk.javacode.srsm.descriptors.TableDescriptorBuilder;
import dk.javacode.srsm.helpers.SqlType;

/**
 * See the individual attributes for specifics of how they are used.
 * 
 * Annotated classes are parsed using {@link TableDescriptorBuilder}.
 * 
 * @author Kavi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * Maps the field to a column-name in the database/resultSet.
	 * 
	 * **Used by**
	 * 
	 *  - ResultSetMapper
	 *  - QueryHelper
	 *  - InsertHelper
	 */
	public String name();
	
	/**
	 * Specifies that this is the primaryKey for the class.
	 * Only one primaryKey is allowed at the moment.
	 * 
	 * Used by:
	 * 
	 *  - ResultSetMapper (For adding collection objects to the correct instance)
	 *  - InsertHelper (retrieving and setting generated keys)
	 *  - QueryHelper?? (to generate joins)
	 *   
	 */
	public boolean primaryKey() default false;
	
	
	/**
	 * The ResultSetMapper will map all columns that are found
	 * in the ResultSet - no matter what this value is. 
	 *  
	 * Used by:
	 * 
	 *  - QueryHelper - No SQL will be generated for retrieving columns where writeOnly is true.
	 */
	public boolean writeOnly() default false;
	
	/**
	 * This value must be set on collections.
	 * 
	 * A collection must have an (add<fieldName>(<fieldType>))
	 * method.
	 * 
	 * Used by:
	 * 
	 *  - ResultSetMapper
	 *  - QueryHelper (Join on this collections type)
	 *  - InsertHelper (This field won't be inserted)
	 * @return
	 */
	public boolean collection() default false;
	
	/**
	 * Specify the type of the collection
     *
	 * Used by:
	 * 
	 *  - ResultSetMapper
	 *  - QueryHelper (Join on this collections type)
	 */
	public Class<?> collectionType() default Object.class;
	
	/**
	 * The field this foreign key references.
	 * That is: The field name on the referenced
	 * class.
	 * 
	 * The empty string is considered to be null/no reference.
	 * 
	 * Use for 1-1 references.
	 * 
	 * #### Used by
	 * 
	 *  - ResultSetMapper
	 *  - QueryHelper (Join on this columns type)
	 *  
	 */
	public String fieldReference() default "";
	
	/**
	 * The column this foreign key references.
	 * That is: The column name on the referenced
	 * table.
	 * 
	 * The empty string is considered to be null/no reference.
	 * 
	 * This annotation is mandatory for collections and
	 * invalid for all other types (i.e. collection = false) 
	 * 
	 * #### Example
	 * 
	 * If the foreign key is specified by:
	 * 
	 *     foreign key (customer_id) references customer(id),
	 *   
	 * The columnReference would be "id"
	 * 
	 * #### Used by
	 * 
	 *  - ResultSetMapper
	 *  - QueryHelper (Join on this columns type)
	 *  
	 */
	public String columnReference() default "";
	
	/**
	 * Used by:
	 * 
	 *  - ResultSetMapper
	 *  - InsertHelper
	 */
	public Class<? extends JdbcDataConverter<?>> dataConverter() default DefaultJdbcDataConverter.class;
	
	public SqlType sqlType() default SqlType.ANY;
}
