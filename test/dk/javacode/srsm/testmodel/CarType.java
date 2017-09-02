package dk.javacode.srsm.testmodel;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;

@Table(name = "car_type")
public class CarType {

	@Column(name = "id", primaryKey = true)
	private Integer id;
	
	@Column(name = "make")
	private String make;
	
	@Column(name = "model")
	private String model;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return "CarType [id=" + id + ", make=" + make + ", model=" + model + "]";
	}	
}
