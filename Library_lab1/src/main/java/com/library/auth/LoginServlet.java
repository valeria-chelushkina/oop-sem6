package com.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(LoginServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Initiating login process");
        String randomState = StateGenerator.generateState();
        // save state to session for verification later
        req.getSession().setAttribute("oauth_state", randomState);

        InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");

        if (is == null) {
            logger.error("keycloak.json not found in classpath!");
            throw new RuntimeException("keycloak.json not found in classpath!");
        }
        KeycloakConfig config = objectMapper.readValue(is, KeycloakConfig.class);

        String authUrl = config.getAuthUrl() + "?" +
                "client_id=" + URLEncoder.encode(config.clientId, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(config.redirectUri, StandardCharsets.UTF_8) +
                "&state=" + randomState;

        logger.debug("Redirecting to Keycloak: {}", authUrl);
        resp.sendRedirect(authUrl);
    }
}