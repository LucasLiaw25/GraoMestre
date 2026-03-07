package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.CategoryRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.CategoryResponseDTO;
import com.liaw.dev.GraoMestre.entity.Category;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.CategoryMapper;
import com.liaw.dev.GraoMestre.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return CategoryMapper.toResponseDTOList(categories);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + id));
        return CategoryMapper.toResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category category = CategoryMapper.toEntity(categoryRequestDTO);
        category = categoryRepository.save(category);
        return CategoryMapper.toResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + id));

        CategoryMapper.updateEntityFromDto(categoryRequestDTO, category);
        category = categoryRepository.save(category);
        return CategoryMapper.toResponseDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada com ID: " + id);
        }

        categoryRepository.deleteById(id);
    }
}