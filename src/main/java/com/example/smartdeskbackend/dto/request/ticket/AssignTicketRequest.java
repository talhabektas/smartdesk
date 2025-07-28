
// AssignTicketRequest.java
package com.example.smartdeskbackend.dto.request.ticket;

import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    private String assignmentNote;

    // Constructors
    public AssignTicketRequest() {}

    public AssignTicketRequest(Long agentId) {
        this.agentId = agentId;
    }

    // Getters and Setters
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getAssignmentNote() { return assignmentNote; }
    public void setAssignmentNote(String assignmentNote) { this.assignmentNote = assignmentNote; }
}