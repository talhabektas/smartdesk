package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.customer.CreateCustomerRequest;
import com.example.smartdeskbackend.dto.request.customer.UpdateCustomerRequest;
import com.example.smartdeskbackend.dto.response.customer.CustomerResponse;
import com.example.smartdeskbackend.dto.response.customer.CustomerDetailResponse;
import com.example.smartdeskbackend.enums.CustomerSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    CustomerDetailResponse getCustomerById(Long id);
    CustomerDetailResponse getCustomerByEmail(String email);
    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    Page<CustomerResponse> getCustomersByCompany(Long companyId, Pageable pageable);
    Page<CustomerResponse> searchCustomers(Long companyId, String searchTerm, Pageable pageable);
    Page<CustomerResponse> searchAllCustomers(String searchTerm, Pageable pageable);
    List<CustomerResponse> getVipCustomers(Long companyId);
    CustomerDetailResponse createCustomer(CreateCustomerRequest request);
    CustomerDetailResponse updateCustomer(Long id, UpdateCustomerRequest request);
    void deleteCustomer(Long id);
}
