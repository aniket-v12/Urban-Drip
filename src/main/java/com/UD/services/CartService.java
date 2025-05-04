package com.UD.services;

import java.util.List;

import com.UD.Entity.Cart;

public interface CartService {

	public Cart saveCart(int productId, int userId);
	
	public List<Cart> getCartsByUserId(int userId);
	
	public int getCartCount(int userId);
	
	public void updateQuantity(String sy, int cartId);
}
