package com.sms.sub.dto;

import com.sms.sub.dto.SubscriptionDtos.CreateSubscriptionRequest;
import com.sms.sub.dto.SubscriptionDtos.SubscriptionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class SignupDtos {

    private SignupDtos() {
    }

    /**
     * planSelection reuses CreateSubscriptionRequest's exact shape (currency,
     * billingCycle, etc.) instead of redeclaring those types here. Its
     * customerId field is ignored - there is no customer yet at signup time;
     * the one created by this call is used instead.
     */
    public record SignupRequest(
            @NotBlank @Email String email,
            String name,
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String mobile,
            @NotNull @Valid CreateSubscriptionRequest planSelection
    ) {
    }

    public record SignupResponse(
            String customerId,
            String username,
            String organizationId,
            String mobile,
            SubscriptionResponse subscription
    ) {
    }
}
