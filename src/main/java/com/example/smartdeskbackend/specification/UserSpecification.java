package com.example.smartdeskbackend.specification;

import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Specifications for User entity queries
 */
public class UserSpecification {

    /**
     * Search by term in firstName, lastName, or email
     */
    public static Specification<User> searchByTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern));
        };
    }

    /**
     * Filter by company ID
     */
    public static Specification<User> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Object, Object> companyJoin = root.join("company", JoinType.LEFT);
            return criteriaBuilder.equal(companyJoin.get("id"), companyId);
        };
    }

    /**
     * Filter by department ID
     */
    public static Specification<User> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
            return criteriaBuilder.equal(departmentJoin.get("id"), departmentId);
        };
    }

    /**
     * Filter by user role
     */
    public static Specification<User> hasRole(UserRole role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("role"), role);
        };
    }

    /**
     * Filter by user roles (IN clause)
     */
    public static Specification<User> hasRoleIn(List<UserRole> roles) {
        return (root, query, criteriaBuilder) -> {
            if (roles == null || roles.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return root.get("role").in(roles);
        };
    }

    /**
     * Filter by user status
     */
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by user statuses (IN clause)
     */
    public static Specification<User> hasStatusIn(List<UserStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return root.get("status").in(statuses);
        };
    }

    /**
     * Filter by email verification status
     */
    public static Specification<User> isEmailVerified(Boolean emailVerified) {
        return (root, query, criteriaBuilder) -> {
            if (emailVerified == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("emailVerified"), emailVerified);
        };
    }

    /**
     * Filter by account locked status
     */
    public static Specification<User> isAccountLocked(Boolean accountLocked) {
        return (root, query, criteriaBuilder) -> {
            if (accountLocked == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("accountLocked"), accountLocked);
        };
    }

    /**
     * Filter non-locked accounts
     */
    public static Specification<User> isNotLocked() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("accountLocked"), false),
                criteriaBuilder.isNull(root.get("accountLocked")));
    }

    /**
     * Filter by creation date after
     */
    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Filter by creation date before
     */
    public static Specification<User> createdBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * Filter by last login date after
     */
    public static Specification<User> lastLoginAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("lastLoginAt"), date);
        };
    }

    /**
     * Filter by last login date before
     */
    public static Specification<User> lastLoginBefore(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get("lastLoginAt"), date);
        };
    }

    /**
     * Exclude deleted users (if soft delete is implemented)
     */
    public static Specification<User> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("status"), UserStatus.INACTIVE);
    }

    /**
     * Filter by login attempts greater than
     */
    public static Specification<User> loginAttemptsGreaterThan(Integer attempts) {
        return (root, query, criteriaBuilder) -> {
            if (attempts == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThan(root.get("loginAttempts"), attempts);
        };
    }

    /**
     * Filter by phone number
     */
    public static Specification<User> hasPhone(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(root.get("phone"), "%" + phone + "%");
        };
    }

    /**
     * Combined specification builder for complex queries
     */
    public static SpecificationBuilder builder() {
        return new SpecificationBuilder();
    }

    /**
     * Builder pattern for complex specifications
     */
    public static class SpecificationBuilder {
        private Specification<User> specification = Specification.where(null);

        public SpecificationBuilder withSearchTerm(String searchTerm) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                specification = specification.and(searchByTerm(searchTerm));
            }
            return this;
        }

        public SpecificationBuilder withCompanyId(Long companyId) {
            if (companyId != null) {
                specification = specification.and(hasCompanyId(companyId));
            }
            return this;
        }

        public SpecificationBuilder withDepartmentId(Long departmentId) {
            if (departmentId != null) {
                specification = specification.and(hasDepartmentId(departmentId));
            }
            return this;
        }

        public SpecificationBuilder withRole(UserRole role) {
            if (role != null) {
                specification = specification.and(hasRole(role));
            }
            return this;
        }

        public SpecificationBuilder withRoles(List<UserRole> roles) {
            if (roles != null && !roles.isEmpty()) {
                specification = specification.and(hasRoleIn(roles));
            }
            return this;
        }

        public SpecificationBuilder withStatus(UserStatus status) {
            if (status != null) {
                specification = specification.and(hasStatus(status));
            }
            return this;
        }

        public SpecificationBuilder withStatuses(List<UserStatus> statuses) {
            if (statuses != null && !statuses.isEmpty()) {
                specification = specification.and(hasStatusIn(statuses));
            }
            return this;
        }

        public SpecificationBuilder withEmailVerified(Boolean emailVerified) {
            if (emailVerified != null) {
                specification = specification.and(isEmailVerified(emailVerified));
            }
            return this;
        }

        public SpecificationBuilder withDateRange(LocalDateTime from, LocalDateTime to) {
            if (from != null) {
                specification = specification.and(createdAfter(from));
            }
            if (to != null) {
                specification = specification.and(createdBefore(to));
            }
            return this;
        }

        public SpecificationBuilder activeOnly() {
            specification = specification.and(hasStatus(UserStatus.ACTIVE));
            return this;
        }

        public SpecificationBuilder excludeDeleted() {
            specification = specification.and(isNotDeleted());
            return this;
        }

        public SpecificationBuilder notLocked() {
            specification = specification.and(isNotLocked());
            return this;
        }

        public Specification<User> build() {
            return specification;
        }
    }
}