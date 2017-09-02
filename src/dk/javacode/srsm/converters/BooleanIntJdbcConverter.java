package dk.javacode.srsm.converters;

import dk.javacode.srsm.exceptions.MappingRuntimeException;


/**
 * Converts from a '1/0' boolean representation in the database
 * to a Java Boolean value in the model.
 * 
 * Specifically:
 *
 *  - 0 => false => 0
 *  - !0 => true => 1
 *  - null => null => null
 * 
 * @see JdbcDataConverter
 * @author Kavi
 *
 */
public class BooleanIntJdbcConverter implements JdbcDataConverter<Boolean> {

	@Override
	public Boolean convertToPojo(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			Number n = (Number) o;
			return n.intValue() != 0;
		} else {
			throw new MappingRuntimeException("Unable to convert non-number (" + o + ") to boolean");
		}
	}

	@Override
	public Object pojoToDatabase(Object o) {
		if (o == null) {
			return null;
		}
		return ((Boolean) o) ? 1 : 0;
	}

	
}
