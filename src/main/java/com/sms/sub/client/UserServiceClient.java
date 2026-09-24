package com.sms.sub.client;

import com.sms.sub.dto.UserRegistrationRequest;
import com.sms.sub.exception.UserAlreadyExistsException;
import com.sms.sub.exception.UserServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Registers a login-capable user in user-service on behalf of a newly
 * created customer. subscription-service is a plain Spring MVC (servlet)
 * app, so a blocking RestTemplate call here runs on a normal request thread
 * - not the event-loop-starvation concern this same kind of call would be
 * inside the (WebFlux) api-gateway.
 *
 * NOTE: UserRegistrationRequest.oraganizationId is spelled exactly as
 * user-service's UserRequestDTO expects (missing an "n") - see that DTO's
 * getOraganizationId(). Preserved intentionally so JSON field names match
 * the real contract; worth renaming on both sides together later if you
 * want to fix the typo project-wide.
 */
@Component
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceBaseUrl;

    @Value("${internal.api-key}")
    private String internalApiKey;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * @throws UserAlreadyExistsException if username or email is already taken (user-service returns 409)
     * @throws UserServiceUnavailableException if user-service can't be reached or errors
     */
    public void register(UserRegistrationRequest request) {
        String url = userServiceBaseUrl + "/api/v1/users/register";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Object.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new UserAlreadyExistsException(
                    "Username or email already registered: " + request.getUsername());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("user-service unreachable while registering customer as a user", e);
            throw new UserServiceUnavailableException("user-service is currently unavailable", e);
        }
    }
}
