package com.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet("/auth/callback")
public class CallbackServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String sessionState = (String) req.getSession().getAttribute("oauth_state");
        File jsonFile = new File("resources/keycloak.json");
        KeycloakConfig config = objectMapper.readValue(jsonFile, KeycloakConfig.class);

        // verify state
        if (state == null || !state.equals(sessionState)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid state parameter");
            return;
        }
        req.getSession().removeAttribute("oauth_state"); // doesn't need after verifying

        String formData = "grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + URLEncoder.encode(config.redirectUri, StandardCharsets.UTF_8) +
                "&client_id=" + config.clientId +
                "&client_secret=" + config.credentials.secret;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getTokenUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        // execute exchange
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to exchange token");
                return;
            }

            // parse JSON response
            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), Map.class);
            String accessToken = (String) tokenResponse.get("access_token");

            // establish local session
            // will extract user info from access_token later
            req.getSession().setAttribute("access_token", accessToken);

            // redirect to main page
            resp.sendRedirect(req.getContextPath() + "/");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}