package com.sms.sub.controller;

import com.sms.sub.domain.Customer;
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

    public CustomerController(SubscriptionService subscriptionService, SubscriptionMapper mapper) {
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
    }

    @Operation(summary = "Create a customer", description = "externalId should be the ID from your system of record (CRM, auth provider, etc.) and must be unique within the organization.")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Parameter(description = "Tenant identifier", required = true, example = "org_acme")
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = subscriptionService.createCustomer(
                organizationId, request.email(), request.name());
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
            @PathVariable Long customerId) {
        return mapper.toResponse(subscriptionService.getCustomer(organizationId, customerId));
    }
}
