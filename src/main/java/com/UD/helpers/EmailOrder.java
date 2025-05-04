package com.UD.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.UD.Entity.ProductOrder;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailOrder {
	
	@Autowired
	private  JavaMailSender mailSender;
	
	String msg = null;

	public boolean sendEmailForProductOrder(ProductOrder productOrder, String status) throws Exception {
		
		 msg = "<p>Hello <b>[[name]],</b><p>"
					+ "<p>Thank you , Order <b>[[orderStatus]]</b>.</p>"
				    + "<h4><p>Product Details:</p></h4>"
				    + "<h3>Name : [[productName]]</h3>"
				    + "<h3>Category : [[category]]</h3>"
				    + "<h3>Quantity : [[quantity]]</h3>"
				    + "<h3>Price : [[price]]</h3>"
				    + "<h3>Payment Type : [[paymentType]]</h3>";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("dineshkumarnadar01@gmail.com", "UrbanDrip");
		helper.setTo(productOrder.getOrderAddress().getEmail());
		
		msg = msg.replace("[[name]]", productOrder.getOrderAddress().getFirstName());
		msg = msg.replace("[[orderStatus]]", status);
		msg = msg.replace("[[productName]]", productOrder.getProduct().getName());
		msg = msg.replace("[[category]]", productOrder.getProduct().getCategory());
		msg = msg.replace("[[quantity]]", productOrder.getQuantity().toString());
		msg = msg.replace("[[price]]", productOrder.getPrice().toString());
		msg = msg.replace("[[paymentType]]", productOrder.getPaymentType());



		helper.setSubject("Product Ordered Status");
		helper.setText(msg, true);
		
		mailSender.send(message);
		
		return true;
	}
	
}
