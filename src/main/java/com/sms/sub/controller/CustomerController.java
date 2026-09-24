package com.sms.sub.controller;

import com.sms.sub.client.UserServiceClient;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.ERole;
import com.sms.sub.dto.UserRegistrationRequest;
import com.sms.sub.exception.ConflictException;
import com.sms.sub.repository.CustomerRepository;
import com.sms.sub.service.SubscriptionService;
import com.sms.sub.dto.CustomerDtos.CreateCustomerRequest;
import com.sms.sub.dto.CustomerDtos.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Minimal customer records subscriptions attach to")
public class CustomerController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper mapper;
    private final UserServiceClient userServiceClient;
    private final CustomerRepository customerRepository;



    public CustomerController(SubscriptionService subscriptionService, SubscriptionMapper mapper, UserServiceClient userServiceClient, CustomerRepository customerRepository) {
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
        this.userServiceClient = userServiceClient;
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "Create a customer", description = "externalId should be the ID from your system of record (CRM, auth provider, etc.) and must be unique within the organization.")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Parameter(description = "Tenant identifier", required = true, example = "org_acme")
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody CreateCustomerRequest request) {
        /*
        To-Do check email exist from customer and user table, username from user table
        */
        /*if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }*/

        if (customerRepository.existsByOrganizationIdAndEmailAndMobile(organizationId, request.email(), request.mobile())) {
            throw new ConflictException("Customer already exists in this organization: " + request.email() +" or "+request.mobile());
        }

        Customer customer = subscriptionService.createCustomer(
                organizationId, request.email(), request.name(), request.mobile());

        UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .firstName(firstNameOf(request.name()))
                .lastName(lastNameOf(request.name()))
                .role(ERole.ROLE_CUSTOMER)
                .oraganizationId(organizationId)
                .customerId(customer.getId())
                .build();
        userServiceClient.register(userRequest);

        return mapper.toResponse(customer);
    }

    @Operation(summary = "List all customers in the organization")
    @GetMapping("/all")
    public List<CustomerResponse> listCustomers(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId) {
        return subscriptionService.listCustomers(organizationId).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Get a customer by ID")
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String customerId) {
        return mapper.toResponse(subscriptionService.getCustomer(organizationId, customerId));
    }

    private String firstNameOf(String fullName) {
        if (fullName == null || fullName.isBlank()) return fullName;
        int space = fullName.indexOf(' ');
        return space == -1 ? fullName : fullName.substring(0, space);
    }

    private String lastNameOf(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        int space = fullName.indexOf(' ');
        return space == -1 ? "" : fullName.substring(space + 1).trim();
    }
}
