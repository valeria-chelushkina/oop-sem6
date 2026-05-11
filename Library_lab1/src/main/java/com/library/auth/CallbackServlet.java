package com.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.UserDTO;
import com.library.entity.enums.UserRole;
import com.library.service.UserService;
import com.library.service.UserServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@WebServlet("/auth/callback")
public class CallbackServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(CallbackServlet.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Received authentication callback");
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String sessionState = (String) req.getSession().getAttribute("oauth_state");
        InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");

        if (is == null) {
            logger.error("keycloak.json not found in classpath!");
            throw new RuntimeException("keycloak.json not found in classpath!");
        }
        KeycloakConfig config = objectMapper.readValue(is, KeycloakConfig.class);

        // verify state
        if (state == null || !state.equals(sessionState)) {
            logger.warn("Invalid state parameter. Expected: {}, Received: {}", sessionState, state);
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
            logger.debug("Exchanging authorization code for tokens");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("Failed to exchange token. Status: {}, Body: {}", response.statusCode(), response.body());
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to exchange token");
                return;
            }

            // parse JSON response
            Map<String, Object> tokenResponse = objectMapper.readValue(response.body(), Map.class);
            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");

            // get user info from from ID token
            String idToken = (String) tokenResponse.get("id_token");
            Map<String, Object> idClaims = decodeJwtPayload(idToken);
            String email = (String) idClaims.get("email");
            String firstName = (String) idClaims.get("given_name");
            String lastName = (String) idClaims.get("family_name");

            logger.info("Authenticating user: {}", email);

            // get roles from access token
            Map<String, Object> accessClaims = decodeJwtPayload(accessToken);
            Map<String, Object> realmAccess = (Map<String, Object>) accessClaims.get("realm_access");
            List<String> roles = Collections.emptyList(); // default to empty
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                roles = (List<String>) realmAccess.get("roles");
            }
            // determine app role
            UserRole appRole = roles.contains("LIBRARIAN") ? UserRole.LIBRARIAN : UserRole.READER;
            logger.debug("Assigned role: {}", appRole);

            // sync with DB
            UserDTO user = userService.findByEmail(email);
            if(user == null) {
                logger.info("User {} not found in database - creating new record.", email);
                user = UserDTO.builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .role(appRole)
                        .registrationDate(LocalDateTime.now())
                        .build();

                try{
                    Long userId = userService.create(user);
                    user.setId(userId);
                    logger.info("User created with ID: {}", userId);
                } catch(SQLException e) {
                    logger.error("Failed to create user in database", e);
                }
            }
            else{
                // existing user - update info if changed
                logger.debug("Updating existing user: {}", email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setRole(appRole);
                userService.update(user);
            }

            // establish local session
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("access_token", accessToken);
            session.setAttribute("refresh_token", refreshToken);
            session.setAttribute("id_token", idToken);

            logger.info("Successfully established session for user: {}", email);

            // redirect to main page
            resp.sendRedirect(req.getContextPath() + "/");

        } catch (InterruptedException e) {
            logger.error("Token exchange interrupted", e);
            Thread.currentThread().interrupt();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            logger.error("Database error during callback processing", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> decodeJwtPayload(String token){
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payloadJson, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}