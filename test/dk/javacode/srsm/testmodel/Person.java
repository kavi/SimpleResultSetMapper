package dk.javacode.srsm.testmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.converters.BooleanCharJdbcConverter;

@Table(name = "persons")
public class Person implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	@Column(primaryKey = true, name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "age", dataConverter = NumberJdbcConverter.class)
	private int age;
	
	@Column(name = "smoker", dataConverter = BooleanCharJdbcConverter.class)	
	private boolean smoker;

	private String randomProperty;

	@Column(name = "address_id", fieldReference = "id2")
	private Address address;

	@Column(name = "car_person_id", columnReference = "person_id", collection = true, collectionType = Car.class)
	private List<Car> cars = new ArrayList<Car>();

	public Person() {
		super();
	}

	public Person(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Car> getCars() {
		return cars;
	}

	public void setCars(List<Car> cars) {
		this.cars = cars;
	}
	
	public void addCar(Car c) {
		this.cars.add(c);
	}

	public boolean isSmoker() {
		return smoker;
	}

	public void setSmoker(boolean smoker) {
		this.smoker = smoker;
	}

	public String getRandomProperty() {
		return randomProperty;
	}

	public void setRandomProperty(String randomProperty) {
		this.randomProperty = randomProperty;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", age=" + age + ", smoker=" + smoker + ", address=" + address
				+ ", cars=" + cars + "]";
	}

}
