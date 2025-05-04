package com.UD.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.UD.Entity.OrderRequest;
import com.UD.Entity.ProductOrder;

public interface ProductOrderService {

	public void saveOrder(int userId, OrderRequest orderRequest);
	
	public List<ProductOrder> getOrderByUser(int userId);
	
	public ProductOrder upateOrderStatus(int orderId, String status);
	
	public List<ProductOrder> getAllOrders();
	
	public ProductOrder getOrderByOrderID(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagination(int pageNo, int pageSize);
}
