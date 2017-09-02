package dk.javacode.srsm.examples.sqlexecuter;

public class SqlParam {

	private String name;
	private Object value;
	
	public SqlParam() {
	}

	public SqlParam(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public SqlParam(String name, Object value, int sqlType) {
		super();
		this.name = name;
		this.value = new SqlExecuter.TypedSqlValue(value, sqlType);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "SqlParam [name=" + name + ", value=" + value + "]";
	}
	
	

}
