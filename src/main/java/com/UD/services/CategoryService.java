package com.UD.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.UD.Entity.Category;

public interface CategoryService {

	public Category saveCategory(Category category);
	
	public Boolean existCategory(String name);
	
	public List<Category> getAllCategories();
	
	public boolean deleteCategory(int id);
	
	public Category getCategoryById(int id);
	
	public List<Category> getAllActiveCategories();
	
	public Page<Category> getAllCategoriesPagination(int pageNo , int pageSize);
}
