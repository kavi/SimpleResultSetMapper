package dk.javacode.srsm.examples.model;

import dk.javacode.srsm.annotations.Column;
import dk.javacode.srsm.annotations.Table;

@Table(name = "orderline")
public class Order {
	
	@Column(primaryKey = true, name = "id")
	private Integer id;
	
	@Column(name = "product_id", fieldReference = "id")
	private Product product;
		
	@Column(name = "price")
	private float price;
	
	@Column(name = "customer_id", fieldReference = "id")
	private Customer customer;
	
	public Order() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
//		if (!customer.getOrderList().contains(this)) {
//			customer.addOrderList(this);
//		}			
	}
	
	public Customer getCustomer() {
		return customer;
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
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", product=" + product + ", price=" + price + ", customer.id=" + (customer != null ? customer.getId() : "<null>") + "]";
	}
	
	

}
