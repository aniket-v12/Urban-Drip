package com.UD.implementation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.UD.Entity.Product;
import com.UD.repositories.ProductRepository;
import com.UD.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {

		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {

		return productRepository.findAll();
	}

	@Override
	public boolean deleteProduct(int id) {

		Product product = productRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(product)) {

			productRepository.delete(product);
			;
			return true;
		}

		return false;
	}

	@Override
	public Product getProductById(int id) {

		Product product = productRepository.findById(id).orElse(null);

		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile file) {

		Product oldProduct = getProductById(product.getId());

		String imageName = file != null && file.isEmpty() ? oldProduct.getImage() : file.getOriginalFilename();

		oldProduct.setName(product.getName());
		oldProduct.setDescription(product.getDescription());
		oldProduct.setCategory(product.getCategory());
		oldProduct.setStock(product.getStock());
		oldProduct.setPrice(product.getPrice());
		oldProduct.setImage(imageName);
		oldProduct.setIsActive(product.getIsActive());

		oldProduct.setDiscount(product.getDiscount());
		double discount = product.getPrice() * (product.getDiscount() / 100.0);
		double discountPrice = product.getPrice() - discount;

		oldProduct.setDiscountPrice(discountPrice);

		Product updatedProduct = productRepository.save(oldProduct);

		if (!ObjectUtils.isEmpty(updatedProduct)) {

			if (!file.isEmpty()) {

				try {

					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
							+ file.getOriginalFilename());
					System.out.println("Category Image Stored Path: " + path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}
			return product;
		}

		return null;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {

		List<Product> activeProducts = null;

		if (ObjectUtils.isEmpty(category)) {

			activeProducts = productRepository.findByIsActiveTrue();
		} else {

			activeProducts = productRepository.findByCategory(category);
		}

		return activeProducts;
	}

	@Override
	public List<Product> searchProduct(String character) {

		return productRepository.findByNameContainingIgnoreCase(character);
	}

	@Override
	public Page<Product> getAllActiveProductsPagination(int pageNo, int pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {

			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {

			pageProduct = productRepository.findByCategory(pageable, category);
		}

		return pageProduct;
	}

	@Override
	public Page<Product> searchProductPagination(int pageNo, int pageSize, String character) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return productRepository.findByNameContainingIgnoreCase(character, pageable);
	}

	@Override
	public Page<Product> getAllProductsPagination(int pageNo, int pageSize) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return productRepository.findAll(pageable);
	}

	@Override
	public Page<Product> searchActiveProductPagination(int pageNo, int pageSize, String category, String character) {

		Page<Product> pageProduct = null;
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		pageProduct = productRepository.findByIsActiveTrueAndNameContainingIgnoreCase(character, pageable);

		return pageProduct; 
	}

}
