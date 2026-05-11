package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.AuthorDTO;
import com.library.dto.CreateAuthorRequest;
import com.library.service.AuthorService;
import com.library.service.AuthorServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/authors")
public class AuthorServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AuthorServlet.class);
    private final AuthorService authorService = new AuthorServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
         logger.debug("GET request for AuthorServlet");
         try{
             String id = req.getParameter("id");
             if(id != null && !id.isBlank()){
                 logger.info("Fetching author by id: {}", id);
                 AuthorDTO author = authorService.findById(Long.valueOf(id));
                 if (author == null) {
                     logger.warn("Author not found with id: {}", id);
                     writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
                     return;
                 }
                 writeJson(resp, HttpServletResponse.SC_OK, author);
                 return;
             }
             logger.info("Fetching all authors");
             List<AuthorDTO> authors = authorService.findAll();
             writeJson(resp, HttpServletResponse.SC_OK, authors);
         } catch (SQLException e) {
             logger.error("Database error in doGet", e);
             throw new RuntimeException(e);
         }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        logger.debug("POST request for AuthorServlet");
        try {
            CreateAuthorRequest request = objectMapper.readValue(req.getInputStream(), CreateAuthorRequest.class);
            logger.info("Creating new author: {}", request.getPenName());
            Long createdId = authorService.create(request);
            logger.info("Author created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Author created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            logger.error("Database error in doPost", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in doPost: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("PUT request for AuthorServlet");
        try {
            AuthorDTO request = objectMapper.readValue(req.getInputStream(), AuthorDTO.class);
            logger.info("Updating author id: {}", request.getId());
            int affected = authorService.update(request);
            if (affected == 0) {
                logger.warn("Update failed: Author not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
                return;
            }
            logger.info("Author updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Author updated successfully.");
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
        logger.debug("DELETE request for AuthorServlet, id: {}", id);
        if (id == null || id.isBlank()) {
            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            logger.info("Deleting author with id: {}", id);
            int affected = authorService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                logger.warn("Deletion failed: Author not found with id: {}", id);
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
                return;
            }
            logger.info("Author deleted successfully: id={}", id);
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
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", message);
        writeJson(resp, status, error);
    }
}
