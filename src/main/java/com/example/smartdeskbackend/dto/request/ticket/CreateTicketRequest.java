// CreateTicketRequest.java
package com.example.smartdeskbackend.dto.request.ticket;

import com.example.smartdeskbackend.enums.TicketCategory;
import com.example.smartdeskbackend.enums.TicketPriority;
import com.example.smartdeskbackend.enums.TicketSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Long customerId;

    private Long creatorUserId;

    private Long departmentId;

    private TicketPriority priority = TicketPriority.NORMAL;

    private TicketCategory category;

    private TicketSource source = TicketSource.WEB_FORM;

    private Boolean isInternal = false;

    private String tags;

    // Constructors
    public CreateTicketRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public TicketSource getSource() { return source; }
    public void setSource(TicketSource source) { this.source = source; }

    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}