package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.ProductRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ProductResponseDTO;
import com.liaw.dev.GraoMestre.entity.Category;
import com.liaw.dev.GraoMestre.entity.Product;
import com.liaw.dev.GraoMestre.exception.exceptions.ConflitException;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.ProductMapper;
import com.liaw.dev.GraoMestre.repository.CategoryRepository;
import com.liaw.dev.GraoMestre.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAllProducts() {
        List<Product> products = productRepository.findAll();
        return ProductMapper.toResponseDTOList(products);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + categoryId));
        List<Product> products = productRepository.findByCategory_Id(categoryId);
        return ProductMapper.toResponseDTOList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0 || minPrice.compareTo(maxPrice) > 0) {
            throw new ConflitException("Preços mínimo e máximo inválidos.");
        }
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return ProductMapper.toResponseDTOList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByNameOrDescription(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ConflitException("Termo de busca não pode ser vazio.");
        }
        List<Product> products = productRepository.findByNameOrDescriptionContainingIgnoreCase(searchTerm);
        return ProductMapper.toResponseDTOList(products);
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile imageFile) {
        Product product = ProductMapper.toEntity(productRequestDTO);

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + productRequestDTO.getCategoryId()));
        product.setCategory(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = imageStorageService.storeImage(imageFile);
                product.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar a imagem do produto: " + e.getMessage(), e);
            }
        }

        product = productRepository.save(product);
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO, MultipartFile imageFile) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        String oldImageUrl = product.getImageUrl();

        ProductMapper.updateEntityFromDto(productRequestDTO, product);

        if (!product.getCategory().getId().equals(productRequestDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + productRequestDTO.getCategoryId()));
            product.setCategory(category);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String newImageUrl = imageStorageService.storeImage(imageFile);
                product.setImageUrl(newImageUrl);
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    imageStorageService.deleteImage(oldImageUrl);
                }
            } catch (IOException e) {
                throw new RuntimeException("Erro ao atualizar a imagem do produto: " + e.getMessage(), e);
            }
        } else if (productRequestDTO.getImageUrl() == null || productRequestDTO.getImageUrl().isEmpty()) {
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                imageStorageService.deleteImage(oldImageUrl);
            }
            product.setImageUrl(null);
        }

        product = productRepository.save(product);
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        if (!product.getActive()) {
            throw new ConflitException("Produto com ID: " + id + " já está inativo.");
        }

        product.setActive(false);
        product = productRepository.save(product);
        return ProductMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        if (product.getActive()) {
            throw new ConflitException("Produto com ID: " + id + " já está ativo.");
        }

        product.setActive(true);
        product = productRepository.save(product);
        return ProductMapper.toResponseDTO(product);
    }
}