package com.sms.sub.controller;

import com.sms.sub.dto.CustomerDtos;
import com.sms.sub.dto.CustomerProfileRequest;
import com.sms.sub.dto.CustomerProfileResponse;
import com.sms.sub.service.CustomerProfileService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerProfileController {

    private final CustomerProfileService profileService;

    public CustomerProfileController(CustomerProfileService profileService) {
        this.profileService = profileService;
    }

    // 🔎 View customer profile
    @GetMapping("/{customerId}/profile")
    public CustomerProfileResponse getProfile(@PathVariable String customerId) {
        return profileService.getProfile(customerId);
    }

    // ✏️ Update customer profile
    @PutMapping("/{customerId}/profile/update")
    public CustomerProfileResponse updateProfile(@PathVariable String customerId,
                                                       @Valid @RequestBody CustomerProfileRequest request) {
        return profileService.updateProfile(customerId, request);
    }
}
