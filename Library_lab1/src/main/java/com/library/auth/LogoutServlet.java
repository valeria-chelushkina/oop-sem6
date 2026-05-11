package com.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        HttpSession session = req.getSession(false);
        String idToken = null;

        if(session != null){
            idToken = (String) session.getAttribute("id_token");
            session.invalidate();
            System.out.println("Finished the local session.");
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");
        if (is == null) {
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

        resp.sendRedirect(logoutUrl);
    }
}
