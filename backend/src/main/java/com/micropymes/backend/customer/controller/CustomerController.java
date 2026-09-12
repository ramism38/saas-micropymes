package com.micropymes.backend.customer.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.customer.dto.CreateCustomerRequest;
import com.micropymes.backend.customer.dto.CustomerResponse;
import com.micropymes.backend.customer.dto.UpdateCustomerRequest;
import com.micropymes.backend.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/customers")
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

        private final CustomerService customerService;

        public CustomerController(
                        CustomerService customerService) {
                this.customerService = customerService;
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public CustomerResponse create(
                        @PathVariable UUID organizationId,
                        @Valid @RequestBody CreateCustomerRequest request,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                return customerService.create(
                                organizationId,
                                principal.userId(),
                                request);
        }

        @GetMapping
        public PageResponse<CustomerResponse> findAll(
                        @PathVariable UUID organizationId,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "false") boolean archived,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                return customerService.findAll(
                                organizationId,
                                principal.userId(),
                                q,
                                archived,
                                page,
                                size);
        }

        @GetMapping("/{customerId}")
        public CustomerResponse findById(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID customerId,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                return customerService.findById(
                                organizationId,
                                customerId,
                                principal.userId());
        }

        @PatchMapping("/{customerId}")
        public CustomerResponse update(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID customerId,
                        @Valid @RequestBody UpdateCustomerRequest request,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                return customerService.update(
                                organizationId,
                                customerId,
                                principal.userId(),
                                request);
        }

        @PostMapping("/{customerId}/archive")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void archive(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID customerId,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                customerService.archive(
                                organizationId,
                                customerId,
                                principal.userId());
        }

        @PostMapping("/{customerId}/restore")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void restore(
                        @PathVariable UUID organizationId,
                        @PathVariable UUID customerId,
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                customerService.restore(
                                organizationId,
                                customerId,
                                principal.userId());
        }
}