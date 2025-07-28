package com.example.smartdeskbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Genel uygulama konfigürasyonları
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

    /**
     * Async işlemler için thread pool
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("SmartDesk-");
        executor.initialize();
        return executor;
    }

    /**
     * File upload konfigürasyonları
     */
    @Bean
    @ConfigurationProperties(prefix = "app.file.upload")
    public FileUploadProperties fileUploadProperties() {
        return new FileUploadProperties();
    }

    /**
     * Business logic konfigürasyonları
     */
    @Bean
    @ConfigurationProperties(prefix = "app.business")
    public BusinessProperties businessProperties() {
        return new BusinessProperties();
    }

    /**
     * Security konfigürasyonları
     */
    @Bean
    @ConfigurationProperties(prefix = "app.security")
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }

    /**
     * Notification konfigürasyonları
     */
    @Bean
    @ConfigurationProperties(prefix = "app.notification")
    public NotificationProperties notificationProperties() {
        return new NotificationProperties();
    }

    // Properties Classes
    public static class FileUploadProperties {
        private String directory = "./uploads";
        private long maxSize = 10485760; // 10MB
        private String allowedTypes = "jpg,jpeg,png,pdf,doc,docx,txt,zip,csv,xlsx,xls";

        // Getters and Setters
        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }

        public long getMaxSize() { return maxSize; }
        public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

        public String getAllowedTypes() { return allowedTypes; }
        public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }

        public String[] getAllowedTypesArray() {
            return allowedTypes != null ? allowedTypes.split(",") : new String[0];
        }
    }

    public static class BusinessProperties {
        private TicketProperties ticket = new TicketProperties();
        private SlaProperties sla = new SlaProperties();

        public static class TicketProperties {
            private boolean autoAssign = true;
            private int defaultSlaHours = 24;
            private boolean escalationEnabled = true;

            // Getters and Setters
            public boolean isAutoAssign() { return autoAssign; }
            public void setAutoAssign(boolean autoAssign) { this.autoAssign = autoAssign; }

            public int getDefaultSlaHours() { return defaultSlaHours; }
            public void setDefaultSlaHours(int defaultSlaHours) { this.defaultSlaHours = defaultSlaHours; }

            public boolean isEscalationEnabled() { return escalationEnabled; }
            public void setEscalationEnabled(boolean escalationEnabled) { this.escalationEnabled = escalationEnabled; }
        }

        public static class SlaProperties {
            private boolean businessHoursOnly = true;
            private int businessStartHour = 9;
            private int businessEndHour = 18;

            // Getters and Setters
            public boolean isBusinessHoursOnly() { return businessHoursOnly; }
            public void setBusinessHoursOnly(boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; }

            public int getBusinessStartHour() { return businessStartHour; }
            public void setBusinessStartHour(int businessStartHour) { this.businessStartHour = businessStartHour; }

            public int getBusinessEndHour() { return businessEndHour; }
            public void setBusinessEndHour(int businessEndHour) { this.businessEndHour = businessEndHour; }
        }

        // Getters and Setters
        public TicketProperties getTicket() { return ticket; }
        public void setTicket(TicketProperties ticket) { this.ticket = ticket; }

        public SlaProperties getSla() { return sla; }
        public void setSla(SlaProperties sla) { this.sla = sla; }
    }

    public static class SecurityProperties {
        private JwtProperties jwt = new JwtProperties();
        private RateLimitProperties rateLimit = new RateLimitProperties();
        private AccountLockProperties accountLock = new AccountLockProperties();

        public static class JwtProperties {
            private BlacklistProperties blacklist = new BlacklistProperties();

            public static class BlacklistProperties {
                private boolean enabled = true;

                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
            }

            public BlacklistProperties getBlacklist() { return blacklist; }
            public void setBlacklist(BlacklistProperties blacklist) { this.blacklist = blacklist; }
        }

        public static class RateLimitProperties {
            private boolean enabled = true;
            private int requestsPerMinute = 60;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public int getRequestsPerMinute() { return requestsPerMinute; }
            public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
        }

        public static class AccountLockProperties {
            private int maxAttempts = 5;
            private int durationMinutes = 30;

            public int getMaxAttempts() { return maxAttempts; }
            public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

            public int getDurationMinutes() { return durationMinutes; }
            public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        }

        // Getters and Setters
        public JwtProperties getJwt() { return jwt; }
        public void setJwt(JwtProperties jwt) { this.jwt = jwt; }

        public RateLimitProperties getRateLimit() { return rateLimit; }
        public void setRateLimit(RateLimitProperties rateLimit) { this.rateLimit = rateLimit; }

        public AccountLockProperties getAccountLock() { return accountLock; }
        public void setAccountLock(AccountLockProperties accountLock) { this.accountLock = accountLock; }
    }

    public static class NotificationProperties {
        private EmailProperties email = new EmailProperties();
        private SmsProperties sms = new SmsProperties();

        public static class EmailProperties {
            private boolean enabled = true;
            private String from = "noreply@smartdesk.com";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            public String getFrom() { return from; }
            public void setFrom(String from) { this.from = from; }
        }

        public static class SmsProperties {
            private boolean enabled = false;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }

        // Getters and Setters
        public EmailProperties getEmail() { return email; }
        public void setEmail(EmailProperties email) { this.email = email; }

        public SmsProperties getSms() { return sms; }
        public void setSms(SmsProperties sms) { this.sms = sms; }
    }
}