package de.wwu.pi.app.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class Customer {

	@Id
	private String id;
	
	@NotNull
	private String name;
	
//	@NotNull
//	private CustomerStatus status;
	@Min(value=0)
	private int status;
	
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	private List<CustomerOrder> orders;
	
	public Customer() {
		this.orders = new ArrayList<CustomerOrder>();
	}

//	public Customer(String customerId, String name, CustomerStatus status) {
	public Customer(String customerId, String name, int status) {
		this.id = customerId;
		this.name = name;
		this.status = status;
		this.orders = new ArrayList<CustomerOrder>();
	}
	
	@Override
	public String toString() {
		int ordersSize = orders != null ? orders.size() : 0;
		return "Customer(customerId="+this.id+", name="+this.name+", status="+ status + ", order.size="+ordersSize+")";
	}

	public String getId() {
		return id;
	}

	public void setId(String customerId) {
		this.id = customerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<CustomerOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<CustomerOrder> orders) {
		this.orders = orders;
	}

	public void addOrder(CustomerOrder o1) {
		this.orders.add(o1);
	}
	
}
