package com.sms.sub.repository;

import com.sms.sub.domain.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, String> {
    Optional<CustomerProfile> findByCustomerId(String customerId);
}

