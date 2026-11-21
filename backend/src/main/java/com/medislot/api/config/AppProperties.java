package com.medislot.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Security security = new Security();
    private final Admin admin = new Admin();
    private final Mail mail = new Mail();
    public Security getSecurity() { return security; }
    public Admin getAdmin() { return admin; }
    public Mail getMail() { return mail; }
    public static class Security {
        private String jwtSecret;
        private long accessTokenMinutes = 30;
        private long passwordResetMinutes = 15;
        private int passwordResetMaxRequests = 3;
        private String frontendOrigin = "http://localhost:3000";
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public long getAccessTokenMinutes() { return accessTokenMinutes; }
        public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
        public long getPasswordResetMinutes() { return passwordResetMinutes; }
        public void setPasswordResetMinutes(long passwordResetMinutes) { this.passwordResetMinutes = passwordResetMinutes; }
        public int getPasswordResetMaxRequests() { return passwordResetMaxRequests; }
        public void setPasswordResetMaxRequests(int passwordResetMaxRequests) { this.passwordResetMaxRequests = passwordResetMaxRequests; }
        public String getFrontendOrigin() { return frontendOrigin; }
        public void setFrontendOrigin(String frontendOrigin) { this.frontendOrigin = frontendOrigin; }
    }
    public static class Mail {
        private String mode = "disabled";
        private String resetUrl = "http://localhost:3000/reset-password";
        private String developmentOutbox = ".dev-reset-emails";
        private String from;
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getResetUrl() { return resetUrl; }
        public void setResetUrl(String resetUrl) { this.resetUrl = resetUrl; }
        public String getDevelopmentOutbox() { return developmentOutbox; }
        public void setDevelopmentOutbox(String developmentOutbox) { this.developmentOutbox = developmentOutbox; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
    }
    public static class Admin {
        private String bootstrapEmail;
        private String bootstrapPassword;
        private String bootstrapName = "Platform Administrator";
        public String getBootstrapEmail() { return bootstrapEmail; }
        public void setBootstrapEmail(String bootstrapEmail) { this.bootstrapEmail = bootstrapEmail; }
        public String getBootstrapPassword() { return bootstrapPassword; }
        public void setBootstrapPassword(String bootstrapPassword) { this.bootstrapPassword = bootstrapPassword; }
        public String getBootstrapName() { return bootstrapName; }
        public void setBootstrapName(String bootstrapName) { this.bootstrapName = bootstrapName; }
    }
}
