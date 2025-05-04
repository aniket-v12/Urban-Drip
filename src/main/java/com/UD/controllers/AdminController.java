package com.UD.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.Category;
import com.UD.Entity.Product;
import com.UD.Entity.ProductOrder;
import com.UD.Entity.User;
import com.UD.helpers.EmailOrder;
import com.UD.helpers.OrderStatus;
import com.UD.services.CartService;
import com.UD.services.CategoryService;
import com.UD.services.ProductOrderService;
import com.UD.services.ProductService;
import com.UD.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

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
		}
		List<Category> allActiveCategories = categoryService.getAllActiveCategories();
		m.addAttribute("categories", allActiveCategories);

	}

	@GetMapping("/")
	public String home() {

		return "/admin/home";
	}

	// Admin Category page
	@GetMapping("/category")
	public String add_category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {

		/* m.addAttribute("categories", categoryService.getAllCategories()); */

		Page<Category> page = categoryService.getAllCategoriesPagination(pageNo, pageSize);

		m.addAttribute("categories", page.getContent());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/add_category";
	}

	// Admin adding a category
	@PostMapping("/adding_category")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session, Model m) throws IOException {

		String imageName = file != null && !file.isEmpty() ? file.getOriginalFilename() : "Default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {
			session.setAttribute("errorMsg", "Category already Exists !!!");
		} else {

			Category saveCategory = categoryService.saveCategory(category);
			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Category Not saved - Null Error !");
			} else {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("Category Image Stored Path: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("successMsg", "Category Successfully Saved");
			}
		}

		m.addAttribute(category);

		System.out.println("Category Name : " + category.getName());
		System.out.println("Category imageName : " + category.getImageName());
		System.out.println("Category isActive : " + category.getIsActive());

		return "redirect:/admin/category";
	}

	// Admin deleting a category through actions
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {

		boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("successMsg", "Category Successfully Deleted");
		} else {
			session.setAttribute("errorMsg", "Category Cannot Be Deleted");
		}

		return "redirect:/admin/category";
	}

	// Admin updating a category through actions
	@GetMapping("/update_category/{id}")
	public String updateCategory(@PathVariable int id, Model m) {

		Category categoryById = categoryService.getCategoryById(id);
		m.addAttribute("category", categoryById);

		return "/admin/update_category";
	}

	// Admin updated a category
	@PostMapping("/updating_category")
	public String updatingCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getId());

		String imageName = file != null && file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(oldCategory)) {

			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updatedCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updatedCategory)) {

			if (!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("Category Image Stored Path: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}

			session.setAttribute("successMsg", "Category Successfully Updated");
		} else {
			session.setAttribute("errorMsg", "Category Not Be Updated");
		}

		return "redirect:/admin/category";
	}

	// Admin product page

	@GetMapping("/product")
	public String add_product(Model m) {

		List<Category> categories = categoryService.getAllCategories();
		m.addAttribute("categories", categories);

		return "/admin/add_product";
	}

	// Admin adding a product
	@PostMapping("/adding_product")
	public String addingProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session, Model m) throws IOException {

		String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "Default.jpg";

		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());

		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ file.getOriginalFilename());
			System.out.println("Product Image Stored Path: " + path);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("successMsg", "Product Successfully Added");
		} else {
			session.setAttribute("errorMsg", "Product Not Added !!!");
		}

		m.addAttribute("product", product);

		System.out.println("Product Id : " + product.getId());
		System.out.println("Product Name : " + product.getName());
		System.out.println("Product Description : " + product.getDescription());
		System.out.println("Product Category : " + product.getCategory());
		System.out.println("Product Price : " + product.getPrice());
		System.out.println("Product Stock : " + product.getStock());
		System.out.println("Product ImageName : " + product.getImage());

		return "redirect:/admin/product";
	}

	// Admin viewing Products
	@GetMapping("/view_products")
	public String viewProduct(Model m, @RequestParam(defaultValue = "") String character,
			@RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {

		/*
		 * List<Product> Products = null;
		 * 
		 * if (character != null && character.length() > 0) {
		 * 
		 * Products = productService.searchProduct(character); } else {
		 * 
		 * Products = productService.getAllProducts(); }
		 * 
		 * m.addAttribute("allProducts", Products);
		 */

		Page<Product> page = null;

		if (character != null && character.length() > 0) {

			page = productService.searchProductPagination(pageNo, pageSize, character);
		} else {

			page = productService.getAllProductsPagination(pageNo, pageSize);
		}

		m.addAttribute("allProducts", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/view_products";
	}

	// Admin deleting a product through actions
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {

		boolean deleteProduct = productService.deleteProduct(id);

		if (deleteProduct) {
			session.setAttribute("successMsg", "Product Successfully Deleted");
		} else {
			session.setAttribute("errorMsg", "Product Cannot Be Deleted");
		}

		return "redirect:/admin/view_products";
	}

	// Admin updating product through actions
	@GetMapping("/update_product/{id}")
	public String updateProduct(@PathVariable int id, Model m) {

		m.addAttribute("categories", categoryService.getAllCategories());
		m.addAttribute("product", productService.getProductById(id));

		return "/admin/update_product";
	}

	// Admin updated the product
	@PostMapping("/updating_product")
	public String updatingProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session, Model m) throws IOException {

		if (product.getDiscount() < 0 || product.getDiscount() >= 100) {

			session.setAttribute("errorMsg", "Invalid Discount");
		} else {

			Product updatedProduct = productService.updateProduct(product, file);

			if (!ObjectUtils.isEmpty(updatedProduct)) {

				session.setAttribute("successMsg", "Product Updated Successfully");
			} else {
				session.setAttribute("errorMsg", "Product Not Updated");
			}
		}

		return "redirect:/admin/view_products";
	}

	@GetMapping("/users")
	public String getAllUsers(Model m) {

		List<User> users = userService.getAllUsers("ROLE_USER");

		m.addAttribute("users", users);

		return "admin/users";
	}

	@GetMapping("/update_status")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam int id, HttpSession session) {

		boolean status2 = userService.updateAccountStatus(id, status);

		if (status2) {
			session.setAttribute("successMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Account Not Updated");
		}

		return "redirect:/admin/users";
	}

	@GetMapping("/all_orders")
	public String allOrdersOfUsers(Model m, @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(name = "pageSize", defaultValue = "1") int pageSize) {

		/*
		 * List<ProductOrder> allOrders = productOrderService.getAllOrders();
		 * 
		 * m.addAttribute("allOrders", allOrders); m.addAttribute("user_searching",
		 * false);
		 */

		Page<ProductOrder> page = productOrderService.getAllOrdersPagination(pageNo, pageSize);

		m.addAttribute("allOrders", page.getContent());
		m.addAttribute("user_searching", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/all_orders";
	}

	@PostMapping("/status_update")
	public String updateStatus(@RequestParam int orderId, @RequestParam int status, HttpSession session) {

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

			session.setAttribute("successMsg", "Status Updated");
		} else {

			session.setAttribute("errorMsg", "Order Status Not Updated");
		}

		return "redirect:/admin/all_orders";
	}

	@GetMapping("/search_order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(name = "pageSize", defaultValue = "1") int pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = productOrderService.getOrderByOrderID(orderId.trim());

			if (!ObjectUtils.isEmpty(order)) {

				m.addAttribute("searchOrderId", order);
			} else {
				session.setAttribute("errorMsg", "Incorrect Order Id");
			}

			m.addAttribute("user_searching", true);
		} else {

			/*
			 * List<ProductOrder> allOrders = productOrderService.getAllOrders();
			 * 
			 * m.addAttribute("allOrders", allOrders); m.addAttribute("user_searching",
			 * false);
			 */

			Page<ProductOrder> page = productOrderService.getAllOrdersPagination(pageNo, pageSize);

			m.addAttribute("allOrders", page.getContent());
			m.addAttribute("user_searching", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
		}

		return "/admin/all_orders";
	}

	@GetMapping("add_admin")
	public String addAdmin() {

		return "/admin/add_admin";
	}

	@PostMapping("adding_admin")
	public String registeringAdmin(@RequestParam String name, @RequestParam String mobileNumber,
			@RequestParam String email, @RequestParam String address, @RequestParam String city,
			@RequestParam String state, @RequestParam String pincode, @RequestParam String password,
			@RequestParam String confirmPassword, @RequestParam MultipartFile file, HttpSession session)
			throws IOException {

		User user = new User();
		user.setName(name);
		user.setMobileNumber(mobileNumber);
		user.setEmail(email);
		user.setAddress(address);
		user.setCity(city);
		user.setState(state);
		user.setPincode(pincode);
		user.setPassword(password);

		String imageName = !file.isEmpty() && file != null ? file.getOriginalFilename() : "Default.jpg";
		user.setProfileImage(imageName);

		User saveAdmin = userService.saveAdmin(user);

		if (!ObjectUtils.isEmpty(saveAdmin)) {

			if (!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());
				System.out.println("User Image Stored Path: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}

			session.setAttribute("successMsg", "Successfully Added Admin");
		} else {
			session.setAttribute("successMsg", "Something Went Wrong !!!");
		}

		return "redirect:/admin/add_admin";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}

	@PostMapping("/update_profile")
	public String updateProfile(@ModelAttribute User user, @RequestParam MultipartFile file, HttpSession session) {

		User updatedUserProfile = userService.updateUserProfile(user, file);

		if (!ObjectUtils.isEmpty(updatedUserProfile)) {

			session.setAttribute("successMsg", "Profile Updated");
		} else {
			session.setAttribute("errorMsg", "Profile cannot be updated");
		}

		return "redirect:/admin/profile";
	}

	@PostMapping("/change_password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword,
			Principal principal, HttpSession session) {

		User user = getLoggedInUserDetails(principal);

		boolean passwordMatch = passwordEncoder.matches(currentPassword, user.getPassword());

		if (passwordMatch) {

			String encodePassword = passwordEncoder.encode(newPassword);
			user.setPassword(encodePassword);
			User updateUser = userService.updateUser(user);

			if (ObjectUtils.isEmpty(updateUser)) {

				session.setAttribute("errorMsg", "Password not Updated");
			} else {
				session.setAttribute("successMsg", "Password Changed Successfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password is Incorrect");
		}
		return "redirect:/admin/profile";
	}

	private User getLoggedInUserDetails(Principal principal) {

		String email = principal.getName();
		User user = userService.getUserByEmail(email);

		return user;
	}
	
	@GetMapping("/admins")
	public String getAllAdmins(Model m) {

		List<User> admins = userService.getAllAdmins("ROLE_ADMIN");

		m.addAttribute("admins", admins);

		return "admin/admins";
	}
	
	@GetMapping("/update_stat")
	public String updateAdminAccountStatus(@RequestParam Boolean status, @RequestParam int id, HttpSession session) {

		boolean status2 = userService.updateAccountStatus(id, status);

		if (status2) {
			session.setAttribute("successMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Account Not Updated");
		}

		return "redirect:/admin/admins";
	}

}
