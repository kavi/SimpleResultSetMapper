package dk.javacode.srsm;

public class SqlOrderByColumn {
	private String column;
	private OrderByDirection direction;

	public SqlOrderByColumn(String column, OrderByDirection direction) {
		super();
		this.column = column;
		this.direction = direction;
	}

	public String getColumn() {
		return column;
	}

	public OrderByDirection getDirection() {
		return direction;
	}

	public static enum OrderByDirection {

		ASC("asc"), DESC("desc");

		private String value;

		private OrderByDirection(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

}
