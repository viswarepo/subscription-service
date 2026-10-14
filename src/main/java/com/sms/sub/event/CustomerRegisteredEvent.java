package com.sms.sub.event;

public record CustomerRegisteredEvent (
        String customerId,
        String organizationId,
        String externalId,
        String email,
        String name,
        String mobile,
        String customerType
) {
    public static CustomerRegisteredEvent of(
            String customerId,
            String organizationId,
            String externalId,
            String email,
            String name,
            String mobile,
            String customerType) {
        return new CustomerRegisteredEvent(
                customerId,
                organizationId,
                externalId,
                email,
                name,
                mobile,
                customerType);
    }
}
