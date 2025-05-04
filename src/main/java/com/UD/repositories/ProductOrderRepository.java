package com.UD.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.UD.Entity.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer>{

	List<ProductOrder> findByUserId(int userId);

	ProductOrder findByOrderId(String orderId);

}
