package com.UD.helpers;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class EmailRequest {
	
	@Autowired
	private  JavaMailSender mailSender;

	public  boolean sendEmail(String url, String email) throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("dineshkumarnadar01@gmail.com", "UrbanDrip");
		helper.setTo(email);
		
		String content = "<p>Hey Hi, </p>" + "<p>You have requested to reset your password.</p>" 
			    + "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url 
			    + "\">Change my password</a></p>";

		helper.setSubject("Password Reset");
		helper.setText(content, true);
		
		mailSender.send(message);
		
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		
		
		    String scheme = request.getScheme();             // http or https
		    String serverName = request.getServerName();     // localhost or your domain
		    int serverPort = request.getServerPort();        // 8080, 80, etc.
		    String contextPath = request.getContextPath();   // e.g., /Urban_Drip

		    StringBuilder url = new StringBuilder();
		    url.append(scheme).append("://").append(serverName);

		    if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
		        url.append(":").append(serverPort);
		    }

		    url.append(contextPath); // Add context path if present

		    return url.toString();
		

	}
}
