package com.assignment.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.assignment.entity.entity;

public interface CustomerService {

	public String authenticateUser(String loginId, String password);
	public List<entity> getCustomerList(String bearerToken);
	public void createCustomer(String bearerToken, entity customer);
	public void deleteCustomer(String bearerToken, String uuid);
	 public entity getCustomerByUUID(String uuid);
	 public void updateCustomer(String bearerToken, String uuid, entity customer);
}
