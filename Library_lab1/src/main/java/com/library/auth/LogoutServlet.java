package com.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.UserDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(LogoutServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Received logout request");
        HttpSession session = req.getSession(false);
        String idToken = null;

        if(session != null){
            idToken = (String) session.getAttribute("id_token");
            UserDTO user = (UserDTO) session.getAttribute("user");
            String email = (user != null) ? user.getEmail() : "unknown";
            session.invalidate();
            logger.info("Local session invalidated for user: {}", email);
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");
        if (is == null) {
            logger.error("keycloak.json not found in classpath!");
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        KeycloakConfig config = objectMapper.readValue(is, KeycloakConfig.class);

        // construct the post-logout redirect URI (back to the home page)
        String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
        String postLogoutRedirectUri = baseUrl + "/";

        String logoutUrl = config.getLogoutUrl() + "?" +
                "client_id=" + URLEncoder.encode(config.clientId, StandardCharsets.UTF_8) +
                "&post_logout_redirect_uri=" + URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8);

        if (idToken != null) {
            logoutUrl += "&id_token_hint=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
        }

        logger.debug("Redirecting to Keycloak logout: {}", logoutUrl);
        resp.sendRedirect(logoutUrl);
    }
}
