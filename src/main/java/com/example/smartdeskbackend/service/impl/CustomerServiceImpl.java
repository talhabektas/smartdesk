package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.customer.CreateCustomerRequest;
import com.example.smartdeskbackend.dto.request.customer.UpdateCustomerRequest;
import com.example.smartdeskbackend.dto.response.customer.CustomerResponse;
import com.example.smartdeskbackend.dto.response.customer.CustomerDetailResponse;
import com.example.smartdeskbackend.entity.Customer;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.enums.CustomerSegment;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.exception.UserAlreadyExistsException;
import com.example.smartdeskbackend.repository.CustomerRepository;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer service implementation
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerById(Long id) {
        logger.debug("Getting customer by id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        return mapToDetailResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerByEmail(String email) {
        logger.debug("Getting customer by email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        return mapToDetailResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomersByCompany(Long companyId, Pageable pageable) {
        logger.debug("Getting customers by company: {}", companyId);

        // Company kontrolü
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        List<Customer> customers = customerRepository.findByCompanyIdAndIsActiveTrue(companyId);
        // Convert to Page manually since we need active customers only
        return Page.empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(Long companyId, String searchTerm, Pageable pageable) {
        logger.debug("Searching customers in company: {} with term: {}", companyId, searchTerm);

        Page<Customer> customers = customerRepository.searchCustomers(companyId, searchTerm, pageable);
        return customers.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getVipCustomers(Long companyId) {
        logger.debug("Getting VIP customers for company: {}", companyId);

        List<Customer> vipCustomers = customerRepository.findVipCustomers(companyId);
        return vipCustomers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDetailResponse createCustomer(CreateCustomerRequest request) {
        logger.info("Creating new customer: {}", request.getEmail());

        // Email uniqueness kontrolü
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Customer already exists with email: " + request.getEmail());
        }

        // Company kontrolü
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        // Yeni customer oluştur
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setSegment(request.getSegment() != null ? request.getSegment() : CustomerSegment.STANDARD);
        customer.setCompanyName(request.getCompanyName());
        customer.setAddress(request.getAddress());
        customer.setNotes(request.getNotes());
        customer.setCompany(company);
        customer.setIsActive(true);

        customer = customerRepository.save(customer);

        logger.info("Customer created successfully with id: {}", customer.getId());
        return mapToDetailResponse(customer);
    }

    @Override
    public CustomerDetailResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        logger.info("Updating customer: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Email değişikliği kontrolü
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }

        // Diğer alanları güncelle
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getSegment() != null) {
            customer.setSegment(request.getSegment());
        }
        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }
        if (request.getIsActive() != null) {
            customer.setIsActive(request.getIsActive());
        }

        // Son iletişim tarihini güncelle
        customer.updateLastContact();

        customer = customerRepository.save(customer);

        logger.info("Customer updated successfully: {}", id);
        return mapToDetailResponse(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        logger.info("Deleting customer: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Soft delete
        customer.setIsActive(false);
        customerRepository.save(customer);

        logger.info("Customer deleted successfully: {}", id);
    }

    // ============ HELPER METHODS ============

    /**
     * Customer entity'sini CustomerResponse'a map eder
     */
    private CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setFullName(customer.getFullName());
        response.setSegment(customer.getSegment().getCode());
        response.setSegmentDisplayName(customer.getSegment().getDisplayName());
        response.setCompanyName(customer.getCompanyName());
        response.setIsActive(customer.getIsActive());
        response.setLastContact(customer.getLastContact());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }

    /**
     * Customer entity'sini CustomerDetailResponse'a map eder
     */
    private CustomerDetailResponse mapToDetailResponse(Customer customer) {
        CustomerDetailResponse response = new CustomerDetailResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setFullName(customer.getFullName());
        response.setSegment(customer.getSegment().getCode());
        response.setSegmentDisplayName(customer.getSegment().getDisplayName());
        response.setCompanyName(customer.getCompanyName());
        response.setAddress(customer.getAddress());
        response.setNotes(customer.getNotes());
        response.setIsActive(customer.getIsActive());
        response.setLastContact(customer.getLastContact());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        // Company bilgileri
        if (customer.getCompany() != null) {
            response.setCompanyId(customer.getCompany().getId());
            response.setCompanyDisplayName(customer.getCompany().getName());
        }

        // Ticket istatistikleri
        response.setTotalTickets(customer.getTickets().size());
        response.setActiveTickets((int) customer.getTickets().stream()
                .filter(ticket -> ticket.isActive())
                .count());

        return response;
    }
}