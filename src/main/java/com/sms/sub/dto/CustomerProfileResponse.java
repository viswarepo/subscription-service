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
    private String firstName;
    private String lastName;
    private String alterMobile;
    private String landlineNumber;
    private String address;
    private String city;
    private String state;
    private String country;
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
            response.firstName = profile.getFirstName();
            response.lastName = profile.getLastName();
            response.alterMobile = profile.getAlterMobile();
            response.landlineNumber = profile.getLandlineNumber();
            response.address = profile.getAddress();
            response.country = profile.getCountry();
            response.state = profile.getState();
            response.city = profile.getCity();
            response.preferredCurrency = profile.getPreferredCurrency();
            response.locale = profile.getLocale();
            response.taxId = profile.getTaxId();
        }

        return response;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAlterMobile() {
        return alterMobile;
    }

    public String getLandlineNumber() {
        return landlineNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public String getLocale() {
        return locale;
    }

    public String getTaxId() {
        return taxId;
    }
}
