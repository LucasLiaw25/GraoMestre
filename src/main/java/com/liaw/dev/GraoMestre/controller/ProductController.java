package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.ProductRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ProductResponseDTO;
import com.liaw.dev.GraoMestre.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')") // Users can browse products
    public ResponseEntity<List<ProductResponseDTO>> findAllProducts() {
        return ResponseEntity.ok(productService.findAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<ProductResponseDTO> findProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findProductById(id));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<List<ProductResponseDTO>> findProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.findProductsByCategory(categoryId));
    }

    @GetMapping("/price-range")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<List<ProductResponseDTO>> findProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.findProductsByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_USER')")
    public ResponseEntity<List<ProductResponseDTO>> findProductsByNameOrDescription(@RequestParam String searchTerm) {
        return ResponseEntity.ok(productService.findProductsByNameOrDescription(searchTerm));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestPart("product") @Valid ProductRequestDTO productRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequestDTO, imageFile));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") @Valid ProductRequestDTO productRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        return ResponseEntity.ok(productService.updateProduct(id, productRequestDTO, imageFile));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ProductResponseDTO> deactivateProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deactivateProduct(id));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ProductResponseDTO> activateProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.activateProduct(id));
    }
}