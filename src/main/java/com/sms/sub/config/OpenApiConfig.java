package com.sms.sub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI subscriptionServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Subscription Service API")
                        .description("""
                                Owns customer subscriptions: trial periods, billing periods, renewals,
                                plan changes, cancellation, pausing, and dunning. Treats the Plan & Catalog
                                Service as the source of truth for what a plan costs, but snapshots price
                                onto the subscription at subscribe/change time so a subscriber's price
                                never moves without an explicit changePlan call.

                                Lifecycle: TRIALING -> ACTIVE <-> PAST_DUE, with ACTIVE <-> PAUSED, and
                                CANCELED / EXPIRED as terminal states.
                                """)
                        .version("v1")
                        .contact(new Contact().name("Subscriptions Team").email("subscriptions-team@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .tags(List.of(
                        new Tag().name("Customers").description("Minimal customer records subscriptions attach to"),
                        new Tag().name("Subscriptions").description("Subscription lifecycle: trials, renewals, plan changes, cancellation")
                ));
    }
}
