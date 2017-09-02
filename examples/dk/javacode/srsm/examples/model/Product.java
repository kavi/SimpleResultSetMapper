package dk.javacode.srsm.examples.model;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;

@Table(name = "product")
public class Product {

	@Column(name = "id", primaryKey = true)
	private Integer id;

	@Column(name = "name")
	private String name;

	public Product() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + "]";
	}

}
