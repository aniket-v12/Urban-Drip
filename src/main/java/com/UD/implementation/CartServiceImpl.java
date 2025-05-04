package com.UD.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.UD.Entity.Cart;
import com.UD.Entity.Product;
import com.UD.Entity.User;
import com.UD.repositories.CartRepository;
import com.UD.repositories.ProductRepository;
import com.UD.repositories.UserRepository;
import com.UD.services.CartService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public Cart saveCart(int productId, int userId) {

		User user = userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();

		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

		Cart cart = null;

		if (ObjectUtils.isEmpty(cartStatus)) {
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(user);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountPrice());
		} else {
			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
		}

		Cart saveCart = cartRepository.save(cart);

		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUserId(int userId) {

		List<Cart> carts = cartRepository.findByUserId(userId);

		double totalOrderAmount = 0.0;
		List<Cart> updatedCarts = new ArrayList<>();

		for (Cart cart : carts) {

			double totalPrice = cart.getProduct().getDiscountPrice() * cart.getQuantity();
			cart.setTotalPrice(totalPrice);

			totalOrderAmount = totalOrderAmount + totalPrice;
			cart.setTotalOrderAmount(totalOrderAmount);

			updatedCarts.add(cart);
		}

		return updatedCarts;
	}

	@Override
	public int getCartCount(int userId) {

		int countByUserId = cartRepository.countByUserId(userId);

		return countByUserId;
	}

	@Override
	public void updateQuantity(String sy, int cartId) {

		Cart cart = cartRepository.findById(cartId).get();
		int updatedQuantity;

		if (sy.equalsIgnoreCase("minus")) {
			updatedQuantity = cart.getQuantity() - 1;

			if (updatedQuantity <= 0) {
				cartRepository.delete(cart);
			}
			else {
				cart.setQuantity(updatedQuantity);
				cartRepository.save(cart);
			}
			
		} else {
			updatedQuantity = cart.getQuantity() + 1;
			
			cart.setQuantity(updatedQuantity);
			cartRepository.save(cart);
		}
	}
}
