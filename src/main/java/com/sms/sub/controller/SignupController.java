package com.sms.sub.controller;

import com.sms.sub.dto.SignupDtos.SignupRequest;
import com.sms.sub.dto.SignupDtos.SignupResponse;
import com.sms.sub.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public self-signup: a visitor with no account picks a plan and, in one
 * call, gets a user account (role CUSTOMER, in user-service), a Customer
 * record, and a Subscription to the selected plan.
 *
 * This endpoint must be reachable with no token - mounted under
 * /api/v1/customers/signup specifically so it's covered by the gateway's
 * existing subscription-service route predicate, and must be added to
 * JwtAuthenticationGlobalFilter's EXEMPT_PREFIXES on the gateway (see that
 * class) or every signup attempt will be rejected with 401 before it
 * reaches this controller.
 *
 * Deliberately does NOT return a token - see AuthService/JwtUtil on
 * api-gateway for why token issuance stays in exactly one place. The
 * frontend calls POST /auth/login next, using the username/password just
 * submitted here, and gets back { accessToken, redirectUrl }.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Signup", description = "Public self-signup: create a user, a customer, and a subscription in one call")
public class SignupController {

    private final SignupService signupService;
    private final SubscriptionMapper mapper;

    public SignupController(SignupService signupService, SubscriptionMapper mapper) {
        this.signupService = signupService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Sign up and subscribe",
            description = "Creates a user (role CUSTOMER) in user-service, a Customer record, and a Subscription "
                    + "to the selected plan, in that order. Does not return a token - call POST /auth/login "
                    + "next with the same username/password to get one.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(
            @Parameter(description = "Tenant identifier", required = true, example = "org_acme")
            @RequestHeader("X-Organization-Id") @NotBlank String organizationId,
            @Valid @RequestBody SignupRequest request) {

        var result = signupService.signup(organizationId, request);

        return new SignupResponse(
                result.customer().getId(),
                request.username(),
                organizationId,
                request.mobile(),
                mapper.toResponse(result.subscription()));
    }
}
