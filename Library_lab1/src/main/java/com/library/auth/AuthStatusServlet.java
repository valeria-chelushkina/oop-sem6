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
import java.util.Map;

@WebServlet("/api/auth/status")
public class AuthStatusServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AuthStatusServlet.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        UserDTO user = (session != null) ? (UserDTO) session.getAttribute("user") : null;
        if(user == null) {
            logger.trace("Auth status check: Unauthenticated");
            writeJson(resp, 200, Map.of("authenticated", false));
        }
        else{
            logger.trace("Auth status check: Authenticated as {}", user.getEmail());
            writeJson(resp, 200, Map.of(
                    "authenticated", true,
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().name()
            ));
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(resp.getWriter(), data);
    }
}
