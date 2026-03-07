package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.CategoryRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.CategoryResponseDTO;
import com.liaw.dev.GraoMestre.entity.Category;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static Category toEntity(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public static CategoryResponseDTO toResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    public static List<CategoryResponseDTO> toResponseDTOList(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(CategoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(CategoryRequestDTO dto, Category category) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
    }
}