package com.college.shinecart.service;

import com.college.shinecart.dto.CreateProductRequest;
import com.college.shinecart.dto.ProductDTO;
import com.college.shinecart.dto.ProductDetailDTO;
import com.college.shinecart.entity.Product;
import com.college.shinecart.exception.BadRequestException;
import com.college.shinecart.exception.ResourceNotFoundException;
import com.college.shinecart.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private final String UPLOAD_DIR = "uploads/products/";

    // ──────────────────────────────────────────────
    //  GET ALL PRODUCTS (public - only In Stock &
    //  Limited Stock, NOT Out of Stock or In Auction)
    // ──────────────────────────────────────────────
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(p -> !"Out of Stock".equals(p.getAvailability())
                        && !"In Auction".equals(p.getAvailability()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  GET ALL PRODUCTS (admin - every product,
    //  including Out of Stock and In Auction)
    // ──────────────────────────────────────────────
    public List<ProductDTO> getAllProductsAdmin() {
        return productRepository.findAll().stream()
                .map(this::convertToAdminDTO)   // uses richer DTO
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  GET PRODUCT BY ID
    // ──────────────────────────────────────────────
    public ProductDetailDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return convertToDetailDTO(product);
    }

    // ──────────────────────────────────────────────
    //  GET BY CATEGORY
    // ──────────────────────────────────────────────
    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .filter(p -> !"Out of Stock".equals(p.getAvailability())
                        && !"In Auction".equals(p.getAvailability()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  FILTER
    // ──────────────────────────────────────────────
    public List<ProductDTO> getProductsByFilters(String category, String metal,
                                                 Double minPrice, Double maxPrice) {
        return productRepository.findByFilters(category, metal, minPrice, maxPrice)
                .stream()
                .filter(p -> !"Out of Stock".equals(p.getAvailability())
                        && !"In Auction".equals(p.getAvailability()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  SEARCH
    // ──────────────────────────────────────────────
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .filter(p -> !"Out of Stock".equals(p.getAvailability())
                        && !"In Auction".equals(p.getAvailability()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  CREATE
    // ──────────────────────────────────────────────
    public ProductDetailDTO createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .category(request.getCategory())
                .metal(request.getMetal())
                .stone(request.getStone())
                .discount(request.getDiscount())
                .badge(request.getBadge())
                .sku(request.getSku())
                .availability(request.getAvailability() != null
                        ? request.getAvailability() : "In Stock")
                .sizes(request.getSizes())
                .description(request.getDescription())
                .features(request.getFeatures())
                .specifications(request.getSpecifications())
                .stockQuantity(request.getStockQuantity() != null
                        ? request.getStockQuantity() : 0)
                .rating(0.0)
                .reviews(0)
                .build();

        product = productRepository.save(product);
        return convertToDetailDTO(product);
    }

    // ──────────────────────────────────────────────
    //  UPDATE
    // ──────────────────────────────────────────────
    public ProductDetailDTO updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategory(request.getCategory());
        product.setMetal(request.getMetal());
        product.setStone(request.getStone());
        product.setDiscount(request.getDiscount());
        product.setBadge(request.getBadge());
        product.setSku(request.getSku());
        product.setAvailability(request.getAvailability());
        product.setSizes(request.getSizes());
        product.setDescription(request.getDescription());
        product.setFeatures(request.getFeatures());
        product.setSpecifications(request.getSpecifications());
        product.setStockQuantity(request.getStockQuantity());

        product = productRepository.save(product);
        return convertToDetailDTO(product);
    }

    // ──────────────────────────────────────────────
    //  TOGGLE AVAILABILITY
    //  Cycles: In Stock → Out of Stock → In Stock
    //  (Never touches "In Auction" or "Limited Stock"
    //   via this toggle — those are set by other flows)
    // ──────────────────────────────────────────────
    public String toggleAvailability(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Guard: don't toggle products currently in auction
        if ("In Auction".equals(product.getAvailability())) {
            throw new BadRequestException(
                    "Cannot toggle availability. Product is currently in an active auction.");
        }

        // Toggle logic
        String current = product.getAvailability();
        String newAvailability;

        if ("Out of Stock".equals(current)) {
            newAvailability = "In Stock";
        } else {
            // In Stock or Limited Stock → Out of Stock
            newAvailability = "Out of Stock";
        }

        product.setAvailability(newAvailability);
        productRepository.save(product);
        return newAvailability;
    }

    // ──────────────────────────────────────────────
    //  DELETE
    // ──────────────────────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if ("In Auction".equals(product.getAvailability())) {
            throw new BadRequestException(
                    "Cannot delete product. It is currently in an active auction.");
        }

        if (orderRepository.existsByProductId(id)) {
            throw new BadRequestException(
                    "Cannot delete product. It is referenced in existing orders.");
        }

        if (auctionRepository.existsByProductId(id)) {
            throw new BadRequestException(
                    "Cannot delete product. It has auction history. Please delete the auctions first.");
        }

        cartRepository.deleteByProductId(id);
        wishlistRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    // ──────────────────────────────────────────────
    //  IMAGE UPLOAD
    // ──────────────────────────────────────────────
    public String uploadImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/products/" + filename;
    }

    public void updateProductImages(Long id, List<String> imageUrls) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!imageUrls.isEmpty()) {
            product.setImage(imageUrls.get(0));
        }
        product.setImages(String.join(",", imageUrls));
        productRepository.save(product);
    }

    // ──────────────────────────────────────────────
    //  DTO CONVERTERS
    // ──────────────────────────────────────────────

    // Public-facing DTO (lightweight)
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .rating(product.getRating())
                .reviews(product.getReviews())
                .image(product.getImage())
                .category(product.getCategory())
                .metal(product.getMetal())
                .discount(product.getDiscount())
                .badge(product.getBadge())
                .availability(product.getAvailability())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .stone(product.getStone())
                .build();
    }

    // Admin DTO — same as public but always includes
    // availability + stockQuantity regardless of status
    private ProductDTO convertToAdminDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .rating(product.getRating())
                .reviews(product.getReviews())
                .image(product.getImage())
                .category(product.getCategory())
                .metal(product.getMetal())
                .discount(product.getDiscount())
                .badge(product.getBadge())
                .sku(product.getSku())
                .stone(product.getStone())
                .availability(product.getAvailability())       // always included
                .stockQuantity(product.getStockQuantity())     // always included
                .build();
    }

    // Detail DTO (used in product detail page)
    private ProductDetailDTO convertToDetailDTO(Product product) {
        List<String> imagesList = product.getImages() != null
                ? Arrays.asList(product.getImages().split(","))
                : List.of();

        List<String> sizesList = product.getSizes() != null
                ? Arrays.asList(product.getSizes().split(","))
                : List.of();

        List<String> featuresList   = parseJsonArray(product.getFeatures());
        List<ProductDetailDTO.Specification> specsList = parseSpecifications(product.getSpecifications());

        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .rating(product.getRating())
                .reviews(product.getReviews())
                .category(product.getCategory())
                .metal(product.getMetal())
                .stone(product.getStone())
                .discount(product.getDiscount())
                .badge(product.getBadge())
                .sku(product.getSku())
                .availability(product.getAvailability())
                .stockQuantity(product.getStockQuantity())
                .images(imagesList)
                .sizes(sizesList)
                .description(product.getDescription())
                .features(featuresList)
                .specifications(specsList)
                .build();
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ProductDetailDTO.Specification> parseSpecifications(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<ProductDetailDTO.Specification>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}