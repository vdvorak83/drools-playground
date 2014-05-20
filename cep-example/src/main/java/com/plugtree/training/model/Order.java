package com.plugtree.training.model;

import java.util.UUID;

public class Order {

	private final String id;
	private String val = "";
	
	public Order() {
		this.id = UUID.randomUUID().toString();
	}

	public Order(String val) {
		this();
		this.val = val;
	}
	
	public String getVal() {
		return val;
	}
	
	public void setVal(String val) {
		this.val = val;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "Order " + id;
	}
}
