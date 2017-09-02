package dk.javacode.srsm.converters;

import dk.javacode.srsm.exceptions.MappingRuntimeException;

/**
 * Converts from a 'y/n' boolean representation in the database to a Java
 * Boolean value in the model.
 * 
 * Specifically:
 * 
 * - Y => true - N => false - null => null
 * 
 * @see JdbcDataConverter
 * @author Kavi
 * 
 */
public class BooleanCharJdbcConverter implements JdbcDataConverter<Boolean> {

	@Override
	public Boolean convertToPojo(Object o) {
		if (o == null) {
			return null;
		}
//		if (!(o instanceof String) && !(o instanceof Character)) {
//			throw new MappingRuntimeException("Unable to convert non-string to boolean");
//		}
		String s = o.toString();
		if (s.equalsIgnoreCase("y")) {
			return true;
		}
		if (s.equalsIgnoreCase("n")) {
			return false;
		}
		throw new MappingRuntimeException("Unable to convert string (" + s + ") to boolean");
	}

	@Override
	public Object pojoToDatabase(Object o) {
		if (o == null) {
			return null;
		}
		return ((Boolean) o) ? 'Y' : 'N';
	}

}
