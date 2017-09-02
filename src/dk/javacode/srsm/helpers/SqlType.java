package dk.javacode.srsm.helpers;

import java.sql.Types;

import dk.javacode.srsm.exceptions.MappingRuntimeException;

public enum SqlType
{
	INTEGER(Types.INTEGER), BIGINT(Types.BIGINT), BIT(Types.BIT), FLOAT(Types.FLOAT), DOUBLE(Types.DOUBLE), VARCHAR(Types.VARCHAR), CHAR(Types.CHAR), DATE(
					Types.DATE), SMALLINT(Types.SMALLINT), TINYINT(Types.TINYINT), NUMERIC(Types.NUMERIC), BINARY(Types.BINARY), OTHER(Types.OTHER), ANY(-1);

	private int type;

	private SqlType(int type)
	{
		this.type = type;
	}

	public int getType()
	{
		if (this == ANY)
		{
			throw new MappingRuntimeException("Error in SimpleResultSetMapper - trying to get sqlType of 'ANY' type");
		}
		return type;
	}
}