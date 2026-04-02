package com.college.shinecart.controller;

import com.college.shinecart.dto.CreateProductRequest;
import com.college.shinecart.dto.ProductDTO;
import com.college.shinecart.dto.ProductDetailDTO;
import com.college.shinecart.exception.BadRequestException;
import com.college.shinecart.exception.ResourceNotFoundException;
import com.college.shinecart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class ProductController {

    private final ProductService productService;

    // ── Get all products (public - only In Stock) ──
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ── Get all products (admin - all regardless of availability) ──
    @GetMapping("/admin")
    public ResponseEntity<List<ProductDTO>> getAllProductsAdmin() {
        return ResponseEntity.ok(productService.getAllProductsAdmin());
    }

    // ── Get product by ID ──
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── Get products by category ──
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    // ── Search products ──
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    // ── Filter products ──
    @GetMapping("/filter")
    public ResponseEntity<List<ProductDTO>> filterProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String metal,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return ResponseEntity.ok(productService.getProductsByFilters(category, metal, minPrice, maxPrice));
    }

    // ── Create product (Admin only) ──
    @PostMapping
    public ResponseEntity<ProductDetailDTO> createProduct(@RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    // ── Update product (Admin only) ──
    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody CreateProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ── Toggle availability (Admin only) ──
    // PATCH /api/products/{id}/toggle-availability
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@PathVariable Long id) {
        String newAvailability = productService.toggleAvailability(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Availability updated successfully");
        response.put("availability", newAvailability);
        return ResponseEntity.ok(response);
    }

    // ── Delete product (Admin only) ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }

    // ── Upload single image ──
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = productService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }

    // ── Upload multiple images for a product ──
    @PostMapping("/{id}/upload-images")
    public ResponseEntity<Map<String, Object>> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files
    ) {
        if (files == null || files.length == 0) {
            throw new BadRequestException("No files provided");
        }

        boolean hasValidFile = false;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) { hasValidFile = true; break; }
        }
        if (!hasValidFile) {
            throw new BadRequestException("All files are empty");
        }

        try {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    imageUrls.add(productService.uploadImage(file));
                }
            }
            productService.updateProductImages(id, imageUrls);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images uploaded successfully");
            response.put("imageUrls", imageUrls);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }
    }
}