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

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/authors")
public class AuthorServlet extends HttpServlet {

    private final AuthorService authorService = new AuthorServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
         try{
             String id = req.getParameter("id");
             if(id != null && !id.isBlank()){
                 AuthorDTO author = authorService.findById(Long.valueOf(id));
                 if (author == null) {
                     writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
                     return;
                 }
                 writeJson(resp, HttpServletResponse.SC_OK, author);
                 return;
             }
             List<AuthorDTO> authors = authorService.findAll();
             writeJson(resp, HttpServletResponse.SC_OK, authors);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try {
            CreateAuthorRequest request = objectMapper.readValue(req.getInputStream(), CreateAuthorRequest.class);
            Long createdId = authorService.create(request);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Author created successfully.");
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
            AuthorDTO request = objectMapper.readValue(req.getInputStream(), AuthorDTO.class);
            int affected = authorService.update(request);
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Author updated successfully.");
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
            int affected = authorService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Author not found.");
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
