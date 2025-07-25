package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Kullanıcı bilgilerini tutan entity sınıfı
 * Tüm sistem kullanıcıları (Admin, Manager, Agent, Customer) bu tabloda tutulur
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_company", columnList = "company_id"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_status", columnList = "status")
})
public class User extends AuditableEntity {

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    // Company relationship - bir kullanıcı bir şirkete ait
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_user_company"))
    private Company company;

    // Department relationship - agent ve manager'lar bir departmana ait
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_user_department"))
    private Department department;

    // Tickets created by this user (for customers) - User'lar customer olarak ticket oluşturursa
    @OneToMany(mappedBy = "creatorUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> createdTickets = new ArrayList<>();

    // Tickets assigned to this user (for agents)
    @OneToMany(mappedBy = "assignedAgent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> assignedTickets = new ArrayList<>();

    // Ticket comments made by this user
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketComment> comments = new ArrayList<>();

    // User preferences (JSON format)
    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    // Constructors
    public User() {
        super();
    }

    public User(String email, String passwordHash, String firstName, String lastName, UserRole role) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Business Methods

    /**
     * Kullanıcının tam adını döndürür
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Kullanıcının aktif olup olmadığını kontrol eder
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Kullanıcının giriş yapıp yapamayacağını kontrol eder
     */
    public boolean canLogin() {
        return status.canLogin() && !isAccountLocked() && emailVerified;
    }

    /**
     * Hesabın kilitli olup olmadığını kontrol eder
     */
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Şifre reset token'ının geçerli olup olmadığını kontrol eder
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetExpires != null &&
                passwordResetExpires.isAfter(LocalDateTime.now());
    }

    /**
     * Kullanıcının belirtilen role sahip olup olmadığını kontrol eder
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Kullanıcının belirtilen role veya daha üst yetkiye sahip olup olmadığını kontrol eder
     */
    public boolean hasPermission(UserRole requiredRole) {
        return this.role.hasPermission(requiredRole);
    }

    /**
     * Başarılı giriş sonrası güncelleme
     */
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Başarısız giriş denemesi sonrası güncelleme
     */
    public void recordFailedLogin() {
        this.loginAttempts++;
        // 5 başarısız denemeden sonra hesabı 30 dakika kilitle
        if (this.loginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Hesap kilidi kaldırma
     */
    public void unlockAccount() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Email doğrulama
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Integer getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(Integer loginAttempts) { this.loginAttempts = loginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    public LocalDateTime getPasswordResetExpires() { return passwordResetExpires; }
    public void setPasswordResetExpires(LocalDateTime passwordResetExpires) { this.passwordResetExpires = passwordResetExpires; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public List<Ticket> getCreatedTickets() { return createdTickets; }
    public void setCreatedTickets(List<Ticket> createdTickets) { this.createdTickets = createdTickets; }

    public List<Ticket> getAssignedTickets() { return assignedTickets; }
    public void setAssignedTickets(List<Ticket> assignedTickets) { this.assignedTickets = assignedTickets; }

    public List<TicketComment> getComments() { return comments; }
    public void setComments(List<TicketComment> comments) { this.comments = comments; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', fullName='%s', role=%s, status=%s}",
                getId(), email, getFullName(), role, status);
    }
}