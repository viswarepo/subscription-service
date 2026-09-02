package com.sms.sub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class CustomerDtos {

    private CustomerDtos() {
    }

    public record CreateCustomerRequest(
            //@NotBlank String externalId,
            @NotBlank @Email String email,
            String name
    ) {
    }

    public record CustomerResponse(
            Long id,
            String organizationId,
            String externalId,
            String email,
            String name,
            Instant createdAt
    ) {
    }
}
