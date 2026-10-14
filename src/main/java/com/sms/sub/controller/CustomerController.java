package com.sms.sub.controller;

import com.sms.sub.client.UserServiceClient;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.ECustomerType;
import com.sms.sub.domain.ERole;
import com.sms.sub.dto.UserRegistrationRequest;
import com.sms.sub.producer.CustomerEventProducer;
import com.sms.sub.event.CustomerRegisteredEvent;
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
//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Minimal customer records subscriptions attach to")
public class CustomerController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper mapper;
    private final UserServiceClient userServiceClient;
    private final CustomerRepository customerRepository;
    private final CustomerEventProducer customerEventProducer;

    public CustomerController(SubscriptionService subscriptionService, SubscriptionMapper mapper, UserServiceClient userServiceClient, CustomerRepository customerRepository, CustomerEventProducer customerEventProducer) {
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
        this.userServiceClient = userServiceClient;
        this.customerRepository = customerRepository;
        this.customerEventProducer = customerEventProducer;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @Operation(summary = "Create a customer", description = "externalId should be the ID from your system of record (CRM, auth provider, etc.) and must be unique within the organization.")
    @PostMapping("/create/{userRole}")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String userRole,
            @Valid @RequestBody CreateCustomerRequest request) {

        if (customerRepository.existsByOrganizationIdAndEmailAndMobile(organizationId, request.email(), request.mobile())) {
            throw new ConflictException("Customer already exists in this organization: " + request.email() +" or "+request.mobile());
        }

        String customerType =ECustomerType.CUSTOMER.name();
        String role = ERole.ROLE_CUSTOMER.name();
        if(userRole.equals(ERole.ROLE_OWNER.name())){
            customerType = ECustomerType.TENANT.name();
            role = ERole.ROLE_ADMIN_SUPER.name();
        }
        Customer customer = subscriptionService.createCustomer(
                organizationId, request.email(), request.name(), request.mobile(),customerType);

        UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .fullName(fullNameOf(request.name()))
                .role(role)
                .oraganizationId(organizationId)
                .customerId(customer.getId())
                .build();
        userServiceClient.register(userRequest,role);

        CustomerRegisteredEvent event = new CustomerRegisteredEvent(
                customer.getId(),customer.getOrganizationId(),customer.getExternalId(),customer.getEmail(), customer.getName(),customer.getMobile(),customer.getCustomerType()
        );
        customerEventProducer.publishCustomerRegistered(event);

        return mapper.toResponse(customer);
    }

    @Operation(summary = "List all customers in the organization")
    @GetMapping("/all/{userRole}")
    public List<CustomerResponse> listCustomers(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String userRole) {
        String customerType=ECustomerType.CUSTOMER.name();
        if(userRole.equals(ERole.ROLE_OWNER.name())){
            customerType = ECustomerType.TENANT.name();
        }
        return subscriptionService.listCustomers(organizationId,customerType).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Get a customer by ID")
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @PathVariable String customerId) {
        return mapper.toResponse(subscriptionService.getCustomer(organizationId, customerId));
    }

    private String fullNameOf(String fullName) {
        if (fullName == null || fullName.isBlank()) return fullName;
        int space = fullName.indexOf(' ');
        return space == -1 ? fullName : fullName.substring(0, space);
    }

}
