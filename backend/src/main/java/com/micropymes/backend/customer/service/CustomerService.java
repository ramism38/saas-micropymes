package com.micropymes.backend.customer.service;

import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.customer.dto.CreateCustomerRequest;
import com.micropymes.backend.customer.dto.CustomerResponse;
import com.micropymes.backend.customer.dto.UpdateCustomerRequest;
import com.micropymes.backend.customer.repository.CustomerRepository;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private static final List<OpportunityStatus> OPEN_STATUSES =
            List.of(
                    OpportunityStatus.NEW,
                    OpportunityStatus.CONTACTED,
                    OpportunityStatus.PROPOSAL_SENT,
                    OpportunityStatus.NEGOTIATION
            );

    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrganizationAccessService accessService;

    public CustomerService(
            CustomerRepository customerRepository,
            OpportunityRepository opportunityRepository,
            OrganizationAccessService accessService
    ) {
        this.customerRepository = customerRepository;
        this.opportunityRepository = opportunityRepository;
        this.accessService = accessService;
    }

    @Transactional
    public CustomerResponse create(
            UUID organizationId,
            UUID userId,
            CreateCustomerRequest request
    ) {
        OrganizationMember membership =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        Customer customer = new Customer(
                membership.getOrganization(),
                request.name().trim()
        );

        customer.setCompanyName(
                normalize(request.companyName())
        );
        customer.setEmail(
                normalize(request.email())
        );
        customer.setPhone(
                normalize(request.phone())
        );
        customer.setNotes(
                normalize(request.notes())
        );

        customerRepository.save(customer);

        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(
            UUID organizationId,
            UUID customerId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        return toResponse(
                findCustomer(
                        organizationId,
                        customerId
                )
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findAll(
            UUID organizationId,
            UUID userId,
            String q,
            boolean archived,
            int page,
            int size
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 1),
                100
        );

        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        String normalizedQuery = normalize(q);

        Page<CustomerResponse> result =
                customerRepository
                        .search(
                                organizationId,
                                normalizedQuery,
                                archived,
                                pageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional
    public CustomerResponse update(
            UUID organizationId,
            UUID customerId,
            UUID userId,
            UpdateCustomerRequest request
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Customer customer =
                findCustomer(
                        organizationId,
                        customerId
                );

        if (customer.isArchived()) {
            throw new ApiException(
                    ErrorCode.CUSTOMER_ARCHIVED
            );
        }

        if (request.name() != null) {
            customer.setName(
                    request.name().trim()
            );
        }

        if (request.companyName() != null) {
            customer.setCompanyName(
                    normalize(request.companyName())
            );
        }

        if (request.email() != null) {
            customer.setEmail(
                    normalize(request.email())
            );
        }

        if (request.phone() != null) {
            customer.setPhone(
                    normalize(request.phone())
            );
        }

        if (request.notes() != null) {
            customer.setNotes(
                    normalize(request.notes())
            );
        }

        return toResponse(customer);
    }

    @Transactional
    public void archive(
            UUID organizationId,
            UUID customerId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Customer customer =
                findCustomer(
                        organizationId,
                        customerId
                );

        if (customer.isArchived()) {
            return;
        }

        boolean hasActiveOpportunities =
                opportunityRepository
                        .existsByCustomer_IdAndOrganization_IdAndArchivedAtIsNullAndStatusIn(
                                customerId,
                                organizationId,
                                OPEN_STATUSES
                        );

        if (hasActiveOpportunities) {
            throw new ApiException(
                    ErrorCode.CUSTOMER_HAS_ACTIVE_OPPORTUNITIES
            );
        }

        customer.archive();
    }

    @Transactional
    public void restore(
            UUID organizationId,
            UUID customerId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Customer customer =
                findCustomer(
                        organizationId,
                        customerId
                );

        if (!customer.isArchived()) {
            return;
        }

        customer.restore();
    }

    private Customer findCustomer(
            UUID organizationId,
            UUID customerId
    ) {
        return customerRepository
                .findByIdAndOrganization_Id(
                        customerId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.CUSTOMER_NOT_FOUND
                        )
                );
    }

    private CustomerResponse toResponse(
            Customer customer
    ) {
        return new CustomerResponse(
                customer.getId(),
                customer.getOrganization().getId(),
                customer.getName(),
                customer.getCompanyName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getNotes(),
                customer.getVersion(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getArchivedAt()
        );
    }

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}