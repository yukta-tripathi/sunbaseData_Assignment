package com.assignment.controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.assignment.entity.entity;

import java.util.Arrays;
import java.util.List;

@Controller
public class mycontroller {

    @Value("${authentication.api.url}")
    private String authApiUrl;

    @Value("${customer.api.url}")
    private String customerApiUrl;

    private final RestTemplate restTemplate;

    public mycontroller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String showLoginPage() {
        return "authenticate";
    }

    @PostMapping("/authenticate")
    public String authenticateUser(@RequestParam("login_id") String loginId,
                                   @RequestParam("password") String password,
                                   Model model) {
        // Code to call the authentication API and get the bearer token
        String bearerToken = getBearerToken(loginId, password);

        if (bearerToken != null) {
            // Code to fetch the customer list using the bearer token
            List<entity> customers = getCustomerList(bearerToken);
            model.addAttribute("customers", customers);
            return "customer_list";
        } else {
            model.addAttribute("errorMessage", "Invalid credentials. Please try again.");
            return "authenticate";
        }
    }

    @GetMapping("/create-customer")
    public String showCreateCustomerForm() {
        return "create_customer";
    }

    @PostMapping("/create-customer")
    public String createCustomer(@RequestParam("first_name") String firstName,
                                 @RequestParam("last_name") String lastName,
                                 @RequestParam("street") String street,
                                 @RequestParam("address") String address,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 @RequestHeader("Authorization") String bearerToken,
                                 Model model) {

        // Code to create a new customer using the provided data and the bearer token
        entity customer = new entity(firstName, lastName, street, address, city, state, email, phone);
        boolean success = createNewCustomer(customer, bearerToken);

        if (success) {
            // Code to fetch the updated customer list and display it
            List<entity> customers = getCustomerList(bearerToken);
            model.addAttribute("customers", customers);
            return "customer_list";
        } else {
            model.addAttribute("errorMessage", "Failed to create the customer. Please try again.");
            return "create_customer";
        }
    }

    @PostMapping("/delete-customer/{uuid}")
    public String deleteCustomer(@PathVariable String uuid,
                                 @RequestHeader("Authorization") String bearerToken,
                                 Model model) {
        // Code to delete the customer with the given UUID using the bearer token
        boolean success = deleteCustomerByUUID(uuid, bearerToken);

        if (success) {
            // Code to fetch the updated customer list and display it
            List<entity> customers = getCustomerList(bearerToken);
            model.addAttribute("customers", customers);
            return "customer_list";
        } else {
            model.addAttribute("errorMessage", "Failed to delete the customer. Please try again.");
            return "customer_list";
        }
    }

    @GetMapping("/update-customer/{uuid}")
    public String showUpdateCustomerForm(@PathVariable String uuid, Model model) {
        // Code to fetch the customer details using the UUID and display them in the form
        entity customer = getCustomerByUUID(uuid);
        model.addAttribute("customer", customer);
        return "update_customer";
    }

    @PostMapping("/update-customer/{uuid}")
    public String updateCustomer(@PathVariable String uuid,
                                 @RequestParam("first_name") String firstName,
                                 @RequestParam("last_name") String lastName,
                                 @RequestParam("street") String street,
                                 @RequestParam("address") String address,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 @RequestHeader("Authorization") String bearerToken,
                                 Model model) {

        // Code to update the customer with the given UUID using the provided data and the bearer token
        entity customer = new entity(uuid, firstName, lastName, street, address, city, state, email, phone);
        boolean success = updateExistingCustomer(customer, bearerToken);

        if (success) {
            // Code to fetch the updated customer list and display it
            List<entity> customers = getCustomerList(bearerToken);
            model.addAttribute("customers", customers);
            return "customer_list";
        } else {
            model.addAttribute("errorMessage", "Failed to update the customer. Please try again.");
            return "update_customer";
        }
    }

    private String getBearerToken(String loginId, String password) {
        // Code to call the authentication API and get the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"login_id\":\"" + loginId + "\",\"password\":\"" + password + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(authApiUrl, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return null;
    }

    private List<entity> getCustomerList(String bearerToken) {
        // Code to call the customer API and get the customer list using the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<entity[]> response = restTemplate.exchange(customerApiUrl + "?cmd=get_customer_list", HttpMethod.GET, requestEntity, entity[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Arrays.asList(response.getBody());
        }
        return null;
    }

    private boolean createNewCustomer(entity customer, String bearerToken) {
        // Code to call the customer API and create a new customer using the provided data and the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<entity> requestEntity = new HttpEntity<>(customer, headers);

        ResponseEntity<Void> response = restTemplate.exchange(customerApiUrl + "?cmd=create", HttpMethod.POST, requestEntity, Void.class);
        return response.getStatusCode() == HttpStatus.CREATED;
    }

    private boolean deleteCustomerByUUID(String uuid, String bearerToken) {
        // Code to call the customer API and delete the customer with the given UUID using the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(customerApiUrl + "?cmd=delete&uuid=" + uuid, HttpMethod.POST, requestEntity, Void.class);
        return response.getStatusCode() == HttpStatus.OK;
    }

    private entity getCustomerByUUID(String uuid) {
        // Code to call the customer API and get the customer details using the UUID
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token_recieved_in_authentication_API_call");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String customerApiUrl = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";

        ResponseEntity<entity[]> response = restTemplate.exchange(customerApiUrl + "?cmd=get_customer_list", HttpMethod.GET, requestEntity, entity[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            List<entity> customers = Arrays.asList(response.getBody());
            for (entity customer : customers) {
                if (customer.getUuid().equals(uuid)) {
                    return customer;
                }
            }
        }
        return null;
    }
    

    private boolean updateExistingCustomer(entity customer, String bearerToken) {
        // Code to call the customer API and update the customer with the given UUID using the provided data and the bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);

        HttpEntity<entity> requestEntity = new HttpEntity<>(customer, headers);

        ResponseEntity<Void> response = restTemplate.exchange(customerApiUrl + "?cmd=update&uuid=" + customer.getUuid(), HttpMethod.POST, requestEntity, Void.class);
        return response.getStatusCode() == HttpStatus.OK;
    }
}
