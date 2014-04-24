package com.plugtree.training.model;

import java.util.UUID;

public class Order {

	private final String id;
	
	public Order() {
		this.id = UUID.randomUUID().toString();
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "Order " + id;
	}
}
