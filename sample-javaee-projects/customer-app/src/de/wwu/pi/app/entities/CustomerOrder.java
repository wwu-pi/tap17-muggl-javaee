package de.wwu.pi.app.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class CustomerOrder {

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int orderId;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date orderDate;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> orderItems;
	
	@ManyToOne
	private Customer customer;
	
	@Override
	public String toString() {
		int orderItemsSize = orderItems != null ? orderItems.size() : 0;
		String customerId = customer != null ? customer.getId() : "<null>";
		return "Order(orderId="+this.orderId+", orderDate="+this.orderDate+", customer.id="+ customerId + ", orderItems.size="+orderItemsSize+")";
	}

	public CustomerOrder() {
	}
	
	public CustomerOrder(Date orderDate, Customer customer) {
		this.orderDate = orderDate;
		this.customer = customer;
		this.orderItems = new ArrayList<OrderItem>();
	}
	
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public void addItem(OrderItem i1) {
		this.orderItems.add(i1);	
	}
	
	
}
