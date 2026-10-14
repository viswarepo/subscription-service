package com.sms.sub.controller;

import com.sms.sub.client.UserServiceClient;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.ERole;
import com.sms.sub.dto.CustomerDtos;
import com.sms.sub.dto.SignupDtos.SignupRequest;
import com.sms.sub.dto.UserRegistrationRequest;
import com.sms.sub.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
  */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Signup", description = "Public self-signup: create a user, a customer, and a subscription in one call")
public class SignupController {

    private final SubscriptionService subscriptionService;
    private final UserServiceClient userServiceClient;

    public SignupController(SubscriptionService subscriptionService, UserServiceClient userServiceClient, SubscriptionMapper mapper) {
        this.subscriptionService = subscriptionService;
        this.userServiceClient = userServiceClient;
    }

    @PostMapping("/signup/{organizationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDtos.CustomerResponse signup(
            @Valid @RequestBody SignupRequest request,
            @PathVariable String organizationId) {

        Customer customer = subscriptionService.createCustomer(organizationId, request.email(), request.mobile(),request.name(),"CUSTOMER");

        UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .fullName(fullNameOf(request.name()))
                .role(ERole.ROLE_CUSTOMER.name())
                .oraganizationId(organizationId)
                .customerId(customer.getId())
                .build();
        userServiceClient.register(userRequest,ERole.ROLE_CUSTOMER.name());

        return new CustomerDtos.CustomerResponse(
                customer.getId(),
                customer.getOrganizationId(),
                customer.getExternalId(),
                customer.getEmail(),
                customer.getName(),
                customer.getMobile(),
                customer.getCustomerType(),
                customer.getCreatedAt()
                );
    }

    private String fullNameOf(String fullName) {
        if (fullName == null || fullName.isBlank()) return fullName;
        int space = fullName.indexOf(' ');
        return space == -1 ? fullName : fullName.substring(0, space);
    }
}
