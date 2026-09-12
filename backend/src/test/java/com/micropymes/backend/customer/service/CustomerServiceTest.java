package com.micropymes.backend.customer.service;

import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.customer.dto.UpdateCustomerRequest;
import com.micropymes.backend.customer.repository.CustomerRepository;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OrganizationAccessService accessService;

    private CustomerService customerService;

    private Organization organization;
    private Customer customer;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        customerService = new CustomerService(
                customerRepository,
                opportunityRepository,
                accessService
        );

        organization =
                new Organization(
                        "Empresa",
                        "EUR"
                );

        User user =
                new User(
                        "user@test.com",
                        "hash",
                        "Test",
                        "User"
                );

        OrganizationMember member =
                new OrganizationMember(
                        organization,
                        user,
                        OrganizationRole.OWNER
                );

        customer =
                new Customer(
                        organization,
                        "Cliente"
                );

        when(
                accessService.requireMember(
                        organizationId,
                        userId
                )
        ).thenReturn(member);
    }

    @Test
    void archivedCustomerCannotBeEdited() {

        customer.archive();

        when(
                customerRepository
                        .findByIdAndOrganization_Id(
                                customerId,
                                organizationId
                        )
        ).thenReturn(Optional.of(customer));

        UpdateCustomerRequest request =
                new UpdateCustomerRequest(
                        "Nuevo nombre",
                        null,
                        null,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                customerService.update(
                        organizationId,
                        customerId,
                        userId,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.CUSTOMER_ARCHIVED
                    );
                });
    }

    @Test
    void customerWithOpenOpportunitiesCannotBeArchived() {

        when(
                customerRepository
                        .findByIdAndOrganization_Id(
                                customerId,
                                organizationId
                        )
        ).thenReturn(Optional.of(customer));

        when(
                opportunityRepository
                        .existsByCustomer_IdAndOrganization_IdAndArchivedAtIsNullAndStatusIn(
                                eq(customerId),
                                eq(organizationId),
                                any()
                        )
        ).thenReturn(true);

        assertThatThrownBy(() ->
                customerService.archive(
                        organizationId,
                        customerId,
                        userId
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.CUSTOMER_HAS_ACTIVE_OPPORTUNITIES
                    );
                });

        assertThat(customer.isArchived())
                .isFalse();
    }

    @Test
    void customerWithoutOpenOpportunitiesCanBeArchived() {

        when(
                customerRepository
                        .findByIdAndOrganization_Id(
                                customerId,
                                organizationId
                        )
        ).thenReturn(Optional.of(customer));

        when(
                opportunityRepository
                        .existsByCustomer_IdAndOrganization_IdAndArchivedAtIsNullAndStatusIn(
                                eq(customerId),
                                eq(organizationId),
                                any()
                        )
        ).thenReturn(false);

        customerService.archive(
                organizationId,
                customerId,
                userId
        );

        assertThat(customer.isArchived())
                .isTrue();
    }

    @Test
    void archivedCustomerCanBeRestored() {

        customer.archive();

        when(
                customerRepository
                        .findByIdAndOrganization_Id(
                                customerId,
                                organizationId
                        )
        ).thenReturn(Optional.of(customer));

        customerService.restore(
                organizationId,
                customerId,
                userId
        );

        assertThat(customer.isArchived())
                .isFalse();
    }

    @Test
    void nonexistentCustomerReturnsNotFound() {

        when(
                customerRepository
                        .findByIdAndOrganization_Id(
                                customerId,
                                organizationId
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customerService.findById(
                        organizationId,
                        customerId,
                        userId
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.CUSTOMER_NOT_FOUND
                    );
                });
    }
}