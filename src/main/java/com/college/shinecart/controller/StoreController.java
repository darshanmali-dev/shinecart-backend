package com.college.shinecart.controller;

import com.college.shinecart.dto.StoreResponse;
import com.college.shinecart.entity.Store;
import com.college.shinecart.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class StoreController {

    private final StoreRepository storeRepository;

    /**
     * Get all active stores
     * GET /api/stores
     */
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        List<Store> stores = storeRepository.findByActiveTrue();

        List<StoreResponse> response = stores.stream()
                .map(this::toStoreResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get stores by city
     * GET /api/stores/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<StoreResponse>> getStoresByCity(@PathVariable String city) {
        List<Store> stores = storeRepository.findByCityAndActiveTrue(city);

        List<StoreResponse> response = stores.stream()
                .map(this::toStoreResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Convert Store entity to StoreResponse DTO
     */
    private StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .city(store.getCity())
                .state(store.getState())
                .pincode(store.getPincode())
                .phone(store.getPhone())
                .openingHours(store.getOpeningHours())
                .workingDays(store.getWorkingDays())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .build();
    }
}