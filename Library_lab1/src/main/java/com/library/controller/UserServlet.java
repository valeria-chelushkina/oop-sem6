package com.library.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.CreateUserRequest;
import com.library.dto.UserDTO;
import com.library.service.UserService;
import com.library.service.UserServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/users")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UserServlet.class);
    private final UserService userService = new UserServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("GET request for UserServlet");
        try {
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                logger.info("Fetching user by id: {}", id);
                UserDTO user = userService.findById(Long.valueOf(id));
                if (user == null) {
                    logger.warn("User not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, user);
                return;
            }
            logger.info("Fetching all users");
            List<UserDTO> users = userService.findAll();
            writeJson(resp, HttpServletResponse.SC_OK, users);
        } catch (SQLException e) {
            logger.error("Database error in doGet", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in doGet");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("POST request for UserServlet");
        try {
            CreateUserRequest request = objectMapper.readValue(req.getInputStream(), CreateUserRequest.class);
            logger.info("Creating new user: {}", request.getEmail());
            Long createdId = userService.create(request);
            logger.info("User created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "User created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            logger.error("Database error in doPost", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("PUT request for UserServlet");
        try {
            UserDTO request = objectMapper.readValue(req.getInputStream(), UserDTO.class);
            logger.info("Updating user id: {}", request.getId());
            int affected = userService.update(request);
            if (affected == 0) {
                logger.warn("Update failed: User not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }
            logger.info("User updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "User updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            logger.error("Database error in doPut", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in doPut: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        logger.debug("DELETE request for UserServlet, id: {}", id);
        if (id == null || id.isBlank()) {
            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            logger.info("Deleting user with id: {}", id);
            int affected = userService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                logger.warn("Deletion failed: User not found with id: {}", id);
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }
            logger.info("User deleted successfully: id={}", id);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", affected);
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            logger.error("Database error in doDelete", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in doDelete");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), body);
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("error", message);
        writeJson(resp, status, payload);
    }
}