package com.sms.sub.dto;

import com.sms.sub.domain.ERole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors user-service's UserRequestDTO field-for-field (including its
 * getOraganizationId() typo - see UserServiceClient) so RestTemplate/Jackson
 * serializes JSON field names user-service actually expects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private ERole role;
    private String oraganizationId; // sic - matches user-service's UserRequestDTO
    private String customerId;
}
