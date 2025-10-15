package uy.gub.hcen.auth.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration for gub.uy OpenID Connect integration.
 * Loads OAuth 2.0/OIDC configuration from application.properties.
 *
 * This configuration supports multiple client types:
 * - Mobile app (public client with PKCE)
 * - Patient web portal (confidential client)
 * - Admin web portal (confidential client)
 */
@ApplicationScoped
public class OidcConfiguration {

    private static final Logger LOGGER = Logger.getLogger(OidcConfiguration.class.getName());

    private Properties properties;

    // gub.uy OIDC endpoints
    private String issuer;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String jwksUri;
    private String endSessionEndpoint;

    // OAuth 2.0 clients
    private String mobileClientId;
    private String webPatientClientId;
    private String webPatientClientSecret;
    private String webAdminClientId;
    private String webAdminClientSecret;

    // Common settings
    private String[] scopes;
    private String[] acrValues;

    @PostConstruct
    public void init() {
        try {
            properties = loadProperties();
            loadOidcSettings();
            LOGGER.info("OIDC configuration initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize OIDC configuration: " + e.getMessage());
            throw new RuntimeException("OIDC configuration initialization failed", e);
        }
    }

    private void loadOidcSettings() {
        // gub.uy endpoints
        issuer = getRequiredProperty("gubuy.oidc.issuer");
        authorizationEndpoint = getRequiredProperty("gubuy.oidc.authorization.endpoint");
        tokenEndpoint = getRequiredProperty("gubuy.oidc.token.endpoint");
        userInfoEndpoint = getRequiredProperty("gubuy.oidc.userinfo.endpoint");
        jwksUri = getRequiredProperty("gubuy.oidc.jwks.uri");
        endSessionEndpoint = getRequiredProperty("gubuy.oidc.endsession.endpoint");

        // Client configurations
        mobileClientId = getRequiredProperty("gubuy.oidc.client.mobile.id");
        webPatientClientId = getRequiredProperty("gubuy.oidc.client.web.patient.id");
        webPatientClientSecret = getRequiredProperty("gubuy.oidc.client.web.patient.secret");
        webAdminClientId = getRequiredProperty("gubuy.oidc.client.web.admin.id");
        webAdminClientSecret = getRequiredProperty("gubuy.oidc.client.web.admin.secret");

        // Scopes and ACR values
        String scopesStr = getRequiredProperty("gubuy.oidc.scopes");
        scopes = scopesStr.split("\\s+");

        String acrValuesStr = getRequiredProperty("gubuy.oidc.acr.values");
        acrValues = acrValuesStr.split("\\s+");
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property '" + key + "' is not set");
        }
        return value.trim();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("application.properties not found in classpath");
            }
            props.load(input);
        }
        return props;
    }

    // Getters

    public String getIssuer() {
        return issuer;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public String getEndSessionEndpoint() {
        return endSessionEndpoint;
    }

    public String getMobileClientId() {
        return mobileClientId;
    }

    public String getWebPatientClientId() {
        return webPatientClientId;
    }

    public String getWebPatientClientSecret() {
        return webPatientClientSecret;
    }

    public String getWebAdminClientId() {
        return webAdminClientId;
    }

    public String getWebAdminClientSecret() {
        return webAdminClientSecret;
    }

    public String[] getScopes() {
        return scopes;
    }

    public String[] getAcrValues() {
        return acrValues;
    }

    /**
     * Get client ID for the specified client type
     */
    public String getClientId(ClientType clientType) {
        return switch (clientType) {
            case MOBILE -> mobileClientId;
            case WEB_PATIENT -> webPatientClientId;
            case WEB_ADMIN -> webAdminClientId;
        };
    }

    /**
     * Get client secret for the specified client type (null for public clients)
     */
    public String getClientSecret(ClientType clientType) {
        return switch (clientType) {
            case MOBILE -> null; // Public client, no secret
            case WEB_PATIENT -> webPatientClientSecret;
            case WEB_ADMIN -> webAdminClientSecret;
        };
    }

    /**
     * Check if the client is confidential (has client secret)
     */
    public boolean isConfidentialClient(ClientType clientType) {
        return clientType != ClientType.MOBILE;
    }

    /**
     * Client types supported by HCEN
     */
    public enum ClientType {
        MOBILE,          // React Native mobile app (public client)
        WEB_PATIENT,     // Patient web portal (confidential client)
        WEB_ADMIN        // Admin web portal (confidential client)
    }
}
