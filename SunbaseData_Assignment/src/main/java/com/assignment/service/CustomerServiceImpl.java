package com.assignment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.assignment.entity.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CustomerServiceImpl {

    @Value("${authentication.api.url}")
    private String authApiUrl;

    @Value("${customer.api.url}")
    private String customerApiUrl;

    private final RestTemplate restTemplate;

    public CustomerServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String authenticateUser(String loginId, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"login_id\":\"" + loginId + "\",\"password\":\"" + password + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(authApiUrl, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    public List<entity> getCustomerList(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<entity[]> response = restTemplate.exchange(customerApiUrl + "?cmd=get_customer_list", HttpMethod.GET, requestEntity, entity[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Arrays.asList(response.getBody());
        }
        return new ArrayList<>();
    }

    public void createCustomer(String bearerToken, entity customer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<entity> requestEntity = new HttpEntity<>(customer, headers);

        restTemplate.exchange(customerApiUrl + "?cmd=create", HttpMethod.POST, requestEntity, String.class);
    }

    public void deleteCustomer(String bearerToken, String uuid) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(customerApiUrl + "?cmd=delete&uuid=" + uuid, HttpMethod.POST, requestEntity, String.class);
    }

    public entity getCustomerByUUID(String uuid, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = customerApiUrl + "?cmd=get_customer_list"; // Adjust the API endpoint based on your API design

        ResponseEntity<entity[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, entity[].class);
        if (response.getStatusCode().is2xxSuccessful()) {
            entity[] customers = response.getBody();
            if (customers != null) {
                for (entity customer : customers) {
                    if (customer.getUuid().equals(uuid)) {
                        return customer;
                    }
                }
            }
        }
        return null;
    }

    public void updateCustomer(String bearerToken, String uuid, entity customer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<entity> requestEntity = new HttpEntity<>(customer, headers);

        restTemplate.exchange(customerApiUrl + "?cmd=update&uuid=" + uuid, HttpMethod.POST, requestEntity, String.class);
    }
}
