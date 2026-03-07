package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.ProductRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ProductResponseDTO;
import com.liaw.dev.GraoMestre.entity.Category;
import com.liaw.dev.GraoMestre.entity.Product;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStorage(dto.getStorage());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
        return product;
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setStorage(product.getStorage());
        dto.setImageUrl(product.getImageUrl());
        dto.setRegisterDate(product.getRegisterDate());
        dto.setPrice(product.getPrice());
        dto.setActive(product.getActive());
        if (product.getCategory() != null) {
            dto.setCategory(CategoryMapper.toResponseDTO(product.getCategory()));
        }
        return dto;
    }

    public static List<ProductResponseDTO> toResponseDTOList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(ProductRequestDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStorage(dto.getStorage());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
    }
}