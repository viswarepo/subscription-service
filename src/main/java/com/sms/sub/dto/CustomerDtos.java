package com.sms.sub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
public final class CustomerDtos {

    private CustomerDtos() {
    }

    public record CreateCustomerRequest(
            //@NotBlank String externalId,
            @NotBlank @Email String email,
            String name,

            // --- added: needed to also register this customer as a login-capable
            // user in user-service. See CustomerController.createCustomer(). ---
            @NotBlank String username,
            @NotBlank String mobile,
            String customerType,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
    ) {
    }

    public record CustomerResponse(
            String id,
            String organizationId,
            String externalId,
            String email,
            String name,
            String mobile,
            String customerType,
            Instant createdAt
    ) {
    }

}
