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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@WebFilter(urlPatterns = {"/api/*", "/adminManagement.html",
        "/book.html", "/management"})
public class AuthFilter implements Filter {

    ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        // allow public files to pass through
        if(path.startsWith("/static/") || path.equals("/index.html") || path.equals("/") || path.equals("/login")) {
            chain.doFilter(request, response);
            return;
        }

        String method = req.getMethod();
        if("GET".equalsIgnoreCase(method)) {
            if(path.startsWith("/api/books") || path.startsWith("/api/authors") || path.startsWith("/api/genres") || path.startsWith("/api/filter") ) {
                chain.doFilter(request, response);
                return;
            }
        }

        // check for session
        HttpSession session = req.getSession(false);

        if(session != null) {
            String accessToken = (String) session.getAttribute("access_token");
            if(accessToken != null && isTokenExpired(accessToken)) {
                String refreshToken = (String) session.getAttribute("refresh_token");
                if(refreshToken != null) {
                    // try to refresh
                    boolean success = refreshTokens(req, refreshToken);
                    if(!success) {
                        // refresh failed
                        session.invalidate();
                        resp.sendRedirect(req.getContextPath() + "/login");
                        return;
                    }
                }
            }
        }

        UserDTO user = (session!=null) ? (UserDTO) session.getAttribute("user") : null;

        // logic for guests (not logged in)
        if (user == null) {
            if (path.startsWith("/api/")) {
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Token refresh interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token", e);
        }
        return false;
    }
}
