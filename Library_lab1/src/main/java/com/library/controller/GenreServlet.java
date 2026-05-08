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

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet ("/api/genres")
public class GenreServlet extends HttpServlet {

    private final GenreService genreService = new GenreServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try{
            String id = req.getParameter("id");
            if(id != null && !id.isBlank()){
                GenreDTO genre = genreService.findById(Long.valueOf(id));
                if (genre == null) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, genre);
                return;
            }
            List<GenreDTO> genres = genreService.findAll();
            writeJson(resp, HttpServletResponse.SC_OK, genres);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try {
            CreateGenreRequest request = objectMapper.readValue(req.getInputStream(), CreateGenreRequest.class);
            Long createdId = genreService.create(request);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Genre created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            GenreDTO request = objectMapper.readValue(req.getInputStream(), GenreDTO.class);
            int affected = genreService.update(request);
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Genre updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        if (id == null || id.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            int affected = genreService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Genre not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", affected);
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
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
