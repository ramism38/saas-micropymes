package com.micropymes.backend.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @Size(max = 150)
        @Pattern(
                regexp = ".*\\S.*",
                message = "must not be blank"
        )
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