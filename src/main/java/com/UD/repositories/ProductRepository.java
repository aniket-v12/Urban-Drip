package com.UD.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.UD.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

	public List<Product> findByIsActiveTrue();

	public List<Product> findByCategory(String category);

	List<Product> findByNameContainingIgnoreCase(String character); 
	
	Page<Product> findByIsActiveTrue(Pageable pageable);

	public Page<Product> findByCategory(Pageable pageable, String category);

	public Page<Product> findByNameContainingIgnoreCase(String character, Pageable pageable);

	public Page<Product> findByIsActiveTrueAndNameContainingIgnoreCase(String character, Pageable pageable);
}
