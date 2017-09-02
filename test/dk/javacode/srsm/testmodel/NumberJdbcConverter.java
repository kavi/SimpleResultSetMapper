package dk.javacode.srsm.testmodel;

import dk.javacode.srsm.converters.JdbcDataConverter;

public class NumberJdbcConverter implements JdbcDataConverter<Integer> {

	@Override
	public Integer convertToPojo(Object o) {
		if (o == null) {
			return null;
		}
		if (!(o instanceof Integer)) {
			throw new RuntimeException("Number converter can only convert integers");
		}
		return ((Integer) o) + 5000;
	}

	@Override
	public Object pojoToDatabase(Object o) {
		return ((Integer) o) - 5000;
	}
}
