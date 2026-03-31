package com.college.shinecart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    @Column(nullable = true)
    private String name;

    @Column(nullable = true, length = 500)
    private String address;

    @Column(nullable = true)
    private String city;

    @Column(nullable = true)
    private String state;

    @Column(nullable = true)
    private String pincode;

    @Column(nullable = true)
    private String phone;

    // Optional fields
    private String email;
    private String landmark;
}