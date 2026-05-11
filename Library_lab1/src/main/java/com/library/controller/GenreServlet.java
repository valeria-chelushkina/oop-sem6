package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.CreateGenreRequest;
import com.library.dto.GenreDTO;
import com.library.service.GenreService;
import com.library.service.GenreServiceImpl;
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

@WebServlet ("/api/genres")
public class GenreServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(GenreServlet.class);
    private final GenreService genreService = new GenreServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        logger.debug("GET request for GenreServlet");
        try{
            String id = req.getParameter("id");
            if(id != null && !id.isBlank()){
                logger.info("Fetching genre by id: {}", id);
                GenreDTO genre = genreService.findById(Long.valueOf(id));
                if (genre == null) {
                    logger.warn("Genre not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, genre);
                return;
            }
            logger.info("Fetching all genres");
            List<GenreDTO> genres = genreService.findAll();
            writeJson(resp, HttpServletResponse.SC_OK, genres);
        } catch (SQLException e) {
            logger.error("Database error in doGet", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        logger.debug("POST request for GenreServlet");
        try {
            CreateGenreRequest request = objectMapper.readValue(req.getInputStream(), CreateGenreRequest.class);
            logger.info("Creating new genre: {}", request.getName());
            Long createdId = genreService.create(request);
            logger.info("Genre created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Genre created successfully.");
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
        logger.debug("PUT request for GenreServlet");
        try {
            GenreDTO request = objectMapper.readValue(req.getInputStream(), GenreDTO.class);
            logger.info("Updating genre id: {}", request.getId());
            int affected = genreService.update(request);
            if (affected == 0) {
                logger.warn("Update failed: Genre not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                return;
            }
            logger.info("Genre updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Genre updated successfully.");
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
        logger.debug("DELETE request for GenreServlet, id: {}", id);
        if (id == null || id.isBlank()) {
            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            logger.info("Deleting genre with id: {}", id);
            int affected = genreService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                logger.warn("Deletion failed: Genre not found with id: {}", id);
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                return;
            }
            logger.info("Genre deleted successfully: id={}", id);
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
