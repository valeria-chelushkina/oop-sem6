package com.library.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakConfig {
    public String realm;

    @JsonProperty("auth-server-url")
    public String authServerUrl;

    @JsonProperty("ssl-required")
    public String sslRequired;

    @JsonProperty("resource")
    public String clientId;

    @JsonProperty("redirect-uri")
    public String redirectUri;

    public Credentials credentials;

    @JsonProperty("confidential-port")
    public int confidentialPort;

    public static class Credentials {
        public String secret;
    }

    private String getBaseUrl() {
        return authServerUrl.endsWith("/") ? authServerUrl : authServerUrl + "/";
    }

    public String getAuthUrl() {
        return String.format("%srealms/%s/protocol/openid-connect/auth",
                getBaseUrl(), realm);
    }

    public String getTokenUrl() {
        return String.format("%srealms/%s/protocol/openid-connect/token",
                getBaseUrl(), realm);
    }
}
