package dk.javacode.srsm.converters;

/**
 * Use the JdbcDataConverter to convert primitives between the database
 * model and the Java model.
 * 
 * @author Kavi
 *
 * @param <T> 
 */
public interface JdbcDataConverter<T> {

	/**
	 * Implement this method to convert the Object returned by the ResultSet to 
	 * the value required by your POJO.
	 * @param jdbcValue The JDBC value
	 * @return The value required by the POJO
	 */
	public abstract T convertToPojo(Object jdbcValue);
	
	/**
	 * Implement this method to convert the value from your POJO to the
	 * value to insert using a PreparedStatement
	 * @param javaValue The Java model value
	 * @return The Value as it appears in the database (JDBC)
	 */
	public abstract Object pojoToDatabase(Object javaValue);
}
