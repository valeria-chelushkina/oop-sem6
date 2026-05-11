package com.library.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.auth.KeycloakConfig;
import com.library.dto.UserDTO;
import com.library.entity.enums.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

/**
 * Primary Security Filter for the application.
 * Responsibilities:
 * 1. Manages OAuth2 token lifecycle (checks expiration and performs background refreshes).
 * 2. Enforces authentication for protected HTML pages and API endpoints.
 * 3. Handles Role-Based Access Control (RBAC), restricting management paths to LIBRARIANS.
 * 4. Implements Cache-Control headers to prevent leaking sensitive data via browser history.
 */
@WebFilter(urlPatterns = {"/api/*", "/adminManagement.html",
        "/book.html", "/management", "/profile.html", "/profile", "/orders", "/orders.html"})
public class AuthFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(AuthFilter.class);
    ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // set Cache-Control headers to prevent caching of protected pages
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String path = req.getServletPath();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        logger.trace("Filtering request for path: {}", path);

        // allow public files to pass through
        if(path.startsWith("/static/") || path.equals("/index.html") || path.equals("/") || path.equals("/login") || path.equals("/api/auth/status") || path.equals("/book.html")) {
            logger.trace("Path is public, passing through");
            chain.doFilter(request, response);
            return;
        }

        String method = req.getMethod();
        if("GET".equalsIgnoreCase(method)) {
            if(path.startsWith("/api/books") || path.startsWith("/api/authors") || path.startsWith("/api/genres") || path.startsWith("/api/filter") || path.startsWith("/api/book-items") ) {
                logger.trace("Public GET API path, passing through");
                chain.doFilter(request, response);
                return;
            }
        }

        // check for session
        HttpSession session = req.getSession(false);

        if(session != null) {
            String accessToken = (String) session.getAttribute("access_token");
            if(accessToken != null && isTokenExpired(accessToken)) {
                logger.info("Access token expired, attempting refresh");
                String refreshToken = (String) session.getAttribute("refresh_token");
                if(refreshToken != null) {
                    // try to refresh
                    boolean success = refreshTokens(req, refreshToken);
                    if(!success) {
                        logger.warn("Token refresh failed, invalidating session");
                        // refresh failed
                        session.invalidate();
                        resp.sendRedirect(req.getContextPath() + "/login");
                        return;
                    }
                    logger.info("Tokens refreshed successfully");
                }
            }
        }

        UserDTO user = (session!=null) ? (UserDTO) session.getAttribute("user") : null;

        // logic for guests (not logged in)
        if (user == null) {
            logger.warn("Unauthorized access attempt to protected path: {}", path);
            if (path.startsWith("/api/") || path.startsWith("/profile")) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        // role-based access control
        // only librarians can see and get to admin-related info
        if (path.startsWith("/management") || path.startsWith("/adminManagement.html") || path.startsWith("/api/admin")) {
            if (user.getRole() != UserRole.LIBRARIAN) {
                logger.warn("Forbidden access attempt to admin path: {} by user: {}", path, user.getEmail());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Librarians only!");
                return;
            }
        }

        // if all checks - continue
        chain.doFilter(request, response);
    }

    private boolean isTokenExpired(String token) {
        try{
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            long exp = ((Number) claims.get("exp")).longValue();
            long now = System.currentTimeMillis() / 1000;

            //refresh 30 seconds before ot expires
            return (exp - 30) < now;
        } catch (Exception e) {
           logger.error("Error checking token expiration", e);
           return true; // assume it's expired
        }
    }

    private boolean refreshTokens(HttpServletRequest req, String refreshToken) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");
        KeycloakConfig config = objectMapper.readValue(is, KeycloakConfig.class);
        String formData = "grant_type=refresh_token" +
                "&refresh_token=" + refreshToken +
                "&client_id=" + config.clientId +
                "&client_secret=" + config.credentials.secret;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getTokenUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        try{
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                Map<String, Object> res = objectMapper.readValue(response.body(), Map.class);

                HttpSession session = req.getSession();
                session.setAttribute("access_token", res.get("access_token"));
                session.setAttribute("refresh_token", res.get("refresh_token"));
                return true;
            }
            logger.error("Token refresh request failed with status: {}", response.statusCode());
        } catch (InterruptedException e) {
            logger.error("Token refresh process interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token refresh interrupted", e);
        } catch (Exception e) {
            logger.error("Exception during token refresh", e);
            throw new RuntimeException("Failed to refresh token", e);
        }
        return false;
    }
}
