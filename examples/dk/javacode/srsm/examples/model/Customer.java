package dk.javacode.srsm.examples.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;
import dk.javacode.srsm.converters.BigIntegerLongJdbcConverter;

@Table(name = "customer")
public class Customer {

	@Column(primaryKey = true, name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "active")
	private boolean active;

	@Column(name = "password", writeOnly = true)
	private String password;

	@Column(name = "joined")
	private Date joined;

	@Column(name = "customer_order_id", columnReference ="customer_id", collection = true, collectionType = Order.class)
	private List<Order> orderList = new ArrayList<Order>();

	public Customer() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getJoined() {
		return joined;
	}

	public void setJoined(Date joined) {
		this.joined = joined;
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	public void addOrderList(Order order) {
		orderList.add(order);
		if (!this.equals(order.getCustomer())) {
			order.setCustomer(this);
		}
	}

	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Customer other = (Customer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", active=" + active + ", password=" + password + ", joined=" + joined + ", orderList="
				+ orderList + "]";
	}



}
