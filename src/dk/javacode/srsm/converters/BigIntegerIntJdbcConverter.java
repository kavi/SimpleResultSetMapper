package dk.javacode.srsm.converters;

import java.math.BigInteger;

import dk.javacode.srsm.exceptions.MappingRuntimeException;

public class BigIntegerIntJdbcConverter implements JdbcDataConverter<Integer> {

	@Override
	public Integer convertToPojo(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Long || o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof BigInteger) {
			BigInteger n = (BigInteger) o;
			return n.intValue();
		} else {
			throw new MappingRuntimeException("Unable to convert non-BigInteger(" + o + ") to Long");
		}
	}

	@Override
	public Object pojoToDatabase(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof BigInteger) {
			return o;
		}
		if (o instanceof Long) {
			return BigInteger.valueOf((long) o);
		}
		if (o instanceof Integer) {
			return BigInteger.valueOf((int) o);
		}
		throw new MappingRuntimeException("Unable to convert (" + o + ") to BigInteger");
	}
}