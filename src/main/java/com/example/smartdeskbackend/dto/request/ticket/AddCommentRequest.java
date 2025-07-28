
// AddCommentRequest.java
package com.example.smartdeskbackend.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddCommentRequest {

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    private Boolean isInternal = false;

    private String commentType = "COMMENT";

    // Constructors
    public AddCommentRequest() {}

    public AddCommentRequest(String message, Long authorId) {
        this.message = message;
        this.authorId = authorId;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }

    public String getCommentType() { return commentType; }
    public void setCommentType(String commentType) { this.commentType = commentType; }
}
