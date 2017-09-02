package dk.javacode.srsm.testmodel;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;

@Table(name = "car")
public class Car {

	@Column(primaryKey = true, name = "id")
	private Integer id;

	@Column(name = "registration")
	private String registration;

	@Column(name = "car_type_id", fieldReference = "id")
	private CarType carType;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	public CarType getCarType() {
		return carType;
	}

	public void setCarType(CarType carType) {
		this.carType = carType;
	}

	@Override
	public String toString() {
		return "Car [id=" + id + ", model=" + registration + ", carType=" + carType + "]";
	}


}
