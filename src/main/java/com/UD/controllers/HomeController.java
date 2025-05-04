package com.UD.controllers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.Category;
import com.UD.Entity.Product;
import com.UD.Entity.User;
import com.UD.helpers.EmailRequest;
import com.UD.services.CartService;
import com.UD.services.CategoryService;
import com.UD.services.ProductService;
import com.UD.services.UserService;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailRequest emailRequest;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

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
	public String homeHandler(Model m) {

		List<Category> allActiveCategories = categoryService.getAllActiveCategories().stream()
				.sorted((c1, c2) -> Integer.compare(c2.getId(), c1.getId())).limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> Integer.compare(p2.getId(), p1.getId())).limit(8).toList();
		m.addAttribute("categories", allActiveCategories);
		m.addAttribute("products", allActiveProducts);

		return "home";
	}

	@GetMapping("/products")
	public String productsHandler(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(name = "pageSize", defaultValue = "4") int pageSize,
			@RequestParam(defaultValue = "") String character) {

		m.addAttribute("activeCategories", categoryService.getAllActiveCategories());
		m.addAttribute("paramValue", category);

		Page<Product> page = null;
		if (StringUtils.isEmpty(character)) {

			page = productService.getAllActiveProductsPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchActiveProductPagination(pageNo, pageSize, category, character);
		}

		m.addAttribute("activeProducts", page.getContent());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "products";
	}

	@GetMapping("/product_view/{id}")
	public String viewProduct(@PathVariable int id, Model m) {

		Product product = productService.getProductById(id);

		m.addAttribute("product", product);

		return "product_view";
	}

	@GetMapping("/register")
	public String registerHandler() {

		return "register";
	}

	@PostMapping("/registeringUser")
	public String registeringUser(@ModelAttribute User user, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		boolean existsEmail = userService.existsEmail(user.getEmail());

		if (existsEmail) {

			session.setAttribute("errorMsg", "User already Exists !!!");
		} else {

			String imageName = !file.isEmpty() && file != null ? file.getOriginalFilename() : "Default.jpg";
			user.setProfileImage(imageName);

			User saveUser = userService.saveUser(user);

			if (!ObjectUtils.isEmpty(saveUser)) {

				if (!file.isEmpty()) {

					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());
					System.out.println("User Image Stored Path: " + path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				}

				session.setAttribute("successMsg", "Successfully Registered");
			} 
			else {
				session.setAttribute("successMsg", "Something Went Wrong !!!");
			}
		}

		return "redirect:/register";
	}

	@GetMapping("/login")
	public String loginHandler() {

		return "login";
	}

	@GetMapping("/forgot_password")
	public String forgotPassword() {

		return "forgot_password";
	}

	@PostMapping("/emailSend")
	public String sendingEmailForPasswordReset(@RequestParam String email, HttpSession session,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {

		User userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Email Invalid");
		} else {

			String resetToken = UUID.randomUUID().toString();

			userService.updateUserResetToken(email, resetToken);

			String url = EmailRequest.generateUrl(request) + "/reset_password?token=" + resetToken;

			boolean sendEmail = emailRequest.sendEmail(url, email);

			if (sendEmail) {
				session.setAttribute("successMsg", "Email Sent, Please check your Email");
			} else {
				session.setAttribute("errorMsg", "Something Went Wrong !");
			}

		}

		return "redirect:/forgot_password";
	}

	@GetMapping("/reset_password")
	public String resetPassword(@RequestParam String token, Model m) {

		User userByToken = userService.getUserByToken(token);

		if (userByToken == null) {

			m.addAttribute("errorMsg", "Link is Invalid or Expired");
			return "message";
		}
		m.addAttribute("token", token);

		return "reset_password";
	}

	@PostMapping("/new_password")
	public String newPassword(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword, HttpSession session, Model m) {

		User userByToken = userService.getUserByToken(token);

		if (userByToken == null) {

			m.addAttribute("errorMsg", "Link is Invalid or Expired");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);

			m.addAttribute("msg", "Password Changed Successfully");

			return "message";
		}
	}

	@GetMapping("/search_product")
	public String searchProduct(@RequestParam String character, Model m) {

		List<Product> searchProducts = productService.searchProduct(character);
		m.addAttribute("products", searchProducts);

		m.addAttribute("activeProducts", searchProducts);
		m.addAttribute("activeCategories", categoryService.getAllActiveCategories());

		return "products";
	}

}
