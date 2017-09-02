package dk.javacode.srsm;

import java.util.List;

public class SqlSelectColumn
{
	private String column;
	private Object value;
	private SqlOperator operator;
	private SqlCondition nextCondition;

	private Object[] listValue;

	public SqlSelectColumn(String column, Object value)
	{
		this(SqlCondition.AND, column, SqlOperator.EQUALS, value);

	}

	public SqlSelectColumn(String column, SqlOperator operator, Object value)
	{
		this(SqlCondition.AND, column, operator, value);
	}

	public SqlSelectColumn(SqlCondition previousCondition, String column, SqlOperator operator, Object value)
	{
		super();
		if (operator.isListOperator()) {
			throw new IllegalArgumentException("Cannot set non-list value on list operator: " + operator.getValue());
		}	
		this.column = column;	
		this.value = value;
		this.operator = operator;
		this.nextCondition = previousCondition;
	}

	public SqlSelectColumn(String column, List<Object> listValue)
	{
		this(SqlCondition.AND, column, SqlOperator.EQUALS, listValue);

	}

	public SqlSelectColumn(String column, SqlOperator operator, Object[] listValue)
	{
		this(SqlCondition.AND, column, operator, listValue);
	}

	public SqlSelectColumn(SqlCondition previousCondition, String column, SqlOperator operator, Object[] listValue)
	{
		super();
		if (!operator.isListOperator()) {
			throw new IllegalArgumentException("Cannot set list value on non-list operator: " + operator.getValue());
		}	
		this.column = column;
		this.listValue = listValue;
		this.operator = operator;
		this.nextCondition = previousCondition;
	}

	public String getColumn()
	{
		return column;
	}

	public Object getValue()
	{
		return value;
	}

	public Object[] getListValue()
	{
		return listValue;
	}

	public SqlOperator getOperator()
	{
		return operator;
	}

	public SqlCondition getPreviousCondition()
	{
		return nextCondition;
	}

	@Override
	public String toString()
	{
		return "SqlSelectColumn [column=" + column + ", value=" + value + ", operator=" + operator + "]";
	}

	public static class SqlCondition
	{
		private String value;

		public static final SqlCondition AND = new SqlCondition("AND");
		public static final SqlCondition OR = new SqlCondition("OR");

		public SqlCondition(String value)
		{
			super();
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}
	}

	public static class SqlOperator
	{
		private String value;
		private boolean listOperator;

		public static final SqlOperator NOT_IN = new SqlOperator(true, "NOT IN");
		public static final SqlOperator IN = new SqlOperator(true, "IN");
		public static final SqlOperator LIKE = new SqlOperator(false, "LIKE");
		public static final SqlOperator EQUALS = new SqlOperator(false, "=");
		public static final SqlOperator GREATER_THAN = new SqlOperator(false, ">");
		public static final SqlOperator LESS_THAN = new SqlOperator(false, "<");

		public SqlOperator(boolean listOperator, String value)
		{
			super();
			this.listOperator = listOperator;
			this.value = value;
		}

		public boolean isListOperator()
		{
			return listOperator;
		}

		public String getValue()
		{
			return value;
		}

		@Override
		public String toString()
		{
			return "SqlOperator [value=" + value + "]";
		}

	}

}
