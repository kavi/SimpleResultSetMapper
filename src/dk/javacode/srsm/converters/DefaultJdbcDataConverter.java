package dk.javacode.srsm.converters;

/**
 * This is the default data converter which acts as a simple
 * pass-through - no conversion is taking place in this class.
 * 
 * @author Kavi
 *
 */
public class DefaultJdbcDataConverter implements JdbcDataConverter<Object> {

	@Override
	public Object convertToPojo(Object o) {
		return o;
	}

	@Override
	public Object pojoToDatabase(Object o) {
		return o;
	}
}
