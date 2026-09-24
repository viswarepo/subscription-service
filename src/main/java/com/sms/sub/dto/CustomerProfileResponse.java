package com.sms.sub.dto;
import com.sms.sub.domain.Customer;
import com.sms.sub.domain.CustomerProfile;
import java.time.Instant;

public class CustomerProfileResponse {

    private String id;
    private String organizationId;
    private String externalId;
    private String email;
    private String name;
    private Instant createdAt;

    // Extended profile fields
    private String phoneNumber;
    private String address;
    private String preferredCurrency;
    private String locale;
    private String taxId;

    // Factory method to build response from Customer + Profile
    public static CustomerProfileResponse from(Customer customer, CustomerProfile profile) {
        CustomerProfileResponse response = new CustomerProfileResponse();
        response.id = customer.getId();
        response.organizationId = customer.getOrganizationId();
        response.externalId = customer.getExternalId();
        response.email = customer.getEmail();
        response.name = customer.getName();
        response.createdAt = customer.getCreatedAt();

        if (profile != null) {
            response.phoneNumber = profile.getPhoneNumber();
            response.address = profile.getAddress();
            response.preferredCurrency = profile.getPreferredCurrency();
            response.locale = profile.getLocale();
            response.taxId = profile.getTaxId();
        }

        return response;
    }

    // Getters
    public String getId() { return id; }
    public String getOrganizationId() { return organizationId; }
    public String getExternalId() { return externalId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getPreferredCurrency() { return preferredCurrency; }
    public String getLocale() { return locale; }
    public String getTaxId() { return taxId; }
}
