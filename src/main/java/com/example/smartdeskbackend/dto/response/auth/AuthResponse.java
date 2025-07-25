package com.example.smartdeskbackend.dto.response.auth;




import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private UserInfo user;

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuthResponse response = new AuthResponse();

        public Builder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            response.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public Builder user(UserInfo user) {
            response.user = user;
            return this;
        }

        public AuthResponse build() {
            return response;
        }
    }

    // UserInfo nested class
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private String status;
        private Long companyId;
        private Long departmentId;
        private String avatarUrl;

        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private UserInfo userInfo = new UserInfo();

            public UserInfoBuilder id(Long id) {
                userInfo.id = id;
                return this;
            }

            public UserInfoBuilder email(String email) {
                userInfo.email = email;
                return this;
            }

            public UserInfoBuilder firstName(String firstName) {
                userInfo.firstName = firstName;
                return this;
            }

            public UserInfoBuilder lastName(String lastName) {
                userInfo.lastName = lastName;
                return this;
            }

            public UserInfoBuilder fullName(String fullName) {
                userInfo.fullName = fullName;
                return this;
            }

            public UserInfoBuilder role(String role) {
                userInfo.role = role;
                return this;
            }

            public UserInfoBuilder status(String status) {
                userInfo.status = status;
                return this;
            }

            public UserInfoBuilder companyId(Long companyId) {
                userInfo.companyId = companyId;
                return this;
            }

            public UserInfoBuilder departmentId(Long departmentId) {
                userInfo.departmentId = departmentId;
                return this;
            }

            public UserInfoBuilder avatarUrl(String avatarUrl) {
                userInfo.avatarUrl = avatarUrl;
                return this;
            }

            public UserInfo build() {
                return userInfo;
            }
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
}