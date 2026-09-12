package com.micropymes.backend.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 150)
        String companyName,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phone,

        String notes

) {
}