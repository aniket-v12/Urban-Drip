package com.UD.implementation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.UD.Entity.Cart;
import com.UD.Entity.OrderAddress;
import com.UD.Entity.OrderRequest;
import com.UD.Entity.ProductOrder;
import com.UD.helpers.EmailOrder;
import com.UD.helpers.OrderStatus;
import com.UD.repositories.CartRepository;
import com.UD.repositories.ProductOrderRepository;
import com.UD.services.ProductOrderService;

@Service
public class ProductOrderServiceImpl implements ProductOrderService{

	@Autowired
	private ProductOrderRepository productOrderRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private EmailOrder emailOrder;
	
	@Override
	public void saveOrder(int userId, OrderRequest orderRequest) {
		
		List<Cart> carts = cartRepository.findByUserId(userId);

		for (Cart cart : carts) {
		    ProductOrder order = new ProductOrder();

		    order.setOrderId(UUID.randomUUID().toString());
		    order.setOrderDate(LocalDate.now());

		    order.setProduct(cart.getProduct());
		    order.setPrice(cart.getProduct().getDiscountPrice());

		    order.setQuantity(cart.getQuantity());
		    order.setUser(cart.getUser());

		    order.setStatus(OrderStatus.IN_PROGRESS.getName());
		    order.setPaymentType(orderRequest.getPaymentType());

		    OrderAddress address = new OrderAddress();
		    address.setFirstName(orderRequest.getFirstName());
		    address.setLastName(orderRequest.getLastName());
		    address.setEmail(orderRequest.getEmail());
		    address.setMobileNumber(orderRequest.getMobileNumber());
		    address.setAddress(orderRequest.getAddress());
		    address.setCity(orderRequest.getCity());
		    address.setState(orderRequest.getState());
		    address.setPincode(orderRequest.getPincode());

		    order.setOrderAddress(address);

		    ProductOrder productOrder = productOrderRepository.save(order);
		    try {
				emailOrder.sendEmailForProductOrder(productOrder, "Success");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public List<ProductOrder> getOrderByUser(int userId) {

		List<ProductOrder> orders = productOrderRepository.findByUserId(userId);
		
		return orders;
	}

	@Override
	public ProductOrder upateOrderStatus(int orderId, String status) {
		
		 Optional<ProductOrder> user = productOrderRepository.findById(orderId);
		
		if (user.isPresent()) {
			
			ProductOrder productOrder = user.get();
			productOrder.setStatus(status);
			ProductOrder updatedProductOrder = productOrderRepository.save(productOrder);
			
			return updatedProductOrder;
		}
		
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		
		return productOrderRepository.findAll();
	}

	@Override
	public ProductOrder getOrderByOrderID(String orderId) {
		
		return productOrderRepository.findByOrderId(orderId);
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(int pageNo, int pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		
		return productOrderRepository.findAll(pageable);
	}

}
