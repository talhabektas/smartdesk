
// UpdateTicketRequest.java
package com.example.smartdeskbackend.dto.request.ticket;

import com.example.smartdeskbackend.enums.TicketCategory;
import com.example.smartdeskbackend.enums.TicketPriority;
import com.example.smartdeskbackend.enums.TicketStatus;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    private String description;

    private TicketPriority priority;

    private TicketCategory category;

    private TicketStatus status;

    private String tags;

    private Integer estimatedHours;

    // Constructors
    public UpdateTicketRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
}