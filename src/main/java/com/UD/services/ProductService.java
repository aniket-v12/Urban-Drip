package com.UD.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.Product;

public interface ProductService {

	public Product saveProduct(Product product);
	
	public List<Product> getAllProducts();
	
	public boolean deleteProduct(int id);
	
	public Product getProductById(int id);
	
	public Product updateProduct(Product product, MultipartFile file);
	
	public List<Product> getAllActiveProducts(String category);
	
	public List<Product> searchProduct(String character);
	
	public Page<Product> getAllActiveProductsPagination(int pageNo, int pageSize, String category);
	
	public Page<Product> searchProductPagination(int pageNo, int pageSize, String character);
	
	public Page<Product> getAllProductsPagination(int pageNo, int pageSize);

	public Page<Product> searchActiveProductPagination(int pageNo, int pageSize , String category, String character);
}
