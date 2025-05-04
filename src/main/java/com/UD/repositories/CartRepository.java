package com.UD.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.UD.Entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer>{

	public Cart findByProductIdAndUserId(int productId, int userId);
	
	public int countByUserId(int userId);
	
	public List<Cart> findByUserId(int userId);
}