package com.UD.implementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.UD.Entity.Category;
import com.UD.repositories.CategoryRepository;
import com.UD.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Category saveCategory(Category category) {
		
		return categoryRepository.save(category);
	}
	
	@Override
	public Boolean existCategory(String name) {
		
		return categoryRepository.existsByName(name);
	}

	@Override
	public List<Category> getAllCategories() {
	
		return categoryRepository.findAll();
	}

	@Override
	public boolean deleteCategory(int id) {
		
		Category category = categoryRepository.findById(id).orElse(null);
		
		if (!ObjectUtils.isEmpty(category)) {
			categoryRepository.delete(category);
			return true;
		}
		
		return false;
	}

	@Override
	public Category getCategoryById(int id) {
		
		Category category = categoryRepository.findById(id).orElse(null);
		
		return category;
	}

	@Override
	public List<Category> getAllActiveCategories() {
			
			List<Category> categories = categoryRepository.findByIsActiveTrue();
		
		return categories;
	}

	@Override
	public Page<Category> getAllCategoriesPagination(int pageNo , int pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		
		return categoryRepository.findAll(pageable);
	}

	
}
