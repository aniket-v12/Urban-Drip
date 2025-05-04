package com.UD.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.Cart;
import com.UD.Entity.Category;
import com.UD.Entity.OrderRequest;
import com.UD.Entity.ProductOrder;
import com.UD.Entity.User;
import com.UD.helpers.EmailOrder;
import com.UD.helpers.OrderStatus;
import com.UD.services.CartService;
import com.UD.services.CategoryService;
import com.UD.services.ProductOrderService;
import com.UD.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductOrderService productOrderService;
	
	@Autowired
	private EmailOrder emailOrder;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserDetails(Principal principal, Model m) {

		if (principal != null) {

			String email = principal.getName();
			User userByEmail = userService.getUserByEmail(email);

			m.addAttribute("user", userByEmail);

			int cartCount = cartService.getCartCount(userByEmail.getId());
			m.addAttribute("cartCount", cartCount);
		}

		List<Category> allActiveCategories = categoryService.getAllActiveCategories();
		m.addAttribute("categories", allActiveCategories);

	}

	@GetMapping("/")
	public String home() {

		return "user/home";
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam int productId, @RequestParam int userId, HttpSession session) {

		Cart saveCart = cartService.saveCart(productId, userId);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Product Add to Cart Failed");
		} else {
			session.setAttribute("successMsg", "Product Added to Cart");
		}

		return "redirect:/product_view/" + productId;
	}

	@GetMapping("/cart")
	public String viewCart(Principal p, Model m) {

		User user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUserId(user.getId());

		m.addAttribute("carts", carts);

		if (carts.size() > 0) {

			double totalOrderAmount = carts.get(carts.size() - 1).getTotalOrderAmount();
			m.addAttribute("totalOrderAmount", totalOrderAmount);
		}

		return "/user/cart";
	}

	private User getLoggedInUserDetails(Principal principal) {

		String email = principal.getName();
		User user = userService.getUserByEmail(email);

		return user;
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam int cartId) {
		cartService.updateQuantity(sy, cartId);
		return "redirect:/user/cart";
	}

	@GetMapping("/order")
	public String order(Principal p, Model m) {

		User user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUserId(user.getId());

		m.addAttribute("carts", carts);

		if (carts.size() > 0) {

			double orderAmount = carts.get(carts.size() - 1).getTotalOrderAmount();
			double totalOrderAmount = carts.get(carts.size() - 1).getTotalOrderAmount() + 250 + 100;

			m.addAttribute("orderAmount", orderAmount);
			m.addAttribute("totalOrderAmount", totalOrderAmount);
		}

		return "/user/order";
	}

	@PostMapping("/save_order")
	public String saveOrder(@ModelAttribute OrderRequest orderRequest, Principal principal, Model m) {

		System.out.println(orderRequest);

		User user = getLoggedInUserDetails(principal);
		productOrderService.saveOrder(user.getId(), orderRequest);

		m.addAttribute("orderRequest", orderRequest);

		return "redirect:/user/order_success";
	}

	@GetMapping("/order_success")
	public String orderSuccess() {

		return "/user/order_success";
	}

	@GetMapping("/user_orders")
	public String userOrders(Principal principal, Model m) {

		User user = getLoggedInUserDetails(principal);
		List<ProductOrder> orders = productOrderService.getOrderByUser(user.getId());

		m.addAttribute("orders", orders);

		return "/user/user_orders";
	}

	@GetMapping("/cancel_order")
	public String orderCancel(@RequestParam int orderId, @RequestParam int status, HttpSession session) {
		
		OrderStatus[] values = OrderStatus.values();
		String status2 = null;

		for (OrderStatus orderSt : values) {
		    if (orderSt.getId() == status) {
		        status2 = orderSt.getName();
		    }
		}

		ProductOrder updateOrder = productOrderService.upateOrderStatus(orderId, status2);
		
		try {
			emailOrder.sendEmailForProductOrder(updateOrder, status2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!ObjectUtils.isEmpty(updateOrder)) {
			
			session.setAttribute("successMsg", "Order Cancelled");
		} 
		else {
			
			session.setAttribute("errorMsg", "Order Status Not Updated");
		}
		
		return "redirect:/user/user_orders";
	}
	
	@GetMapping("/profile")
	public String profile()
	{
	    return "/user/profile";
	}

	@PostMapping("/update_profile")
	public String updateProfile(@ModelAttribute User user, @RequestParam MultipartFile file, HttpSession session) {
		
	    User updatedUserProfile = userService.updateUserProfile(user, file);
	    
	    if (!ObjectUtils.isEmpty(updatedUserProfile)) {
	    	
	    	session.setAttribute("successMsg", "Profile Updated");
		} 
	    else 
	    {
			session.setAttribute("errorMsg", "Profile cannot be updated");
		}
	    
	    return "redirect:/user/profile";
	}
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam String newPassword,
								 @RequestParam String currentPassword, 
								 Principal principal,
								 HttpSession session) {
		
		User user = getLoggedInUserDetails(principal);
		
		boolean passwordMatch = passwordEncoder.matches(currentPassword, user.getPassword());
		
		if (passwordMatch) {
			
			String encodePassword = passwordEncoder.encode(newPassword);
			user.setPassword(encodePassword);
			User updateUser = userService.updateUser(user);
			
			if (ObjectUtils.isEmpty(updateUser)) {
				
				session.setAttribute("errorMsg", "Password not Updated");
			}
			else {
				session.setAttribute("successMsg", "Password Changed Successfully");
			}
		} 
	    else 
	    {
			session.setAttribute("errorMsg", "Current Password is Incorrect");
		}
		
		
		
	    return "redirect:/user/profile";
	}

}
