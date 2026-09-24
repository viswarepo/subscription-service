package com.sms.sub.service;

import com.sms.sub.domain.Customer;
import com.sms.sub.domain.CustomerProfile;
import com.sms.sub.dto.CustomerProfileRequest;
import com.sms.sub.dto.CustomerProfileResponse;
import com.sms.sub.repository.CustomerRepository;
import com.sms.sub.repository.CustomerProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerProfileService {

    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository profileRepository;

    public CustomerProfileService(CustomerRepository customerRepository,
                                  CustomerProfileRepository profileRepository) {
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
    }

    public CustomerProfileResponse getProfile(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerProfile profile = profileRepository.findByCustomerId(customerId)
                .orElse(new CustomerProfile(customerId)); // empty profile if not set

        return CustomerProfileResponse.from(customer, profile);
    }

    public CustomerProfileResponse updateProfile(String customerId, CustomerProfileRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerProfile profile = profileRepository.findByCustomerId(customerId)
                .orElse(new CustomerProfile(customerId));

        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setPreferredCurrency(request.getPreferredCurrency());
        profile.setLocale(request.getLocale());
        profile.setTaxId(request.getTaxId());

        profileRepository.save(profile);

        return CustomerProfileResponse.from(customer, profile);
    }
}

