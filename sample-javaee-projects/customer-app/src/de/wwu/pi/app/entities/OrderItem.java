package de.wwu.pi.app.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class OrderItem {

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int itemId;
	
	@NotNull
	private String name;
	
	@Min(value=1) @Max(value=20)
	private int price;
	
	@ManyToOne
	private CustomerOrder order;
	
	@Override
	public String toString() {
		String orderId = order != null ? ""+order.getOrderId() : "<null>";
		return "OrderItem(itemId="+this.itemId+", name="+this.name+", price="+ price + ", order.id="+orderId+")";
	}
	
	public OrderItem() {
	}
	
	public OrderItem(String name, int price, CustomerOrder order) {
		this.name = name;
		this.price = price;
		this.order = order;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public CustomerOrder getOrder() {
		return order;
	}

	public void setOrder(CustomerOrder order) {
		this.order = order;
	}
	
	
}
