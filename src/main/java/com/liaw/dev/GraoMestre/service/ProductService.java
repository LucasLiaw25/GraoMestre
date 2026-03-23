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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Importar Collectors

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${app.base-url}") // Injetar a base URL da aplicação
    private String appBaseUrl;

    @Value("${image.base-path}") // Injetar o base path das imagens
    private String imageBasePath;

    @Autowired
    private ImageStorageService imageStorageService;

    private String buildFullImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        String baseUrl = appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length() - 1) : appBaseUrl;
        String cleanPath = imageBasePath.startsWith("/") ? imageBasePath : "/" + imageBasePath;
        if (!cleanPath.endsWith("/")) cleanPath += "/";

        return baseUrl + cleanPath + fileName;
    }

    private ProductResponseDTO toResponseDTOWithFullImageUrl(Product product) {
        ProductResponseDTO dto = ProductMapper.toResponseDTO(product);
        dto.setImageUrl(buildFullImageUrl(product.getImageUrl()));
        return dto;
    }

    public List<ProductResponseDTO> findAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponseDTOWithFullImageUrl) 
                .collect(Collectors.toList());
    }

    public ProductResponseDTO findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return toResponseDTOWithFullImageUrl(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + categoryId));
        List<Product> products = productRepository.findByCategory_Id(categoryId);
        return products.stream()
                .map(this::toResponseDTOWithFullImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0 || minPrice.compareTo(maxPrice) > 0) {
            throw new ConflitException("Preços mínimo e máximo inválidos.");
        }
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream()
                .map(this::toResponseDTOWithFullImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsByNameOrDescription(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ConflitException("Termo de busca não pode ser vazio.");
        }
        List<Product> products = productRepository.findByNameOrDescriptionContainingIgnoreCase(searchTerm);
        return products.stream()
                .map(this::toResponseDTOWithFullImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile imageFile) {
        Product product = ProductMapper.toEntity(productRequestDTO);

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + productRequestDTO.getCategoryId()));
        product.setCategory(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageFileName = imageStorageService.storeImage(imageFile);
                product.setImageUrl(imageFileName);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar a imagem do produto: " + e.getMessage(), e);
            }
        }

        product = productRepository.save(product);
        return toResponseDTOWithFullImageUrl(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO, MultipartFile imageFile) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + id));

        String oldImageFileName = product.getImageUrl(); // Agora contém apenas o nome do arquivo

        ProductMapper.updateEntityFromDto(productRequestDTO, product);

        if (!product.getCategory().getId().equals(productRequestDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID: " + productRequestDTO.getCategoryId()));
            product.setCategory(category);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // storeImage deve retornar APENAS o nome do novo arquivo
                String newImageFileName = imageStorageService.storeImage(imageFile);
                product.setImageUrl(newImageFileName); // Salva APENAS o nome do novo arquivo no banco

                if (oldImageFileName != null && !oldImageFileName.isEmpty()) {
                    // deleteImage deve receber APENAS o nome do arquivo
                    imageStorageService.deleteImage(oldImageFileName);
                }
            } catch (IOException e) {
                throw new RuntimeException("Erro ao atualizar a imagem do produto: " + e.getMessage(), e);
            }
        }

        product = productRepository.save(product);
        // Retorna o DTO com a URL completa para o frontend
        return toResponseDTOWithFullImageUrl(product);
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
        return toResponseDTOWithFullImageUrl(product); // Retorna DTO com URL completa
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
        return toResponseDTOWithFullImageUrl(product); // Retorna DTO com URL completa
    }
}