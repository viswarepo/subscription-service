package com.sms.sub.repository;

import com.sms.sub.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByIdAndOrganizationId(String id, String organizationId);
    Optional<Customer> findByOrganizationIdAndExternalId(String organizationId, String externalId);
    boolean existsByOrganizationIdAndExternalId(String organizationId, String externalId);
    List<Customer> findByOrganizationId(String organizationId);

    boolean existsByOrganizationIdAndEmailAndMobile(String organizationId, String email, String mobile);
}
