package dk.javacode.srsm.helpers;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

public class DefaultSqlTypeConverter {

	public DefaultSqlTypeConverter() {
	}
	
	public int getSqlTypeFromClass(Class<?> clazz) {
		if (clazz.equals(Integer.class)) {
			return Types.INTEGER;
		} else if (clazz.equals(Long.class)) {
			return Types.BIGINT;
		} else if (clazz.equals(Boolean.class)) {
			return Types.BIT;
		} else if (clazz.equals(Float.class)) {
			return Types.FLOAT;
		} else if (clazz.equals(Double.class)) {
			return Types.DOUBLE;
		} else if (clazz.equals(String.class)) {
			return Types.VARCHAR;
		} else if (clazz.equals(Character.class)) {
			return Types.CHAR;
		} else if (clazz.equals(Date.class)) {
			return Types.DATE;
		} else if (clazz.equals(Short.class)) {
			return Types.SMALLINT;
		} else if (clazz.equals(Byte.class)) {
			return Types.TINYINT;
		} else if (clazz.equals(BigDecimal.class)) {
			return Types.NUMERIC;
		} else if (clazz.isArray()) {
			if (clazz.getComponentType().equals(byte.class) || clazz.getComponentType().equals(Byte.class))
			return Types.BINARY;
		}
		// TODO - it should be possible to specify the SqlType on a column
		return Types.VARCHAR;
	}
	
	public int getSqlType(Object obj) {
		return getSqlTypeFromClass(obj.getClass());
	}

}
