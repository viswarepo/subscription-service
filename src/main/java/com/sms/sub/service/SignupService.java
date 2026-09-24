package com.sms.sub.service;

import com.sms.sub.client.UserServiceClient;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.ERole;
import com.sms.sub.domain.Subscription;
import com.sms.sub.dto.SignupDtos.SignupRequest;
import com.sms.sub.dto.UserRegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates public self-signup: register a login-capable user in
 * user-service, then create the Customer record, then create the
 * Subscription for the plan they selected - all before the caller has any
 * token, since this is how a brand-new visitor becomes a customer.
 *
 * Ordering/failure note: user-service and this service have separate
 * databases, so there's no real distributed transaction across the two.
 * If user registration succeeds but subscription creation then fails (e.g.
 * a duplicate live subscription, or a validation error on the plan), the
 * customer+user already exist with no subscription. This is deliberately
 * left as-is rather than attempting a compensating delete: the customer can
 * log in (their account is real) and retry subscribing, which is a better
 * outcome than an ambiguous partial rollback. Revisit if that tradeoff
 * doesn't fit your product.
 */
@Service
@Slf4j
public class SignupService {

    private final UserServiceClient userServiceClient;
    private final SubscriptionService subscriptionService;

    public SignupService(UserServiceClient userServiceClient, SubscriptionService subscriptionService) {
        this.userServiceClient = userServiceClient;
        this.subscriptionService = subscriptionService;
    }

    public SignupResult signup(String organizationId, SignupRequest request) {

        // 1. Create the Customer record.
        Customer customer = subscriptionService.createCustomer(organizationId, request.email(), request.mobile(),request.name());

        // 1. Register the login-capable user first - if this fails (duplicate
        //    username/email, user-service unreachable), nothing else happens.

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


        // 3. Create the Subscription for the plan they selected. See class
        //    javadoc for what happens if this step fails.
        var plan = request.planSelection();
        Subscription subscription;
        try {
            subscription = subscriptionService.createSubscription(
                    organizationId, customer.getId(), plan.productCode(), plan.planCode(), plan.planVersion(),
                    plan.currency(), plan.billingCycle(), plan.unitAmount(), plan.trialDays());
        } catch (RuntimeException e) {
            log.error("Signup: user '{}' and customer {} were created, but subscription creation failed: {}",
                    request.username(), customer.getId(), e.getMessage());
            throw e;
        }

        return new SignupResult(customer, subscription);
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

    public record SignupResult(Customer customer, Subscription subscription) {
    }
}
